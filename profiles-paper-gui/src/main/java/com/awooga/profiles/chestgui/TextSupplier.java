package com.awooga.profiles.chestgui;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface TextSupplier {
	String get(Player player, String key);
}
