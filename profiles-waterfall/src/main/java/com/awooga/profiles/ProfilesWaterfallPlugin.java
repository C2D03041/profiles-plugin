package com.awooga.profiles;

import com.google.inject.Injector;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import javax.inject.Inject;

public final class ProfilesWaterfallPlugin extends Plugin {

    @Inject
    ProfilesWaterfallEventListener listener;

    @Inject
    SetUUIDCommand setUUIDCommand;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getProxy().registerChannel(ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_NOTIFICATIONS);
        getProxy().registerChannel(ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_NOTIFICATIONS.substring(0, ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_NOTIFICATIONS.length() - 1));
        getProxy().registerChannel(ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_REQUESTS);
        getProxy().registerChannel(ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_REQUESTS.substring(0, ProfilesConstants.BUNGEE_CHANNEL_NAME_FOR_REQUESTS.length() - 1));
        ProfilesWaterfallModule module = new ProfilesWaterfallModule(this);
        Injector injector = module.createInjector();
        injector.injectMembers(this);
        PluginManager manager = getProxy().getPluginManager();
        manager.registerListener(this, listener);
        manager.registerCommand(this, setUUIDCommand);
    }

    public TaskScheduler getScheduler() {
        return this.getProxy().getScheduler();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
