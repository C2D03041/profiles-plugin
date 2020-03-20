package com.awooga.profiles;

import com.awooga.profiles.dao.PlayerProfilesDAO;
import com.awooga.profiles.dao.impl.PlayerProfilesDAOImpl;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;

public final class ProfilesPaperGuiPlugin extends JavaPlugin {

    @Inject
    private ProfilesCommand profilesCommand;

    @Inject
    Connection connection;

    @Inject
    ProfilesPaperGuiEventListener profilesPaperGuiEventListener;

    @Inject
    PlayerProfilesDAO playerProfilesDAO;

    @Inject
    ProfilesPlaceholderExpansion profilesPlaceholderExpansion;

    @SneakyThrows
    @Override
    public void onEnable() {
        if(!this.checkIfBungee()){ return; }

        ProfilesPaperGuiModule module = new ProfilesPaperGuiModule(this);
        Injector injector = module.createInjector();
        injector.injectMembers(this);

        saveDefaultConfig();
        playerProfilesDAO.applyMigrations();

        getCommand("profiles").setExecutor(profilesCommand);
        getServer().getPluginManager().registerEvents(profilesCommand, this);
        getServer().getPluginManager().registerEvents(profilesPaperGuiEventListener, this);

        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            profilesPlaceholderExpansion.register();
        } else {
            throw new Exception("Couldn't initialize ProfilesPaperGuiPlugin -- missing PlaceholderAPI plugin");
        }
    }

    @SneakyThrows
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // we check like that if the specified server is BungeeCord.
    private boolean checkIfBungee()
    {
        // we check if the server is Spigot/Paper (because of the spigot.yml file)
        if ( !getServer().getVersion().contains( "Spigot" ) && !getServer().getVersion().contains( "Paper" ) )
        {
            getLogger().severe( "You probably run CraftBukkit... Please update to spigot." );
            getLogger().severe( "Plugin disabled!" );
            getServer().getPluginManager().disablePlugin( this );
            return false;
        }
        if ( getServer().spigot().getConfig().getConfigurationSection("settings").getBoolean( "settings.bungeecord" ) )
        {
            getLogger().severe( "This server is not using BungeeCord." );
            getLogger().severe( "If the server is already hooked to BungeeCord, please enable it into your spigot/paper config as well." );
            getLogger().severe( "Plugin disabled!" );
            getServer().getPluginManager().disablePlugin( this );
            return false;
        }
        return true;
    }
}
