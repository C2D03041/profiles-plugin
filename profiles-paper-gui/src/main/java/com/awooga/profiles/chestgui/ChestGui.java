package com.awooga.profiles.chestgui;

import com.awooga.profiles.util.HiddenStringUtil;
import lombok.Builder;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Builder
public class ChestGui<S> {
    @Inject
    Configuration config;

    StatefulItemStackSupplier<S> supplier;
    String configKey;

    public Inventory getInventory(Player player) {
        List<String> slotConfig = this.config.getStringList(this.configKey+".slots");

        List<StatefulItemStack<S>> storedState = new ArrayList<>();
        AtomicReference<Integer> i = new AtomicReference<>(0);
        slotConfig.forEach(row -> {
            Arrays.asList(row.split(",")).forEach(slotChar -> {
                String legendName = this.config.getString(this.configKey+".legend."+slotChar);
                StatefulItemStack<S> stack = supplier.get(i.get(), legendName, this::getText)
                    .toBuilder()
                    .legendName(legendName)
                .build();
                storedState.add(stack);
                i.set(i.get() + 1);
            });
        });

        String windowTitle = this.getWindowTitle(player);
        Inventory inv = Bukkit.createInventory(player, storedState.size(), windowTitle);
        i.set(0);
        storedState.forEach(stack -> {
            ItemStack itemStack = stack.getItemStack();
            S state = stack.getState();
            HiddenStringUtil.addLore(itemStack, state);

            inv.setItem(i.get(), itemStack);
            i.set(i.get()+1);
        });

        return inv;
    }

    public String getWindowTitle(Player player) {
        return this.getText(player,"windowTitle");
    }

    public String getLegendNameBySlot(Integer i) {
        List<String> slotConfig = this.config.getStringList(this.configKey+".slots");
        Integer j = 0;
        for(String row: slotConfig) {
            List<String> cells = Arrays.asList(row.split(","));
            if(j + cells.size() > i) {
                String slotChar = cells.get(i - j);
                String legendName = this.config.getString(this.configKey+".legend."+slotChar);
                return legendName;
            }
            j += cells.size();
        }
        return null;
    }

    public String getText(OfflinePlayer player, String key) {
        return getTextRaw(player, this.configKey + ".text."+ key);
    }

    String getTextRaw(OfflinePlayer player, String key) {
        String unexpandedText = config.getString(key);
        try {
            return PlaceholderAPI.setPlaceholders((OfflinePlayer) player, unexpandedText == null ? "" : unexpandedText);
        } catch(Exception e) {
            System.out.println("Caught exception when generating placeholder for /profiles: "+e);
            e.printStackTrace();
            return "Exception filling placeholders: "+unexpandedText;
        }
    }
}
