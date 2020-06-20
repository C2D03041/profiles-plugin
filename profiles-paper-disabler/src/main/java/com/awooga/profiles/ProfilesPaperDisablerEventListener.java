package com.awooga.profiles;

import com.awooga.profiles.events.PlayerUUIDOverrideEvent;
import com.google.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ProfilesPaperDisablerEventListener implements Listener {

	@Inject
	ProfilesPaperCoreSDK sdk;

	@EventHandler
	public void onUUIDOverrideEvent(PlayerUUIDOverrideEvent ev) {
		if(!ev.getCurrentUuid().equals(ev.getOriginalUuid())) {
			System.out.println("Got PlayerUUIDOverrideEvent for "+ev.getPlayer().getDisplayName()+" - reconnecting the user on their genuine profile");
			sdk.switchPlayerToProfile(ev.getPlayer(), ev.getOriginalUuid(), ev.getOriginalUuid());
		}

	}
}
