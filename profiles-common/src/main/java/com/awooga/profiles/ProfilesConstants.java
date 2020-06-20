package com.awooga.profiles;


public final class ProfilesConstants {
    // extra underscore char on the end because viaversion seems to trim the last char for clients
    // before version 1.13 when the server is 1.13
    public final static String BUNGEE_CHANNEL_NAME_FOR_NOTIFICATIONS = "userprofiles:uuid-overrides";
    public static final String BUNGEE_CHANNEL_NAME_FOR_REQUESTS = "userprofiles:set-uuid";

    public static final String PLAYER_UUID_OVERRIDE_EVENT = "PlayerUUIDOverrideEvent";
    public static final String SWITCH_PLAYER_TO_NEW_PROFILE = "SwitchPlayerToNewProfile";
}
