package com.awooga.profiles;

import com.awooga.profiles.chestgui.ChestGui;
import com.awooga.profiles.chestgui.ChestGuiGenerator;
import com.awooga.profiles.chestgui.DefaultStatefulItemStackSupplier;
import com.awooga.profiles.chestgui.StatefulItemStack;
import com.awooga.profiles.dao.PlayerProfilesDAO;
import com.awooga.profiles.dao.ProfileEntity;
import com.awooga.profiles.fsm.BoundUserFunction;
import com.awooga.profiles.fsm.core.EventType;
import com.awooga.profiles.fsm.events.BukkitCommandEvent;
import com.awooga.profiles.fsm.BukkitEventFSM;
import com.awooga.profiles.fsm.core.Transition;
import com.awooga.profiles.util.HiddenStringUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
//import net.Indyuce.mmocore.MMOCore;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.ChatPaginator;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProfilesCommand2 extends BukkitEventFSM<ProfilesCommandState> implements CommandExecutor {
	public static final String DEFAULT_PROFILE_SLOTS_CONFIG_PATH = "options.defaultProfileSlots";
	public static final String PROFILE_SLOTS_BY_PERMISSION_CONFIG_PATH = "options.profileSlotsByPermission";

	@Inject
	ChestGuiGenerator chestGuiGenerator;
	@Inject
	PlayerProfilesDAO playerProfilesDAO;
	@Inject
	ProfilesPaperCoreSDK profilesPaperCoreSDK;
	@Inject
	Configuration config;
	@Inject
	Plugin plugin;
	//@Inject
	//Optional<MMOCore> maybeMMOCore;

	@Getter
	private final List<Transition<ProfilesCommandState, Event>> transitions = ImmutableList.<Transition<ProfilesCommandState, Event>>builder()
		// initial open
		.add(Transition.<ProfilesCommandState, Event>builder()
			.from("closed")
			.to("mainOpened")
			.onEvent(BukkitCommandEvent.class)
			.onTransit((from, to, s, event) -> {
				return s.toBuilder()
					.menuMode("SELECT")
				.build();
			})
		.build())

		// closed
		.add(Transition.<ProfilesCommandState, Event>builder()
			.from("mainOpened")
			.to("closed")
			.onEvent(InventoryCloseEvent.class)
			.onTransit((from, to, s, rawEvent) -> {
				InventoryCloseEvent event = (InventoryCloseEvent) rawEvent;
				ChestGui<UUID> gui = chestGuiGenerator.createNewGui("gui.profileSelectorMain", (slot, legendName, getText) -> null);
				if(!event.getView().getTitle().equals(gui.getWindowTitle((Player) event.getPlayer()))) {
					return null;
				}
				return s;
			})
		.build())

		// closed
		.add(Transition.<ProfilesCommandState, Event>builder()
			.from("deleteOpened")
			.to("closed")
			.onEvent(InventoryCloseEvent.class)
			.onTransit((from, to, s, rawEvent) -> {
				InventoryCloseEvent event = (InventoryCloseEvent) rawEvent;
				ChestGui<UUID> gui = chestGuiGenerator.createNewGui("gui.profileSelectorDeleteConfirm", (slot, legendName, getText) -> null);
				if(!event.getView().getTitle().equals(gui.getWindowTitle((Player) event.getPlayer()))) {
					return null;
				}
				return s;
			})
		.build())

		// mode change
		.add(Transition.<ProfilesCommandState, Event>builder()
			.from("mainOpened")
			.to("mainOpened")
			.onEvent(InventoryClickEvent.class)
			.onTransit((from, to, s, plainEvent) -> {
				InventoryClickEvent event = (InventoryClickEvent) plainEvent;
				Player player = (Player) event.getWhoClicked();
				event.setCancelled(true);
				Integer slotClicked = event.getRawSlot();
				if(slotClicked == -999) {
					return null;
				}
				ChestGui<UUID> gui = this.guiMap.get(player);
				String legendName = gui.getLegendNameBySlot(slotClicked);
				//System.out.println("Got legend name: "+legendName);
				if(!"SELECT_PROFILE".equals(legendName) && !"DELETE_PROFILE".equals(legendName)) {
					// reject the transition
					//System.out.println("Not a valid state change, rejecting this transition");
					return null;
				}
				return s.toBuilder()
					.menuMode("SELECT_PROFILE".equals(legendName) ? "SELECT" : "DELETE")
				.build();
			})
		.build())

		// clicking on a profile slot (select)
		.add(Transition.<ProfilesCommandState, Event>builder()
			.from("mainOpened")
			.to("mainOpened")
			.onEvent(InventoryClickEvent.class)
			.onTransit((from, to, s, plainEvent) -> {
				InventoryClickEvent event = (InventoryClickEvent) plainEvent;
				Player player = (Player) event.getWhoClicked();

				Triplet<@NotNull Boolean, UUID, UUID> cont = this.verifyInventoryClickEvent(event, player, s, "CHAR_SLOT", true);
				//System.out.println("Got triplet in mainOpened transition: "+cont);
				//System.out.println("Current state: "+s);

				Boolean validationPassed = cont.getValue0();
				UUID newUuid = cont.getValue1();
				UUID targetUuid = cont.getValue2();
				if(!validationPassed){ return null; }
				if(newUuid != null || targetUuid == null) { return s; }
				if(!"SELECT".equals(s.getMenuMode())) {
					return null;
				}

				if(targetUuid.equals(player.getUniqueId())) {
					// can't switch to the selected profile
					return s;
				}

				UUID genuineUuid = playerProfilesDAO.getGenuineUUID(player);
				profilesPaperCoreSDK.switchPlayerToProfile(player, genuineUuid, targetUuid);

				return s;
			})
		.build())

		// clicking on a profile slot (delete)
		.add(Transition.<ProfilesCommandState, Event>builder()
			.from("mainOpened")
			.to("deleteOpened")
			.onEvent(InventoryClickEvent.class)
			.onTransit((from, to, s, plainEvent) -> {
				InventoryClickEvent event = (InventoryClickEvent) plainEvent;
				Player player = (Player) event.getWhoClicked();

				Triplet<@NotNull Boolean, UUID, UUID> cont = this.verifyInventoryClickEvent(event, player, s, "CHAR_SLOT", true);
				//System.out.println("Got triplet in deleteOpened transition: "+cont);

				Boolean validationPassed = cont.getValue0();
				UUID newUuid = cont.getValue1();
				UUID targetUuid = cont.getValue2();
				if(!validationPassed){ return null; }
				if(newUuid != null || targetUuid == null) { return s; }
				if(!"DELETE".equals(s.getMenuMode())) {
					return null;
				}

				UUID genuineUuid = playerProfilesDAO.getGenuineUUID(player);
				if(targetUuid.equals(player.getUniqueId()) || targetUuid.equals(genuineUuid)) {
					// can't delete the selected profile
					return null;
				}

				return s.toBuilder()
					.menuMode(null)
					.attemptDeleteUuid(targetUuid)
				.build();
			})
		.build())


		// clicking on a profile slot
		.add(Transition.<ProfilesCommandState, Event>builder()
			.from("deleteOpened")
			.to("mainOpened")
			.onEvent(InventoryClickEvent.class)
			.onTransit((from, to, s, plainEvent) -> {
				InventoryClickEvent event = (InventoryClickEvent) plainEvent;
				Player player = (Player) event.getWhoClicked();

				Triplet<@NotNull Boolean, UUID, UUID> cont = this.verifyInventoryClickEvent(event, player, s, "BACK", false);
				//System.out.println("Got triplet in deleteOpened transition: "+cont);

				Boolean validationPassed = cont.getValue0();
				UUID newUuid = cont.getValue1();
				UUID targetUuid = cont.getValue2();
				if(!validationPassed){ return null; }

				return s.toBuilder()
					.menuMode("DELETE")
					.attemptDeleteUuid(null)
				.build();
			})
		.build())


		// clicking on a profile slot
		.add(Transition.<ProfilesCommandState, Event>builder()
			.from("deleteOpened")
			.to("mainOpened")
			.onEvent(InventoryClickEvent.class)
			.onTransit((from, to, s, plainEvent) -> {
				InventoryClickEvent event = (InventoryClickEvent) plainEvent;
				Player player = (Player) event.getWhoClicked();

				Triplet<@NotNull Boolean, UUID, UUID> cont = this.verifyInventoryClickEvent(event, player, s, "CONFIRM_DELETE", false);
				//System.out.println("Got triplet in deleteOpened transition: "+cont);

				Boolean validationPassed = cont.getValue0();
				UUID targetUuid = cont.getValue2();
				if(!validationPassed){ return null; }

				playerProfilesDAO.deleteProfile(player, targetUuid);

				return s.toBuilder()
					.menuMode("SELECT")
					.attemptDeleteUuid(null)
				.build();
			})
		.build())
	.build();

	// Pair<validationPassed, newUserUuid, selectedUuid>
	private Triplet<@NotNull Boolean, UUID, UUID> verifyInventoryClickEvent(InventoryClickEvent event, Player player, ProfilesCommandState state, String legendType, Boolean doCreate) {
		event.setCancelled(true);
		Integer slotClicked = event.getRawSlot();
		ItemStack clicked = event.getCurrentItem(); // The item that was clicked

		if(clicked == null || slotClicked == -999) { // out of the inventory
			return Triplet.with(false, null, null);
		}

		ChestGui<UUID> gui = this.guiMap.get(player);
		String legendName = gui.getLegendNameBySlot(slotClicked);
		//System.out.println("Got legend name: "+legendName);
		if(!legendType.equals(legendName)) {
			//System.out.println("Not a valid state change, rejecting this transition");
			return Triplet.with(false, null, null);
		}

		UUID targetUuid = HiddenStringUtil.getLore(clicked, UUID.class);

		//System.out.println("Got clicked uuid "+targetUuid);

		if(doCreate && targetUuid == null) {
			UUID genuineUUID = playerProfilesDAO.getGenuineUUID(player);
			if(this.getUserMaxSlots(player) < playerProfilesDAO.getProfilesByGenuineUUID(genuineUUID).length + 1) {
				// player has no slots left
				return Triplet.with(false, null, null);
			}
			UUID newUuid = playerProfilesDAO.createNewProfile(player);
			playerProfilesDAO.addBrandNewProfileId(newUuid);
			UUID genuineUuid = playerProfilesDAO.getGenuineUUID(player);
			profilesPaperCoreSDK.switchPlayerToProfile(player, genuineUuid, newUuid);
			return Triplet.with(true, targetUuid, null);
		}

		return Triplet.with(true, null, targetUuid);
	}

	private void createProfile(Player player) {
		//System.out.println("TODO: create profile");
	}

	Map<Player, ChestGui<UUID>> guiMap = new HashMap<>();

	@Getter
	protected final Map<EventType, BoundUserFunction<Player, ProfilesCommandState>> boundEvents = ImmutableMap.of(
		new EventType.ToState("mainOpened"), this::onMainOpen,
		new EventType.ToAndFromState("mainOpened", "mainOpened"), this::onMainOpen,
		new EventType.ToState("deleteOpened"), this::onDeleteOpen,
		//new EventType.ToAndFromState("deleteOpened", "deleteOpened"), this::onDeleteOpen,
		new EventType.ToState("closed"), this::onClose
	);

	@Getter
	protected final String defaultStateName = "closed";
	@Getter
	protected final ProfilesCommandState defaultStateData = ProfilesCommandState.builder().build();

	private void onMainOpen(Player player, String stateName, ProfilesCommandState state) {

		//System.out.println("On main open"+player+" - "+stateName+" - "+state);

		final UUID genuineUuid = playerProfilesDAO.getGenuineUUID(player);
		final List<ProfileEntity> profiles = playerProfilesDAO.getProfileEntitiesByGenuineUUID(genuineUuid);

		final Integer userMaxSlots = this.getUserMaxSlots(player);
		final Integer maxSlots = this.getMaxSlots();
		AtomicReference<Integer> profileSlotIndex = new AtomicReference<>(0);
		//System.out.println("User "+player.getDisplayName()+" has "+userMaxSlots+"/"+maxSlots+" profile slots");

		ChestGui<UUID> gui = chestGuiGenerator.createNewGui("gui.profileSelectorMain", (slot, legendName, getText) -> {
			StatefulItemStack<UUID> item = DefaultStatefulItemStackSupplier.getStatic(slot, legendName, getText);
			if(item != null) { return item; }

			if("CHAR_SLOT".equals(legendName)) {

				Integer i = profileSlotIndex.get();
				Optional<ProfileEntity> tProfile = Optional.ofNullable(i >= profiles.size() ? null : profiles.get(i));
				boolean isSelectedProfile = player.getUniqueId().equals(tProfile.map(ProfileEntity::getProfileUuid).orElse(null));


				/*
				Player dummyPlayer = isEmptyProfile ? player : ProfilePlayerImpl.builder()
					.actualPlayer(player)
					.overrideUuid(tProfile)
				.build();
				*/

				Optional<OfflinePlayer> dummyPlayer = tProfile.map(tProfile2 -> Bukkit.getOfflinePlayer(tProfile2.getProfileUuid()));

				String key = "slotEmpty";
				/*
				String titleKey = "slotEmpty.title";
				String bodyKey = "slotEmpty.body";
				String material = "slotEmpty.material";
				*/
				if("DELETE".equals(state.getMenuMode()) && tProfile.isPresent() && genuineUuid.equals(tProfile)) {
					key = "slotBlockedFromDeletion";
					tProfile = null;
				} else if(isSelectedProfile) {
					key = "slotActive";
				} else if(tProfile.isPresent()) {
					key = "slotCreated";
				} else if(profileSlotIndex.get() >= userMaxSlots) {
					if(profileSlotIndex.get() < maxSlots) {
						key = "slotBlocked";
					} else {
						return DefaultStatefulItemStackSupplier.getEmpty();
					}
				}
				final Optional<ProfileEntity> profile = tProfile;

				/*
				maybeMMOCore.ifPresent(mmoCore -> {
					if(profile.isPresent()) {
						//MMOCore.plugin.dataProvider.getDataManager().setup(dummyPlayer);
					}
				});
				 */

				final OfflinePlayer nullPlayer = dummyPlayer.orElse(null);
				boolean preferCachedPlaceholders = config.getBoolean("options.preferCachedPlaceholders", false);
				StatefulItemStack<UUID> res = StatefulItemStack.<UUID>builder()
					.state(profile.map(ProfileEntity::getProfileUuid).orElse(null))
					.itemStack(DefaultStatefulItemStackSupplier.generateItem(
						new ItemStack(Material.valueOf(getText.get(dummyPlayer.orElse(null), key+".material"))),
						preferCachedPlaceholders && "slotCreated".equals(key)
							? profile
								.map(ProfileEntity::getCachedPlaceholderTitle)
								.filter(v -> !"".equals(v))
								.orElseGet(() -> getText.get(nullPlayer, "slotBrandNew.title"))
							: getText.get(nullPlayer, key+".title"),
						preferCachedPlaceholders && "slotCreated".equals(key)
							? profile.map(ProfileEntity::getCachedPlaceholderBody)
								.filter(v -> !"".equals(v))
								.orElseGet(() -> getText.get(nullPlayer, "slotBrandNew.body"))
							: getText.get(nullPlayer, key+".body")
					))
				.build();
				profileSlotIndex.updateAndGet(v -> v + 1);
				return res;
			} else if("SELECT_PROFILE".equals(legendName)) {
				String key = "selectButton";
				return StatefulItemStack.<UUID>builder()
					.state(null)
					.itemStack(DefaultStatefulItemStackSupplier.generateItem(
						new ItemStack(Material.valueOf(getText.get(player,
							"SELECT".equals(state.getMenuMode()) ? key+".activeMaterial" : key+".inactiveMaterial"
						))),
						getText.get(player,key+".title"),
						getText.get(player,key+".body")
					))
				.build();
			} else if("DELETE_PROFILE".equals(legendName)) {
				String key = "deleteButton";
				return StatefulItemStack.<UUID>builder()
					.state(null)
					.itemStack(DefaultStatefulItemStackSupplier.generateItem(
						new ItemStack(Material.valueOf(getText.get(player,
							"DELETE".equals(state.getMenuMode()) ? key+".activeMaterial" : key+".inactiveMaterial"
						))),
						getText.get(player,key+".title"),
						getText.get(player,key+".body")
					))
				.build();
			} else {
				return DefaultStatefulItemStackSupplier.getEmpty();
			}
			// unreachable
		});
		this.guiMap.put(player, gui);
		this.updateInventory(player, gui.getInventory(player));

	}

	private void updateInventory(Player player, Inventory inventory) {
		Inventory currentInv = player.getOpenInventory().getTopInventory();
		if(currentInv.getSize() != inventory.getSize()) {
			player.openInventory(inventory);
			return;
		}
		for(int i = 0; i < currentInv.getSize(); i++) {
			currentInv.setItem(i, inventory.getItem(i));
		}
	}

	private void onDeleteOpen(Player player, String stateName, ProfilesCommandState state) {
		//System.out.println("On delete open"+player+" - "+stateName+" - "+state);

		final UUID genuineUuid = playerProfilesDAO.getGenuineUUID(player);
		final UUID[] profiles = playerProfilesDAO.getProfilesByGenuineUUID(genuineUuid);

		final Integer userMaxSlots = this.getUserMaxSlots(player);
		final Integer maxSlots = this.getMaxSlots();
		AtomicReference<Integer> profileSlotIndex = new AtomicReference<>(0);
		//System.out.println("User "+player.getDisplayName()+" has "+userMaxSlots+"/"+maxSlots+" profile slots");


		Player dummyPlayer = ProfilePlayerImpl.builder()
			.actualPlayer(player)
			.overrideUuid(state.getAttemptDeleteUuid())
		.build();

		ChestGui<UUID> gui = chestGuiGenerator.createNewGui("gui.profileSelectorDeleteConfirm", (slot, legendName, getText) -> {

			StatefulItemStack<UUID> item = DefaultStatefulItemStackSupplier.getStatic(slot, legendName, getText);
			if(item != null) { return item; }

			if("CONFIRM_DELETE".equals(legendName)) {
				String key = "confirmDeleteButton";
				return StatefulItemStack.<UUID>builder()
					.state(state.getAttemptDeleteUuid())
					.itemStack(DefaultStatefulItemStackSupplier.generateItem(
						new ItemStack(Material.valueOf(getText.get(dummyPlayer, key+".material"))),
						getText.get(dummyPlayer,key+".title"),
						getText.get(dummyPlayer,key+".body")
					))
					.build();
			} else if("BACK".equals(legendName)) {
				String key = "backButton";
				return StatefulItemStack.<UUID>builder()
					.state(null)
					.itemStack(DefaultStatefulItemStackSupplier.generateItem(
						new ItemStack(Material.valueOf(getText.get(dummyPlayer, key+".material"))),
						getText.get(dummyPlayer,key+".title"),
						getText.get(dummyPlayer,key+".body")
					))
					.build();
			} else {
				return DefaultStatefulItemStackSupplier.getEmpty();
			}
			// unreachable
		});
		this.guiMap.put(player, gui);
		this.updateInventory(player, gui.getInventory(player));
	}

	private void onClose(Player player, String stateName, ProfilesCommandState state) {
		if(config.getBoolean("options.disableMojangProfile", true)) {
			UUID genuineUuid = playerProfilesDAO.getGenuineUUID(player);
			if(player.getUniqueId().equals(genuineUuid)) {
				Bukkit.getScheduler().runTask(plugin, bukkitTask -> {
					String[] args = {};
					this.onCommand(player, null, "", args);
				});
			}
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerMoveEvent(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (config.getBoolean("options.disableMojangProfile", true)) {
			UUID genuineUuid = playerProfilesDAO.getGenuineUUID(player);
			if (player.getUniqueId().equals(genuineUuid)) {
				event.getPlayer().sendMessage(ChatColor.RED + "Cancelling your movement because you must choose a profile first");
				event.setCancelled(true);
			}
		}
	}


	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerMoveEvent(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		Bukkit.getScheduler().runTaskLater(plugin, bukkitTask -> {
			if(config.getBoolean("options.disableMojangProfile", true)) {
				UUID genuineUuid = playerProfilesDAO.getGenuineUUID(player);
				if(player.getUniqueId().equals(genuineUuid)) {
					player.sendMessage(ChatColor.GRAY + "Opening the profile selector automatically...");
					String[] args = {};
					this.onCommand(player, null, "", args);
				}
			}
		}, 20);
	}

	@EventHandler
	public void onPlayerConnect(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Bukkit.getScheduler().runTaskLater(plugin, bukkitTask -> {
			if(config.getBoolean("options.disableMojangProfile", true)) {
				UUID genuineUuid = playerProfilesDAO.getGenuineUUID(player);
				if(player.getUniqueId().equals(genuineUuid)) {
					player.sendMessage(ChatColor.GRAY + "Opening the profile selector automatically...");
					String[] args = {};
					this.onCommand(event.getPlayer(), null, "", args);
				}
			}
		}, 20);
	}

	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent event) {
		this.guiMap.remove(event.getPlayer());
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

		Player target = null;
		if(args.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "/profiles only works for players");
				return false;
			}
			if(!sender.hasPermission("profiles.user")) {
				sender.sendMessage(ChatColor.RED + "Missing permission to use /profiles: profiles.user");
				return false;
			}
			target = (Player) sender;
		} else {
			if(!sender.hasPermission("profiles.user-others")) {
				sender.sendMessage(ChatColor.RED + "Missing permission to use /profiles [other]: profiles.user-others");
				return false;
			}
			String arg = args[0];
			target = Bukkit.getPlayer(arg);
			if(target == null) {
				try {
					UUID uuid = UUID.fromString(arg);
					target = Bukkit.getPlayer(uuid);
				} catch(Exception ignored) {
					// can be ignored because of below null check on target
				}
			}
		}

		if(target == null) {
			sender.sendMessage(ChatColor.RED + "Couldn't identify who the target. Usage: /profile [name|uuid]");
			return false;
		}


		Player finalTarget = target;
		Bukkit.getScheduler().runTask(plugin, bukkitTask -> {
			String[] dummyArgs = {};
			this.removeUser(finalTarget);
			this.fire(finalTarget, BukkitCommandEvent.builder()
				.args(args)
				.command(command)
				.label(label)
				.sender(sender)
			.build());
		});
		return true;
	}

	private Integer getUserMaxSlots(Player player) {
		Integer defaultSlots = config.getInt(DEFAULT_PROFILE_SLOTS_CONFIG_PATH, 5);
		if(defaultSlots > 15) {
			defaultSlots = 15;
		}
		Integer maxSlots = defaultSlots;
		Set<String> permissionKeys = config.getConfigurationSection(PROFILE_SLOTS_BY_PERMISSION_CONFIG_PATH).getKeys(true);
		//System.out.println("Permission keys"+permissionKeys);
		for(String key : permissionKeys) {
			//System.out.println("Checking permission "+key);
			if(player != null && !player.hasPermission(key)) {
				continue;
			}
			Integer newMax = config.getInt(PROFILE_SLOTS_BY_PERMISSION_CONFIG_PATH + "." + key, 0);
			if(newMax > maxSlots) {
				maxSlots = newMax;
			}
		}
		return maxSlots;
	}
	private Integer getMaxSlots() {
		return getUserMaxSlots(null);
	}
}
