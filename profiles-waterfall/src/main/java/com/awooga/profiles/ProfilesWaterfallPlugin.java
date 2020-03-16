package com.awooga.profiles;

import com.google.inject.Injector;
import net.md_5.bungee.api.plugin.Plugin;

import javax.inject.Inject;

public final class ProfilesWaterfallPlugin extends Plugin {

    @Inject
    ProfilesWaterfallEventListener listener;

    @Override
    public void onEnable() {
        // Plugin startup logic
        ProfilesWaterfallModule module = new ProfilesWaterfallModule(this);
        Injector injector = module.createInjector();
        injector.injectMembers(this);
        getProxy().getPluginManager().registerListener(this, listener);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
