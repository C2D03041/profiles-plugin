package com.awooga.profiles.dao.impl;

import com.awooga.profiles.dao.ProfileDAO;
import com.google.common.base.Optional;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.UUID;

public class ProfileDAOImpl implements ProfileDAO {
	HashMap<ProxiedPlayer, String> targetServerMap = new HashMap();

	// two way mapping of uuids. contains entries for both real -> fake and fake -> real
	HashMap<UUID, UUID> genuineUuidMap = new HashMap();

	HashMap<ProxiedPlayer, UUID> originalUuidMap = new HashMap();

	@Override
	public Optional<UUID> getRealUUID(UUID override) {
		return Optional.fromNullable(genuineUuidMap.get(override));
	}

	@Override
	public void storeUUIDOverride(UUID override, UUID user) {
		UUID storedOverride = genuineUuidMap.get(user);
		if(storedOverride != null) {
			genuineUuidMap.remove(storedOverride);
		}
		genuineUuidMap.remove(override);
		genuineUuidMap.remove(user);
		genuineUuidMap.put(override, user);
		genuineUuidMap.put(user, override);
	}

	@Override
	public void setUserTargetServer(ProxiedPlayer player, String serverName) {
		if(serverName == null) {
			targetServerMap.remove(player);
		} else {
			targetServerMap.put(player, serverName);
		}
	}

	@Override
	public Optional<String> getUserTargetServer(ProxiedPlayer player) {
		return Optional.fromNullable(targetServerMap.get(player));
	}

	@Override
	public void storeOriginalUUID(ProxiedPlayer player) {
		originalUuidMap.put(player, player.getUniqueId());
	}

	@Override
	public Optional<UUID> getOriginalUUID(ProxiedPlayer player) {
		return Optional.fromNullable(originalUuidMap.get(player))
				.or(Optional.fromNullable(player.getUniqueId()));
	}

	@Override
	public void onUserDisconnect(ProxiedPlayer player) {
		Optional<UUID> maybeOriginalUUID = getOriginalUUID(player);

		if(maybeOriginalUUID.isPresent()) {
			UUID originalUUID = maybeOriginalUUID.get();
			UUID storedOverride = genuineUuidMap.get(originalUUID);
			if (storedOverride != null) {
				genuineUuidMap.remove(storedOverride);
			}
			genuineUuidMap.remove(originalUUID);
		}

		originalUuidMap.remove(player);
		targetServerMap.remove(player);
	}
}
