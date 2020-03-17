package com.awooga.profiles;

import com.awooga.profiles.events.PlayerUUIDOverrideEvent;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

public class ProfilesPaperCoreMessageListener implements PluginMessageListener {
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		System.out.println("Got message from player uuid channel - "+channel+" - "+player);
		if(!channel.equals( ProfilesConstants.BUNGEE_CHANNEL_NAME )) {
			return;
		}
		ByteArrayDataInput in = ByteStreams.newDataInput( message );
		String subChannel = in.readUTF();
		if(subChannel.equalsIgnoreCase(ProfilesConstants.PLAYER_UUID_OVERRIDE_EVENT)){
			String originalUuidString = in.readUTF();
			String currentUuidString = in.readUTF();

			UUID originalUuid = UUID.fromString(originalUuidString);
			UUID currentUuid = UUID.fromString(currentUuidString);

			PlayerUUIDOverrideEvent event = PlayerUUIDOverrideEvent.builder()
				.originalUuid(originalUuid)
				.currentUuid(currentUuid)
			.build();

			System.out.println("Dispatching PlayerUUIDOverrideEvent: "+event);

			Bukkit.getServer().getPluginManager().callEvent(event);
		}
	}
}
