package com.awooga.profiles.chestgui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.ChatPaginator;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class DefaultStatefulItemStackSupplier<S> implements StatefulItemStackSupplier<S> {
	@Override
	public StatefulItemStack get(Integer slot, String legendName, TextSupplier getText) {
		return DefaultStatefulItemStackSupplier.<S>getStatic(slot, legendName, getText);
	}

	static public <S> StatefulItemStack<S> getStatic(Integer slot, String legendName, TextSupplier getText) {
		if("DIVIDER".equals(legendName)) {
			return getDivider();
		} else if("NOTHING".equals(legendName)) {
			return getEmpty();
		}

		Material mat = Material.matchMaterial(legendName);
		if(mat != null) {
			return StatefulItemStack.<S>builder()
				.state(null)
				.itemStack(generateItem(
					new ItemStack(mat),
					" ",
					null
				))
			.build();
		}

		return null;
	}

	static public <S> StatefulItemStack<S> getDivider() {
		return StatefulItemStack.<S>builder()
			.state(null)
			.itemStack(generateItem(
				new ItemStack(Material.WHITE_STAINED_GLASS_PANE),
				" ",
				null
			))
		.build();
	}

	static public <S> StatefulItemStack<S> getEmpty() {
		return StatefulItemStack.<S>builder()
			.state(null)
			.itemStack(DefaultStatefulItemStackSupplier.generateItem(
				new ItemStack(Material.BLACK_STAINED_GLASS_PANE),
				" ",
				null
			))
		.build();
	}


	public static ItemStack generateItem(ItemStack item, String title, String body) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(title);
		im.setLore(
			body == null || "".equals(body) ? null :
				Arrays.stream(body.split("\n"))
					.map(line -> ChatPaginator.wordWrap(line, 30))
					.flatMap(Arrays::stream)
					.collect(Collectors.toList())
		);
		item.setItemMeta(im);
		return item;
	}
}
