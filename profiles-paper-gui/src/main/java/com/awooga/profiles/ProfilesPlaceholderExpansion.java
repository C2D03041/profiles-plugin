package com.awooga.profiles;


import com.awooga.profiles.dao.PlayerProfilesDAO;
import com.google.inject.Inject;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class will be registered through the register-method in the
 * plugins onEnable-method.
 */
public class ProfilesPlaceholderExpansion extends PlaceholderExpansion {

	@Inject
	private ProfilesPaperGuiPlugin plugin;

	@Inject
	private PlayerProfilesDAO playerProfilesDAO;

	private HashMap<Player, UUID> recentlyDeletedUuids = new HashMap<>();

	/**
	 * Because this is an internal class,
	 * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
	 * PlaceholderAPI is reloaded
	 *
	 * @return true to persist through reloads
	 */
	@Override
	public boolean persist(){
		return true;
	}

	/**
	 * Because this is a internal class, this check is not needed
	 * and we can simply return {@code true}
	 *
	 * @return Always true since it's an internal class.
	 */
	@Override
	public boolean canRegister(){
		return true;
	}

	/**
	 * The name of the person who created this expansion should go here.
	 * <br>For convienience do we return the author from the plugin.yml
	 *
	 * @return The name of the author as a String.
	 */
	@Override
	public String getAuthor(){
		return plugin.getDescription().getAuthors().toString();
	}

	/**
	 * The placeholder identifier should go here.
	 * <br>This is what tells PlaceholderAPI to call our onRequest
	 * method to obtain a value if a placeholder starts with our
	 * identifier.
	 * <br>This must be unique and can not contain % or _
	 *
	 * @return The identifier in {@code %<identifier>_<value>%} as String.
	 */
	@Override
	public String getIdentifier(){
		return "playerprofiles";
	}

	/**
	 * This is the version of the expansion.
	 * <br>You don't have to use numbers, since it is set as a String.
	 *
	 * For convienience do we return the version from the plugin.yml
	 *
	 * @return The version as a String.
	 */
	@Override
	public String getVersion(){
		return plugin.getDescription().getVersion();
	}

	/**
	 * This is the method called when a placeholder with our identifier
	 * is found and needs a value.
	 * <br>We specify the value identifier in this method.
	 * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
	 *
	 * @param  player
	 *         A {@link org.bukkit.entity.Player Player}.
	 * @param  identifier
	 *         A String containing the identifier/value.
	 *
	 * @return possibly-null String of the requested identifier.
	 */
	@Override
	public String onRequest(OfflinePlayer player, @NotNull String identifier){

		if(player == null){
			return "";
		}

		// %playerprofiles_profileuuid%
		if(identifier.equals("profileuuid")){
			return plugin.getConfig().getString("profileuuid", playerProfilesDAO.getProfileUUID(player).toString());
		}


		// %playerprofiles_genuineuuid%
		if(identifier.equals("genuineuuid")){
			return plugin.getConfig().getString("genuineuuid", playerProfilesDAO.getGenuineUUID(player).toString());
		}


		if(identifier.equals("deleteduuid")){
			return plugin.getConfig().getString("deleteduuid", this.recentlyDeletedUuids.get(player).toString());
		}

		// We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
		// was provided
		return null;
	}

	void storeRecentlyDeletedProfile(Player player, UUID uuid) {
		this.recentlyDeletedUuids.put(player, uuid);
	}

	void removeRecentlyDeletedProfile(Player player) {
		this.recentlyDeletedUuids.remove(player);
	}
}
