package com.awooga.profiles;

import com.awooga.profiles.dao.PlayerProfilesDAO;
import com.awooga.profiles.events.PlayerUUIDOverrideEvent;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ProfilesPaperGuiEventListener implements Listener {

	@Inject
	ProfilesPaperGuiPlugin plugin;

	@Inject
	PlayerProfilesDAO playerProfilesDAO;

	@Inject
	HookExecutionHelper hookExecutionHelper;

	@EventHandler
	public void onUUIDOverrideEvent(PlayerUUIDOverrideEvent ev) {
		System.out.println("Got PlayerUUIDOverrideEvent");
		playerProfilesDAO.storeUuidOverride(ev.getOriginalUuid(), ev.getCurrentUuid());
		Player player = ev.getPlayer();
		if(playerProfilesDAO.isProfileIdBrandNew(ev.getCurrentUuid())) {
			System.out.println("Running new profile hooks...");
			playerProfilesDAO.removeBrandNewProfileId(ev.getCurrentUuid());
			hookExecutionHelper.executeHooks(player, HookExecutionHelper.PROFILE_CREATE_HOOK);
		}
		hookExecutionHelper.executeHooks(player, HookExecutionHelper.PROFILE_SWITCH_HOOK);
	}

	@EventHandler
	public void onPlayerDisconnectEvent(PlayerQuitEvent ev) {
		playerProfilesDAO.onUserDisconnect(ev.getPlayer());
	}
}
