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
	PlayerProfilesDAO playerProfilesDAO;

	@EventHandler
	public void onUUIDOverrideEvent(PlayerUUIDOverrideEvent ev) {
		playerProfilesDAO.storeUuidOverride(ev.getOriginalUuid(), ev.getCurrentUuid());
	}

	public void onPlayerDisconnectEvent(PlayerQuitEvent ev) {
		playerProfilesDAO.onUserDisconnect(ev.getPlayer());
	}
}
