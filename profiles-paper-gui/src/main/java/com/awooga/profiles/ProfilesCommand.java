package com.awooga.profiles;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ProfilesCommand implements CommandExecutor, Listener {

	public static final String PROFILE_MANAGER_NAME = "Profile Manager";
	public static final String CREATE_PROFILE = "Create New Profile";

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
		if(!(commandSender instanceof Player)) {
			commandSender.sendMessage(new TextComponent(ChatColor.RED + "/profiles only works for players"));
			return false;
		}

		Player player = (Player) commandSender;
		Inventory inv = this.generateMainMenu(player);
		player.openInventory(inv);

		return true;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked(); // The player that clicked the item
		ItemStack clicked = event.getCurrentItem(); // The item that was clicked
		Inventory inventory = event.getInventory(); // The inventory that was clicked in

		if (event.getView().getTitle().equals(PROFILE_MANAGER_NAME)) { // The inventory is our custom Inventory

			event.setCancelled(true); // users can't pick up or insert items ever
			if(clicked == null) { return; }

			/*
			if(clicked.getType() == Material.DIRT) { // The item that the player clicked it dirt
				player.closeInventory();
				player.getInventory().addItem(new ItemStack(Material.DIRT, 1)); // Adds dirt
			}
			 */
			if(clicked.getType() == Material.GRASS) {

			}

		}
	}

	public Inventory generateMainMenu(Player player) {
		Inventory inv = Bukkit.createInventory(null, 36, PROFILE_MANAGER_NAME);
		inv.setItem(35, generateItemWithNameAndLore(new ItemStack(Material.GRASS, 1), CREATE_PROFILE, new String[]{}));

		return inv;
	}

	private ItemStack generateItemWithNameAndLore(ItemStack item, String name, String[] lore) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(Arrays.asList(lore));
		item.setItemMeta(im);
		return item;
	}
}
