package com.awooga.profiles;

import com.google.inject.Injector;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import javax.inject.Inject;

public final class ProfilesWaterfallPlugin extends Plugin {

    @Inject
    ProfilesWaterfallEventListener listener;

    @Inject
    SetUUIDCommand setUUIDCommand;

    @Override
    public void onEnable() {
        // Plugin startup logic
        ProfilesWaterfallModule module = new ProfilesWaterfallModule(this);
        Injector injector = module.createInjector();
        injector.injectMembers(this);
        PluginManager manager = getProxy().getPluginManager();
        manager.registerListener(this, listener);
        manager.registerCommand(this, setUUIDCommand);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
