package com.awooga.profiles;

import com.google.inject.*;
import org.bukkit.plugin.Plugin;

public class ProfilesPaperDisablerModule extends AbstractModule {

	private final ProfilesPaperDisablerPlugin plugin;

	// This is also dependency injection, but without any libraries/frameworks since we can't use those here yet.
	public ProfilesPaperDisablerModule(ProfilesPaperDisablerPlugin plugin) {
		this.plugin = plugin;
	}

	public Injector createInjector() {
		return Guice.createInjector(this);
	}

	@Override
	protected void configure() {
		// Here we tell Guice to use our plugin instance everytime we need it
		bind(ProfilesPaperDisablerPlugin.class).toInstance(this.plugin);
		bind(Plugin.class).toInstance(this.plugin);
	}

	@Provides
	@Singleton
	ProfilesPaperCoreSDK providePaperCoreSdk() {
		return ProfilesPaperCoreSDK.getInstance();
	}

}