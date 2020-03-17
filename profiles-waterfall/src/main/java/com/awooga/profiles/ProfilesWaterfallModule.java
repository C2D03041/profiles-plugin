package com.awooga.profiles;

import com.awooga.profiles.dao.ProfileDAO;
import com.awooga.profiles.dao.impl.ProfileDAOImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;

public class ProfilesWaterfallModule extends AbstractModule {

    private final ProfilesWaterfallPlugin plugin;

    // This is also dependency injection, but without any libraries/frameworks since we can't use those here yet.
    public ProfilesWaterfallModule(ProfilesWaterfallPlugin plugin) {
        this.plugin = plugin;
    }

    public Injector createInjector() {
        return Guice.createInjector(this);
    }

    @Override
    protected void configure() {
        // Here we tell Guice to use our plugin instance everytime we need it
        this.bind(ProfilesWaterfallPlugin.class).toInstance(this.plugin);
        bind(ProfilesWaterfallEventListener.class);
        bind(ProfileDAO.class).to(ProfileDAOImpl.class).in(Singleton.class);
    }
}