package com.awooga.profiles;

import com.awooga.profiles.dao.ProfileDAO;
import com.google.common.base.Optional;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.graalvm.compiler.api.replacements.Fold;

import javax.inject.Inject;
import java.util.UUID;

public class ProfilesWaterfallEventListener implements Listener {

    @Inject
    UUIDSetterHelper uuidSetterHelper;

    @Inject
    ProfileDAO profileDAO;

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent ev) {
        profileDAO.onUserDisconnect(ev.getPlayer());
    }

    @EventHandler
    public void onLogin(LoginEvent ev) {
        /*
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
        */
    }

    @EventHandler
    public void onServerConnected(ServerSwitchEvent ev) {
        ProxiedPlayer player = ev.getPlayer();

        UUID originalUuid = profileDAO.getOriginalUUID(player).get();
        Optional<UUID> maybeOverrideUuid = profileDAO.getRealUUID(originalUuid);

        if(maybeOverrideUuid.isPresent()) {
            UUID overrideUuid = maybeOverrideUuid.get();
            uuidSetterHelper.setUuidBeforeLogin(player.getPendingConnection(), overrideUuid);
            uuidSetterHelper.notifyServerOfUuidOverride(player);
        }

        Optional<String> maybeTargetServer = profileDAO.getUserTargetServer(player);
        if(maybeTargetServer.isPresent()) {
            String targetServer = maybeTargetServer.get();
            uuidSetterHelper.sendUserToOriginalServer(player, targetServer);
        }
    }
}