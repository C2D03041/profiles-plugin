package com.awooga.profiles;

import com.awooga.profiles.dao.ProfileDAO;
import com.google.common.base.Optional;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.HashMap;
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

        Optional<String> maybeTargetServer = profileDAO.getUserTargetServer(player);
        if(maybeTargetServer.isPresent()) {
            String targetServer = maybeTargetServer.get();
            uuidSetterHelper.sendUserToOriginalServer(player, targetServer);
            return;
        }

        // TODO: for some reaosn this ServerSwitchEvent runs twice when we're switching
        // so we need to suppress one of the notifications. Downstream channel listneers will need
        // to deduplicate
        if(maybeOverrideUuid.isPresent()) {
            UUID overrideUuid = maybeOverrideUuid.get();
            uuidSetterHelper.setUuidBeforeLogin(player.getPendingConnection(), overrideUuid);
            uuidSetterHelper.notifyServerOfUuidOverride(player);
        }
    }

    @SneakyThrows
    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        System.out.println("Got plugin message event: "+event+" - "+event.getTag());
        if (
            !event.getTag().equals(ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_REQUESTS) &&
            !event.getTag().equals(ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_REQUESTS.substring(0, ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_REQUESTS.length() - 1))
        ) {
            return;
        }
        System.out.println("BUNGEE_CHANNEL_NAME_FOR_REQUESTS received a byte stream...");
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
        String channel = in.readUTF();

        if(
            ProfilesConstants.SWITCH_PLAYER_TO_NEW_PROFILE.equals(channel) ||
            ProfilesConstants.SWITCH_PLAYER_TO_NEW_PROFILE.substring(0, ProfilesConstants.SWITCH_PLAYER_TO_NEW_PROFILE.length() - 1).equals(channel)
        ) {
            UUID genuineUuid = UUID.fromString(in.readUTF());
            UUID profileUuid = UUID.fromString(in.readUTF());
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(genuineUuid);
            if(player == null) {
                throw new Exception("SWITCH_PLAYER_TO_NEW_PROFILE command got a uuid that doesn't refer to an online player");
            }
            uuidSetterHelper.setUuid(player, profileUuid);
        }
    }
}