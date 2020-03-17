package com.awooga.profiles.dao.impl;

import com.awooga.profiles.dao.PlayerProfilesDAO;
import com.awooga.profiles.events.PlayerUUIDOverrideEvent;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.util.HashMap;
import java.util.UUID;

public class PlayerProfilesDAOImpl implements PlayerProfilesDAO {

	Connection conn;

	//
	HashMap<UUID, UUID> originalUuidMap = new HashMap();

	@SneakyThrows
	@Override
	public void applyMigrations() {
		conn.prepareStatement("CREATE TABLE `profiles` (\n" +
				"  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n" +
				"  `playerUuid` char(36) NOT NULL DEFAULT NULL,\n" +
				"  `profileUuid` char(36) NOT NULL DEFAULT NULL,\n" +
				"  PRIMARY KEY (`id`),\n" +
				"  KEY `playerUuid` (`playerUuid`)\n" +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
	}

	@Override
	public UUID getGenuineUUID(Player player) {
		UUID currentUuid = player.getUniqueId();
		UUID originalUuid = originalUuidMap.get(currentUuid);
		if(originalUuid != null) {
			return originalUuid;
		}
		return currentUuid;
	}

	@Override
	public UUID[] getProfilesByGenuineUUID(UUID uuid) {
		return new UUID[0];
	}

	@Override
	public void storeUuidOverride(UUID original, UUID override) {
		originalUuidMap.put(original, override);
		originalUuidMap.put(override, original);
	}

	@Override
	public void onUserDisconnect(Player player) {
		UUID currentUuid = player.getUniqueId();
		UUID originalUuid = originalUuidMap.get(currentUuid);
		if(originalUuid != null) {
			originalUuidMap.remove(originalUuid);
			originalUuidMap.remove(currentUuid);
		}
	}
}
