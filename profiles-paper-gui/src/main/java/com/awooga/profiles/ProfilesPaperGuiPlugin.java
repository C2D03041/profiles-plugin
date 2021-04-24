package com.awooga.profiles;

import com.awooga.profiles.dao.PlayerProfilesDAO;
import com.awooga.profiles.dao.impl.PlayerProfilesDAOImpl;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.SneakyThrows;
//import net.Indyuce.mmocore.MMOCore;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.util.Optional;

public final class ProfilesPaperGuiPlugin extends JavaPlugin {

    @Inject
    private ProfilesCommand2 profilesCommand2;

    @Inject
    private ProfilesAdminCommand profilesAdminCommand;

    @Inject
    Connection connection;

    @Inject
    ProfilesPaperGuiEventListener profilesPaperGuiEventListener;

    @Inject
    PlayerProfilesDAO playerProfilesDAO;

    @Inject
    ProfilesPlaceholderExpansion profilesPlaceholderExpansion;

    //@Inject
    //Optional<MMOCore> maybeMMOCore;

    @SneakyThrows
    @Override
    public void onEnable() {
        if(!this.checkIfBungee()){ return; }

        saveDefaultConfig();

        ProfilesPaperGuiModule module = new ProfilesPaperGuiModule(this);
        Injector injector = module.createInjector();
        injector.injectMembers(this);

        playerProfilesDAO.applyMigrations();

        getCommand("profiles").setExecutor(profilesCommand2);
        getCommand("profilesadmin").setExecutor(profilesAdminCommand);
        getServer().getPluginManager().registerEvents(profilesCommand2, this);
        getServer().getPluginManager().registerEvents(profilesPaperGuiEventListener, this);

        profilesCommand2.onEnable();

        /*
        if(maybeMMOCore.isPresent()) {
            System.out.println("Detected MMOCore -- registering required hooks to integrate with ProfilesPaperGui");
        } else {
            System.out.println("Did not detect MMOCore");
        }
         */

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
