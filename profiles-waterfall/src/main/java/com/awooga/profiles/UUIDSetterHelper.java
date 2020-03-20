package com.awooga.profiles;

import com.awooga.profiles.dao.ProfileDAO;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.google.common.base.Optional;

import javax.inject.Inject;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;

public class UUIDSetterHelper {
	@Inject
	ProfileDAO profileDao;

	public void setUuid(ProxiedPlayer player, UUID newUuid) {
		//PendingConnection conn = ev.getConnection();
		PendingConnection conn = player.getPendingConnection();
		String s = conn.getUniqueId().toString();
		System.out.println("Setting uuid of "+player.getName()+" to \""+newUuid+"\"");

		profileDao.storeOriginalUUID(player);
		UUID originalUuid = profileDao.getOriginalUUID(player).get();

		Boolean didSetUuid = this.setUuidBeforeLogin(conn, newUuid);
		if(!didSetUuid) {
			player.sendMessage(new TextComponent(ChatColor.RED + "Couldn't set UUID on your connection. Internal error"));
			return;
		}
		String currentServerName = player.getServer().getInfo().getName();
		System.out.println("Current server info 1 "+currentServerName);

		Map<String, ServerInfo> servers = ProxyServer.getInstance().getServersCopy();
		java.util.Optional<Map.Entry<String, ServerInfo>> maybeTempServer = servers.entrySet().stream()
				.filter(server -> server.getKey() != currentServerName) // not the server the player is on
				.filter(server -> server.getValue().canAccess(player)) // and the player can connect to it
				.findAny();

		if(!maybeTempServer.isPresent()) {
			player.sendMessage(new TextComponent(ChatColor.RED + "Couldn't set UUID on your connection. This plugin requires at least two servers to switch the user between"));
			return;
		}

		Map.Entry<String, ServerInfo> tempServer = maybeTempServer.get();

		profileDao.setUserTargetServer(player, currentServerName);
		profileDao.storeUUIDOverride(newUuid, originalUuid);

		player.connect(tempServer.getValue(), (success, _unknown) -> {
			System.out.println("First connect callback "+success);
			if(!success) {
				System.out.println("Couldn't connect to temp uuid swap server");
				player.sendMessage(new TextComponent(ChatColor.RED + "Couldn't connect you to the temp uuid swap server: "+tempServer.getValue().getName()));
				return;
			}
		});

		return;
	}

	public void sendUserToOriginalServer(ProxiedPlayer player, String targetServerName) {
		System.out.println("Current server info 2 -- in sendUserToOriginalServer "+targetServerName);
		player.connect(ProxyServer.getInstance().getServerInfo(targetServerName), (success2, _unknown2) -> {
			System.out.println("Second connect callback "+success2);
			if(!success2) {
				System.out.println("Couldn't send you back to the original server");
				player.sendMessage(new TextComponent(ChatColor.RED + "Couldn't send you back to the original server from the swap server. original: " + targetServerName));
				return;
			}
			profileDao.setUserTargetServer(player, null);
			System.out.println("UUID update success");
			UUID originalUUID = profileDao.getOriginalUUID(player).get();
			player.sendMessage(new TextComponent(ChatColor.GREEN + "Your UUID has been set to: " + profileDao.getRealUUID(originalUUID).get()));
		});
	}

	public boolean setUuidBeforeLogin(PendingConnection conn , UUID newUuid) {
		try {
			FieldUtils.writeField(conn, "uniqueId", newUuid, true);
		} catch (IllegalAccessException e) {
			System.out.println("Couldn't set field uniqueid: " + e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void notifyServerOfUuidOverride(ProxiedPlayer player) {
		Optional<UUID> originalUuid = profileDao.getOriginalUUID(player);
		UUID currentUuid = player.getUniqueId();

		Optional<String> maybeTargetServer = profileDao.getUserTargetServer(player);
		if(maybeTargetServer.isPresent()) {
			return;
		}

		System.out.println("Notify stack trace: ");
		new Exception().printStackTrace();

		System.out.println("Checking to see if we should send a player uuid override event... " + originalUuid + " ---- " + currentUuid);
		if(originalUuid.isPresent() && !currentUuid.equals(originalUuid.get())) {
			System.out.println("Sending it");
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("PlayerUUIDOverrideEvent");
			out.writeUTF(originalUuid.get().toString());
			out.writeUTF(currentUuid.toString());
			player.getServer().getInfo().sendData(ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_NOTIFICATIONS, out.toByteArray());
		}
	}
}
