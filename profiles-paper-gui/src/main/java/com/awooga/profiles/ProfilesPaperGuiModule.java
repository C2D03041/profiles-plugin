package com.awooga.profiles;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ProfilesPaperGuiModule extends AbstractModule {

	private final ProfilesPaperGuiPlugin plugin;

	// This is also dependency injection, but without any libraries/frameworks since we can't use those here yet.
	public ProfilesPaperGuiModule(ProfilesPaperGuiPlugin plugin) {
		this.plugin = plugin;
	}

	public Injector createInjector() {
		return Guice.createInjector(this);
	}

	@Override
	protected void configure() {
		// Here we tell Guice to use our plugin instance everytime we need it
		this.bind(ProfilesPaperGuiPlugin.class).toInstance(this.plugin);
	}
}