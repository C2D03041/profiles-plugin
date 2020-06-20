package com.awooga.profiles;

import lombok.experimental.Delegate;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;

public class ConfigurationDelegator implements Configuration {
	@Delegate(types=Configuration.class)
	FileConfiguration config;

	@Inject
	Plugin plugin;

	@Inject
	public void init() {
		this.config = plugin.getConfig();
	}

	public void reloadConfig() {
		plugin.reloadConfig();
		this.config = plugin.getConfig();
	}
}
