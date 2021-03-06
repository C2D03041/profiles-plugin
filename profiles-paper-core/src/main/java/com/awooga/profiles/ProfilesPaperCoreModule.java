package com.awooga.profiles;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;

public class ProfilesPaperCoreModule extends AbstractModule {

	private final ProfilesPaperCorePlugin plugin;

	// This is also dependency injection, but without any libraries/frameworks since we can't use those here yet.
	public ProfilesPaperCoreModule(ProfilesPaperCorePlugin plugin) {
		this.plugin = plugin;
	}

	public Injector createInjector() {
		return Guice.createInjector(this);
	}

	@Override
	protected void configure() {
		// Here we tell Guice to use our plugin instance everytime we need it
		this.bind(ProfilesPaperCorePlugin.class).toInstance(this.plugin);
		bind(ProfilesPaperCoreMessageListener.class).in(Singleton.class);
		bind(ProfilesPaperCoreSDK.class).in(Singleton.class);
	}
}