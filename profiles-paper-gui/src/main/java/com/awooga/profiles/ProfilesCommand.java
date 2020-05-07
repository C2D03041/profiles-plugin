package com.awooga.profiles;

import com.awooga.profiles.dao.PlayerProfilesDAO;
import com.google.inject.Inject;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ProfilesCommand implements CommandExecutor, Listener {
	public static final String PROFILE_MANAGER_NAME = "Profile Manager";
	public static final String CHANGE_PROFILE_NAME = "Change Profile";
	public static final String CREATE_PROFILE = "Create New Profile";
	public static final String SWITCH_TO_PROFILE = "Switch to this profile";
	public static final String DELETE_THIS_PROFILE = "Delete this profile";
	public static final String BACK = "Back";
	public static final String DELETE_PROFILE_CONFIRM = "Confirm Profile Deletion";

	public static final String DEFAULT_PROFILE_SLOTS_CONFIG_PATH = "options.defaultProfileSlots";
	public static final String PROFILE_SLOTS_BY_PERMISSION_CONFIG_PATH = "options.profileSlotsByPermission";
	private static final String NO_MORE_PROFILE_SLOTS = "Out of profile slots";

	@Inject
	PlayerProfilesDAO playerProfilesDAO;

	@Inject
	ProfilesPaperCoreSDK profilesPaperCoreSDK;

	@Inject
	HookExecutionHelper hookExecutionHelper;

	@Inject
	ProfilesPlaceholderExpansion profilesPlaceholderExpansion;

	@Inject
	ProfilesPaperGuiPlugin plugin;

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
		if(!(commandSender instanceof Player)) {
			commandSender.sendMessage(ChatColor.RED + "/profiles only works for players");
			return false;
		}

		if(!commandSender.hasPermission("profiles.user")) {
			commandSender.sendMessage(ChatColor.RED + "Missing permission to use /profiles: profiles.user");
			return false;
		}

		Player player = (Player) commandSender;
		Inventory inv = this.generateMainMenu(player);
		player.openInventory(inv);

		return true;
	}

	private String getFieldText(Player player, UUID profileUuid, String configPath) {
		ProfilePlayerImpl fakePlayer = ProfilePlayerImpl.builder()
			.actualPlayer(player)
			.overrideUuid(profileUuid)
		.build();

		String unexpandedString = plugin.getConfig().getString(configPath);
		if(Objects.isNull(unexpandedString)) {
			return "";
		}

		String expandedString;
		try {
			expandedString = PlaceholderAPI.setPlaceholders(fakePlayer, unexpandedString);
		} catch(Exception e) {
			System.out.println("Caught exception when generating placeholder for /profiles: "+e+" - "+e.getStackTrace());
			expandedString = "Error Generating Text: "+unexpandedString;
		}
		if(Objects.isNull(expandedString)) {
			return "";
		}
		return expandedString;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked(); // The player that clicked the item
		ItemStack clicked = event.getCurrentItem(); // The item that was clicked
		Inventory inventory = event.getInventory(); // The inventory that was clicked in
		java.util.Optional<UUID> maybeSwitchToProfile = Optional.ofNullable(null);
		Integer slotClicked = event.getRawSlot();

		if (event.getView().getTitle().equals(PROFILE_MANAGER_NAME)) { // The inventory is our custom Inventory

			event.setCancelled(true); // users can't pick up or insert items ever
			if(clicked == null) { return; } // click was on an empty slot
			if( slotClicked >= inventory.getSize() ) { return; } // click didn't occur in the chest

			/*
			if(clicked.getType() == Material.DIRT) { // The item that the player clicked it dirt
				player.closeInventory();
				player.getInventory().addItem(new ItemStack(Material.DIRT, 1)); // Adds dirt
			}
			 */
			if(clicked.getType() == Material.GRASS) {
				UUID newUuid = playerProfilesDAO.createNewProfile(player);
				maybeSwitchToProfile = Optional.ofNullable(newUuid);
				playerProfilesDAO.addBrandNewProfileId(newUuid);
			}

			if(clicked.getType() == Material.LAPIS_BLOCK) {
				// TODO: set maybeSwitchToProfile to this specific profile
				UUID newUuid = UUID.fromString(clicked.getItemMeta().getDisplayName());
				//maybeSwitchToProfile = Optional.ofNullable(newUuid);
				Inventory inv = generateProfileMenu(player, newUuid);
				player.openInventory(inv);
			}

			//System.out.println("MaybeSwitchToProfile: "+maybeSwitchToProfile);
		}
		else if (event.getView().getTitle().equals(CHANGE_PROFILE_NAME)) { // The inventory is our custom Inventory
			event.setCancelled(true); // users can't pick up or insert items ever
			if(clicked == null) { return; }
			if( slotClicked >= inventory.getSize() ) { return; } // click didn't occur in the chest

			//System.out.println("In change profile name hook");

			if(clicked.getType() == Material.GRASS_BLOCK) {
				//System.out.println("ItemMeta"+clicked.getItemMeta().getLore());
				UUID newUuid = UUID.fromString(clicked.getItemMeta().getLore().get(0));
				maybeSwitchToProfile = Optional.ofNullable(newUuid);
			}

			if(clicked.getType() == Material.BARRIER) {
				Inventory inv = this.generateMainMenu(player);
				player.openInventory(inv);
			}

			if(clicked.getType() == Material.REDSTONE_BLOCK) {
				UUID newUuid = UUID.fromString(clicked.getItemMeta().getLore().get(0));
				Inventory inv = this.generateDeleteMenu(player, newUuid);
				player.openInventory(inv);
			}
		}
		else if (event.getView().getTitle().equals(DELETE_PROFILE_CONFIRM)) {
			event.setCancelled(true); // users can't pick up or insert items ever
			if(clicked == null) { return; }
			if( slotClicked >= inventory.getSize() ) { return; } // click didn't occur in the chest

			if(clicked.getType() == Material.BARRIER || clicked.getType() == Material.REDSTONE_BLOCK) {
				Inventory inv = this.generateMainMenu(player);
				player.openInventory(inv);
			}

			if(clicked.getType() == Material.GRASS_BLOCK) {
				//System.out.println("Actually run delete now");
				UUID profileUuid = UUID.fromString(clicked.getItemMeta().getLore().get(0));
				profilesPlaceholderExpansion.storeRecentlyDeletedProfile(player, profileUuid);
				hookExecutionHelper.executeHooks(player, HookExecutionHelper.PROFILE_DELETE_HOOK);
				profilesPlaceholderExpansion.removeRecentlyDeletedProfile(player);
				playerProfilesDAO.deleteProfile(player, profileUuid);
				Inventory inv = this.generateMainMenu(player);
				player.openInventory(inv);
			}
		}

		if(maybeSwitchToProfile.isPresent()) {
			UUID targetUuid = maybeSwitchToProfile.get();
			// TODO: send event to bungee to switch profiles
			UUID genuineUuid = playerProfilesDAO.getGenuineUUID(player);
			//System.out.println("Calling sdk.switchPlayerToProfile()");
			profilesPaperCoreSDK.switchPlayerToProfile(player, genuineUuid, targetUuid);
		}
	}

	public Inventory generateDeleteMenu(Player player, UUID uuid) {
		Inventory inv = Bukkit.createInventory(null, 27, DELETE_PROFILE_CONFIRM);
		ItemStack deleteItem = playerProfilesDAO.getGenuineUUID(player).equals(uuid) ?
			generateItemWithNameAndLore(new ItemStack(Material.BARRIER, 1), BACK, new String[]{"The profile with the uuid of your Mojang account cannot be deleted"}) :
			generateItemWithNameAndLore(new ItemStack(Material.GRASS_BLOCK, 1), DELETE_THIS_PROFILE, new String[]{uuid.toString(), "Yes, I am CERTAIN I want to delete the profile with this UUID,", "and I understand that this cannot be reversed."})
		;

		inv.setItem(9+2, deleteItem);
		inv.setItem(9+6, generateItemWithNameAndLore(new ItemStack(Material.REDSTONE_BLOCK, 1), BACK, new String[]{uuid.toString()}));
		inv.setItem(18+4, generateItemWithNameAndLore(new ItemStack(Material.BARRIER, 1), BACK, new String[]{}));
		return inv;
	}

	public Inventory generateProfileMenu(Player player, UUID uuid) {
		Inventory inv = Bukkit.createInventory(null, 27, CHANGE_PROFILE_NAME);

		inv.setItem(9+2, generateItemWithNameAndLore(new ItemStack(Material.GRASS_BLOCK, 1),
				SWITCH_TO_PROFILE, new String[]{uuid.toString()}));
		inv.setItem(9+6, generateItemWithNameAndLore(new ItemStack(Material.REDSTONE_BLOCK, 1),
				DELETE_THIS_PROFILE, new String[]{uuid.toString(), "There is a confirmation screen after you click"}));
		inv.setItem(18+4, generateItemWithNameAndLore(new ItemStack(Material.BARRIER, 1), BACK, new String[]{}));
		return inv;
	}

	public Inventory generateMainMenu(Player player) {
		Inventory inv = Bukkit.createInventory(null, 36, PROFILE_MANAGER_NAME);
		UUID genuineUuid = playerProfilesDAO.getGenuineUUID(player);
		UUID[] profiles = playerProfilesDAO.getProfilesByGenuineUUID(genuineUuid);

		Integer defaultSlots = plugin.getConfig().getInt(DEFAULT_PROFILE_SLOTS_CONFIG_PATH, 5);
		if(defaultSlots > 15) {
			defaultSlots = 15;
		}
		Integer maxSlots = defaultSlots;
		Set<String> permissionKeys = plugin.getConfig().getConfigurationSection(PROFILE_SLOTS_BY_PERMISSION_CONFIG_PATH).getKeys(true);
		//System.out.println("Permission keys"+permissionKeys);
		for(String key : permissionKeys) {
			//System.out.println("Checking permission "+key);
			if(!player.hasPermission(key)) {
				continue;
			}
			Integer newMax = plugin.getConfig().getInt(PROFILE_SLOTS_BY_PERMISSION_CONFIG_PATH + "." + key, 0);
			if(newMax > maxSlots) {
				maxSlots = newMax;
			}
		}
		//System.out.println("Max slots: "+maxSlots+" - profile length: "+profiles.length);
		ItemStack createButtonItem = maxSlots <= profiles.length ?
			generateItemWithNameAndLore(new ItemStack(Material.REDSTONE_BLOCK, 1), NO_MORE_PROFILE_SLOTS, new String[]{}) :
			generateItemWithNameAndLore(new ItemStack(Material.GRASS, 1), CREATE_PROFILE, new String[]{});

		inv.setItem(35, createButtonItem);

		int index = 0;
		for(UUID profile : profiles) {

			boolean isSelectedProfile = player.getUniqueId().equals(profile);
			String bodyText = this.getFieldText(player, profile, isSelectedProfile ? "text.profileSlotBodySelected" : "text.profileSlotBodyUnselected");
			String titleText = this.getFieldText(player, profile, isSelectedProfile ? "text.profileSlotTitleSelected" : "text.profileSlotTitleUnselected");
			inv.setItem(index, generateItemWithNameAndLore(
				new ItemStack(isSelectedProfile ? Material.EMERALD_BLOCK : Material.LAPIS_BLOCK, 1),
				titleText,
				bodyText.split("\n")
			));
			index++;
		}

		while(index < maxSlots) {
			String titleText = this.getFieldText(player, player.getUniqueId(), "text.emptyProfileSlotTitle");
			String bodyText = this.getFieldText(player, player.getUniqueId(), "text.emptyProfileSlotBody");
			inv.setItem(index, generateItemWithNameAndLore(
				new ItemStack(Material.BIRCH_BUTTON, 1),
				titleText,
				bodyText.split("\n")
			));
			index++;
		}

		return inv;
	}

	private ItemStack generateItemWithNameAndLore(ItemStack item, String name, String[] lore) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(Arrays.asList(lore));
		item.setItemMeta(im);
		return item;
	}
}
