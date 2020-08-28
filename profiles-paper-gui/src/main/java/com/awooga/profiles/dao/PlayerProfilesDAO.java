package com.awooga.profiles.dao;

import lombok.SneakyThrows;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.javatuples.Pair;

import java.util.List;
import java.util.UUID;

public interface PlayerProfilesDAO {
	public void applyMigrations();

	public UUID getGenuineUUID(OfflinePlayer player);
	public UUID getProfileUUID(OfflinePlayer player);

	UUID createNewProfile(Player player);

	boolean deleteProfile(Player player, UUID profile);

	@Deprecated(forRemoval=true)
	UUID[] getProfilesByGenuineUUID(UUID uuid);

	List<ProfileEntity> getProfileEntitiesByGenuineUUID(UUID uuid);

	void save(ProfileEntity ent);

	void storeUuidOverride(UUID current, UUID override);

	void onUserDisconnect(Player player);

	void addBrandNewProfileId(UUID uuid);

	boolean isProfileIdBrandNew(UUID uuid);

	void removeBrandNewProfileId(UUID uuid);
}
