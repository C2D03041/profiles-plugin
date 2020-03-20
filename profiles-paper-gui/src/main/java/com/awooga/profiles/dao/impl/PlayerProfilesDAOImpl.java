package com.awooga.profiles.dao.impl;

import com.awooga.profiles.dao.PlayerProfilesDAO;
import com.awooga.profiles.events.PlayerUUIDOverrideEvent;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class PlayerProfilesDAOImpl implements PlayerProfilesDAO {

	@Inject
	Connection conn;

	HashSet<UUID> brandNewUuids = new HashSet();
	HashMap<UUID, UUID> originalUuidMap = new HashMap();
	HashSet<UUID> genuineUuids = new HashSet<>();

	@SneakyThrows
	@Override
	public void applyMigrations() {
		PreparedStatement stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `profiles` (\n" +
				"  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n" +
				"  `playerUuid` char(36) NOT NULL,\n" +
				"  `profileUuid` char(36) NOT NULL,\n" +
				"  `deleted` tinyint(4) NOT NULL,\n" +
				"  PRIMARY KEY (`id`),\n" +
				"  KEY `playerUuid` (`playerUuid`)\n" +
				") ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4");
		stmt.execute();
	}

	@Override
	public UUID getGenuineUUID(Player player) {
		UUID currentUuid = player.getUniqueId();
		UUID originalUuid = originalUuidMap.get(currentUuid);
		System.out.println("Trying to determine genuine uuid - "+currentUuid+" --- "+originalUuid);
		if(originalUuid != null) {
			return genuineUuids.contains(originalUuid) ? originalUuid : currentUuid;
		}
		return currentUuid;
	}

	@Override
	public UUID getProfileUUID(Player player) {
		UUID genuineUUID = getGenuineUUID(player);
		UUID profileUUID = originalUuidMap.get(genuineUUID);
		return profileUUID != null ? profileUUID : genuineUUID;
	}

	@SneakyThrows
	@Override
	public UUID createNewProfile(Player player) {
		UUID genuineUuid = getGenuineUUID(player);
		PreparedStatement stmt = conn.prepareStatement("INSERT INTO profiles (playerUuid, profileUuid) VALUES (?, ?)");
		UUID newUuid = UUID.randomUUID();
		stmt.setString(1, genuineUuid.toString());
		stmt.setString(2, newUuid.toString());
		int result = stmt.executeUpdate();
		if(result == 0) {
			throw new Exception("Couldn't create new profile for genuineUuid: "+genuineUuid);
		}
		return newUuid;
	}

	@SneakyThrows
	@Override
	public boolean deleteProfile(Player player, UUID profileUuid) {
		UUID genuineUuid = getGenuineUUID(player);
		PreparedStatement stmt = conn.prepareStatement("UPDATE profiles SET deleted=true WHERE playerUuid=? AND profileUuid=?");
		stmt.setString(1, genuineUuid.toString());
		stmt.setString(2, profileUuid.toString());
		return stmt.execute();
	}

	@SneakyThrows
	@Override
	public UUID[] getProfilesByGenuineUUID(UUID genuineUuid) {
		PreparedStatement stmt = conn.prepareStatement("SELECT profileUuid FROM profiles WHERE playerUuid=? AND deleted=false");
		stmt.setString(1, genuineUuid.toString());
		ResultSet resultSet = stmt.executeQuery();
		ArrayList<UUID> result = new ArrayList();

		result.add(genuineUuid);
		while (resultSet.next()) {
			result.add(UUID.fromString(resultSet.getString(1)));
		}

		return result.toArray(new UUID[result.size()]);
	}

	@Override
	public void storeUuidOverride(UUID original, UUID override) {
		originalUuidMap.put(original, override);
		originalUuidMap.put(override, original);
		genuineUuids.add(original);
	}

	@Override
	public void onUserDisconnect(Player player) {
		UUID currentUuid = player.getUniqueId();
		UUID originalUuid = originalUuidMap.get(currentUuid);
		if(originalUuid != null) {
			originalUuidMap.remove(originalUuid);
			originalUuidMap.remove(currentUuid);
		}
		genuineUuids.remove(originalUuid);
		genuineUuids.remove(currentUuid);
	}

	@Override
	public void addBrandNewProfileId(UUID uuid) {
		brandNewUuids.add(uuid);
	}

	@Override
	public boolean isProfileIdBrandNew(UUID uuid) {
		return brandNewUuids.contains(uuid);
	}

	@Override
	public void removeBrandNewProfileId(UUID uuid) {
		brandNewUuids.remove(uuid);
	}
}
