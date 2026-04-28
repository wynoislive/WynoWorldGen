package me.wyno.wynogen;

import me.wyno.wynogen.commands.FWCommand;
import me.wyno.wynogen.listeners.PlayerListener;
import me.wyno.wynogen.managers.DatabaseManager;
import me.wyno.wynogen.managers.LanguageManager;
import me.wyno.wynogen.managers.DataManager;
import me.wyno.wynogen.managers.UpdateManager;
import me.wyno.wynogen.managers.WorldManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class WynoGen extends JavaPlugin {

    private WorldManager worldManager;
    private DataManager dataManager;
    private DatabaseManager databaseManager;
    private LanguageManager languageManager;
    private UpdateManager updateManager;

    @Override
    public void onEnable() {
        // 1. Ensure config and messages exist
        saveDefaultConfig();
        this.languageManager = new LanguageManager(this);
        
        // 2. Initialize Managers
        this.databaseManager = new DatabaseManager(this);
        this.worldManager = new WorldManager(this);
        this.dataManager = new DataManager(this);
        this.updateManager = new UpdateManager(this);

        // 2.5 Check for updates asynchronously
        if (getConfig().getBoolean("options.updater.auto_check", true)) {
            updateManager.checkForUpdates().thenAccept(available -> {
                if (available) {
                    getLogger().info("A new version is available: v" + updateManager.getLatestVersion());
                } else {
                    getLogger().info("You are running the latest version.");
                }
            });
        }

        // 3. Register Metrics (bStats)
        if (getConfig().getBoolean("options.metrics", true)) {
            new Metrics(this, 21820); // Placeholder ID or real one if assigned
            getLogger().info("bStats metrics enabled.");
        }

        // 3. Load tracked worlds
        this.worldManager.loadAllWorlds();

        // 4. Register Commands
        FWCommand fwCommand = new FWCommand(this);
        getCommand("featuredworld").setExecutor(fwCommand);
        getCommand("featuredworld").setTabCompleter(fwCommand);

        // 5. Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // 6. Start Auto-Save Task
        long interval = getConfig().getLong("options.save_interval_ticks", 6000L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            getLogger().info("Running scheduled auto-save for all online players...");
            for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                String worldName = player.getWorld().getName();
                // isManagedWorld covers both parent Featured Worlds AND companion Nether/End worlds
                String id = getWorldManager().isManagedWorld(worldName) ? worldName : "default";
                getDataManager().savePlayerData(player, id);
            }
        }, interval, interval);

        getLogger().info("WYNO GEN has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("WYNO GEN has been disabled!");
    }

    public void reloadPlugin() {
        reloadConfig();
        languageManager.reload();
        
        // Database reload (Close and Re-Open if type changed)
        if (databaseManager != null) {
            databaseManager.close();
        }
        this.databaseManager = new DatabaseManager(this);
        
        getLogger().info("Plugin configuration and database connection reloaded.");
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public UpdateManager getUpdateManager() {
        return updateManager;
    }
}
