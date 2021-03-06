package com.awooga.profiles;

import com.awooga.profiles.chestgui.ChestGui;
import com.awooga.profiles.chestgui.ChestGuiGenerator;
import com.awooga.profiles.dao.PlayerProfilesDAO;
import com.awooga.profiles.dao.ProfileEntity;
import com.awooga.profiles.events.PlayerUUIDOverrideEvent;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProfilesPaperGuiEventListener implements Listener {

	@Inject
	ProfilesPaperGuiPlugin plugin;

	@Inject
	PlayerProfilesDAO playerProfilesDAO;

	@Inject
	HookExecutionHelper hookExecutionHelper;

	@Inject
	Configuration config;

	@Inject
	ChestGuiGenerator chestGuiGenerator;

	@EventHandler(priority=EventPriority.LOWEST)
	public void onUUIDOverrideEvent(PlayerUUIDOverrideEvent ev) {
		//System.out.println("Got PlayerUUIDOverrideEvent");
		playerProfilesDAO.storeUuidOverride(ev.getOriginalUuid(), ev.getCurrentUuid());
		Player player = ev.getPlayer();
		if(playerProfilesDAO.isProfileIdBrandNew(ev.getCurrentUuid())) {
			//System.out.println("Running new profile hooks...");
			playerProfilesDAO.removeBrandNewProfileId(ev.getCurrentUuid());
			hookExecutionHelper.executeHooks(player, HookExecutionHelper.PROFILE_CREATE_HOOK);
		}
		hookExecutionHelper.executeHooks(player, HookExecutionHelper.PROFILE_SWITCH_HOOK);
	}

	// MUST be lowest priority to run before other cleanup hooks of other plugins, since this call relies on the data
	// that those other plugins might clean up
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent ev) {
		boolean preferCachedPlaceholders = config.getBoolean("options.preferCachedPlaceholders", false);
		if(!preferCachedPlaceholders) {
			return; // not necessary if we won't actually use the cache
		}

		Player player = ev.getPlayer();
		UUID genuineUuid = playerProfilesDAO.getGenuineUUID(player);
		if(player.getUniqueId().equals(genuineUuid)) {
			return;
		}

		List<ProfileEntity> profiles = playerProfilesDAO.getProfileEntitiesByGenuineUUID(genuineUuid);
		Optional<ProfileEntity> maybeProfileEntity = profiles.stream().filter(p -> player.getUniqueId().equals(p.getProfileUuid())).findAny();

		if(!maybeProfileEntity.isPresent()) {
			return;
        }

		ProfileEntity profileEntity = maybeProfileEntity.get();

		ChestGui<Object> chestGui = chestGuiGenerator.createNewGui("gui.profileSelectorMain", null);

		profileEntity = profileEntity.toBuilder()
			.cachedPlaceholderTitle(chestGui.getText(player, "slotCreated.title"))
			.cachedPlaceholderBody(chestGui.getText(player, "slotCreated.body"))
		.build();

		playerProfilesDAO.save(profileEntity);
	}

	@EventHandler
	public void onPlayerDisconnectEvent(PlayerQuitEvent ev) {
		playerProfilesDAO.onUserDisconnect(ev.getPlayer());
	}
}
