package com.awooga.profiles;

import lombok.NoArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class SetUUIDCommand extends Command {
    SetUUIDCommand() {
        super("setuuid");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        commandSender.sendMessage(new TextComponent(ChatColor.GREEN + "Hello World!"));
    }
}
