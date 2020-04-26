package com.awooga.profiles;

import com.awooga.profiles.dao.PlayerProfilesDAO;
import com.awooga.profiles.dao.impl.PlayerProfilesDAOImpl;
import com.google.inject.*;
import lombok.SneakyThrows;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;

import javax.inject.Named;
import java.sql.Connection;
import java.sql.DriverManager;

public class ProfilesPaperGuiModule extends AbstractModule {

	public static final String MYSQL_CONNECTION_STRING="MYSQL_CONNECTION_STRING";

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
		bind(ProfilesPaperGuiPlugin.class).toInstance(this.plugin);
		bind(Plugin.class).toInstance(this.plugin);

		bind(ProfilesCommand.class);
		bind(PlayerProfilesDAO.class).to(PlayerProfilesDAOImpl.class).in(Singleton.class);
		bind(ProfilesPaperGuiEventListener.class);
		bind(ProfilesPlaceholderExpansion.class).in(Singleton.class);
	}

	@Provides
	Configuration getConfiguration(Plugin plugin) {
		return plugin.getConfig();
	}

	@Provides
	@Singleton
	ProfilesPaperCoreSDK providePaperCoreSdk() {
		return ProfilesPaperCoreSDK.getInstance();
	}

	@Provides
	@Named(MYSQL_CONNECTION_STRING)
	String provideConnectionString() {
		return this.plugin.getConfig().getString("mysql.connector");
	}

	@SneakyThrows
	@Provides
	@Singleton
	Connection provideConnection(
		@Named(MYSQL_CONNECTION_STRING) String connStr
	) {
		return DriverManager.getConnection(connStr);
	}
}