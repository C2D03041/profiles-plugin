package com.awooga.profiles;

import com.google.inject.Inject;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HookExecutionHelper {
	public static final String PROFILE_CREATE_HOOK = "onCreateProfile";
	public static final String PROFILE_SWITCH_HOOK = "onSwitchProfile";
	public static final String PROFILE_DELETE_HOOK = "onDeleteProfile";

	@Inject
	ProfilesPaperGuiPlugin plugin;

	public void executeHooks(Player player, String hookName) {
		List<String> commands = plugin.getConfig().getStringList("hooks."+hookName);
		for(String command : commands) {
			//System.out.println("Running command (unexpanded): "+command);
			String expandedCommand = PlaceholderAPI.setPlaceholders((OfflinePlayer) player, command);

			//System.out.println("Running command (expanded): "+expandedCommand);

			ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
			Bukkit.dispatchCommand(console, expandedCommand);
		}
	}
}
