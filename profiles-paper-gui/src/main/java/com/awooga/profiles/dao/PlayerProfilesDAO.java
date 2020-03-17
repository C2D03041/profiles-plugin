package com.awooga.profiles.dao;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface PlayerProfilesDAO {
	public void applyMigrations();

	public UUID getGenuineUUID(Player player);

	public UUID[] getProfilesByGenuineUUID(UUID uuid);

	void storeUuidOverride(UUID current, UUID override);

	void onUserDisconnect(Player player);
}
