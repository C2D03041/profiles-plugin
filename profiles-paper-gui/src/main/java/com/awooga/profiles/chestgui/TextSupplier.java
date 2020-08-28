package com.awooga.profiles.chestgui;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface TextSupplier {
	String get(OfflinePlayer player, String key);
}
