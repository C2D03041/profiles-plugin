package com.awooga.profiles.dao;

import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface PlayerProfilesDAO {
	public void applyMigrations();

	public UUID getGenuineUUID(Player player);
	public UUID getProfileUUID(Player player);

	UUID createNewProfile(Player player);

	boolean deleteProfile(Player player, UUID profile);

	public UUID[] getProfilesByGenuineUUID(UUID uuid);

	void storeUuidOverride(UUID current, UUID override);

	void onUserDisconnect(Player player);

	void addBrandNewProfileId(UUID uuid);

	boolean isProfileIdBrandNew(UUID uuid);

	void removeBrandNewProfileId(UUID uuid);
}
