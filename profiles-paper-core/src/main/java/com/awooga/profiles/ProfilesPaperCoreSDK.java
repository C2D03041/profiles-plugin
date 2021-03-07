package com.awooga.profiles;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class ProfilesPaperCoreSDK {

	@Inject
	private ProfilesPaperCorePlugin plugin;

	@SneakyThrows
	public static ProfilesPaperCoreSDK getInstance() {
		Plugin plainPlugin = Bukkit.getPluginManager().getPlugin("ProfilesPaperCore");
		if(!(plainPlugin instanceof ProfilesPaperCorePlugin)) {
			throw new Exception("The plugin with the name ProfilesPaperCore is not an instance of ProfilesPaperCorePlugin");
		}
		ProfilesPaperCorePlugin plugin = (ProfilesPaperCorePlugin) plainPlugin;
		return plugin.sdk;
	}

	public boolean switchPlayerToProfile(Player player, UUID profileUuid) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF( ProfilesConstants.SWITCH_PLAYER_TO_NEW_PROFILE );
		out.writeUTF( profileUuid.toString() );
		player.sendPluginMessage( plugin, ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_REQUESTS, out.toByteArray() ); // Send to Bungee
		return true;
	}
}
