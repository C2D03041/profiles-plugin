package com.awooga.profiles.chestgui;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.bukkit.inventory.ItemStack;

@Builder(toBuilder=true)
@Getter
public class StatefulItemStack<S> {
    ItemStack itemStack;
    S state;
    String legendName;
}
