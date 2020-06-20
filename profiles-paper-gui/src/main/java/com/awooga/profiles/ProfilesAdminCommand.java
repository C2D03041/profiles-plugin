package com.awooga.profiles;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

public class ProfilesAdminCommand implements CommandExecutor {

	@Inject
	ConfigurationDelegator configurationDelegator;

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
		if(!(commandSender instanceof Player)) {
			commandSender.sendMessage(ChatColor.RED + "/profilesadmin only works for players");
			return false;
		}

		if(!commandSender.hasPermission("profiles.admin")) {
			commandSender.sendMessage(ChatColor.RED + "Missing permission to use /profilesadmin: profiles.admin");
			return false;
		}

		String subcommand = args.length != 0 ? args[0] : "";

		if("reload".equals(subcommand)) {
			configurationDelegator.reloadConfig();
			commandSender.sendMessage(ChatColor.GREEN + "Configuration has been reloaded");
		} else {
			commandSender.sendMessage(ChatColor.RED + "Unrecognized subcommand: "+subcommand);
		}

		return true;
	}
}
