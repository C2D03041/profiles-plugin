package com.awooga.profiles;

import com.awooga.profiles.events.PlayerUUIDOverrideEvent;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.HashSet;
import java.util.UUID;

public class ProfilesPaperCoreMessageListener implements PluginMessageListener, Listener {

	@Inject
	ProfilesPaperCorePlugin plugin;

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		//System.out.println("Got message from player uuid channel - "+channel+" - "+player);
		if(
			!channel.equals( ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_NOTIFICATIONS) &&
			!channel.equals( ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_NOTIFICATIONS.substring(0, ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_NOTIFICATIONS.length() - 1))
		) {
			return;
		}
		ByteArrayDataInput in = ByteStreams.newDataInput( message );
		String subChannel = in.readUTF();
		if(
			subChannel.equalsIgnoreCase(ProfilesConstants.PLAYER_UUID_OVERRIDE_EVENT) ||
			subChannel.equalsIgnoreCase(ProfilesConstants.PLAYER_UUID_OVERRIDE_EVENT.substring(0, ProfilesConstants.PLAYER_UUID_OVERRIDE_EVENT.length() - 1))
		) {
			String originalUuidString = in.readUTF();
			String currentUuidString = in.readUTF();

			UUID originalUuid = UUID.fromString(originalUuidString);
			UUID currentUuid = UUID.fromString(currentUuidString);

			// necessary because the upstream bungee plugin may not use the correct player channel to notify paper, we
			// should find the correct player using the uuid
			//Player actualPlayer = plugin.getServer().getPlayer(currentUuid);
			//if(actualPlayer == null) {
				Player actualPlayer = player; // use the player thru which bungee communicated to Paper as the user who is getting the assignment
			//}

			//System.out.println("Got actual player: "+actualPlayer);

			// sometimes, there's a bug where the bungee plugin notifies paper twice for the same user. This should
			// suppress that for downstream plugins
			//if(actualPlayer == null || this.suppressDuplicates.contains(actualPlayer)) { return; }
			//this.suppressDuplicates.add(actualPlayer);

			PlayerUUIDOverrideEvent event = PlayerUUIDOverrideEvent.builder()
				.originalUuid(originalUuid)
				.currentUuid(currentUuid)
				.player(actualPlayer)
			.build();

			//System.out.println("Dispatching PlayerUUIDOverrideEvent: "+event);

			Bukkit.getServer().getPluginManager().callEvent(event);
		}
	}
}
