package com.awooga.profiles;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.UUID;

public class ProfilesWaterfallEventListener implements Listener {

    @EventHandler
    public void onLogin(LoginEvent ev) {
        PendingConnection conn = ev.getConnection(); //ev.getPlayer().getPendingConnection()
        String s = conn.getUniqueId().toString();
        System.out.println("USer connecting with uuid: "+s);
        if(s.equals("2c8e8bb8-1a58-45b9-b023-e060a0296e15")) {
            System.out.println("Setting uuid to notch...");
            UUID newUuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");

            try {
                FieldUtils.writeField(conn, "uniqueId", newUuid, true);
            } catch (IllegalAccessException e) {
                System.out.println("Couldn't set field uniqueid: " + e.toString());
                e.printStackTrace();
            }
        }
    }
}