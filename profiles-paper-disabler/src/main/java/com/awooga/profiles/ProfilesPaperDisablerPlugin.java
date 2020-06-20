package com.awooga.profiles;

import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProfilesPaperDisablerPlugin extends JavaPlugin {

    @Inject
    ProfilesPaperDisablerEventListener profilesPaperDisablerEventListener;

    @SneakyThrows
    @Override
    public void onEnable() {
        if(!this.checkIfBungee()){ return; }

        saveDefaultConfig();

        ProfilesPaperDisablerModule module = new ProfilesPaperDisablerModule(this);
        Injector injector = module.createInjector();
        injector.injectMembers(this);

        getServer().getPluginManager().registerEvents(profilesPaperDisablerEventListener, this);
    }

    @SneakyThrows
    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
