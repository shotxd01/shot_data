package com.shotdevs.shotdata;

import com.shotdevs.shotdata.api.ShotDevsClient;
import com.shotdevs.shotdata.commands.ShotDataCommand;
import com.shotdevs.shotdata.listeners.PlayerListener;
import com.shotdevs.shotdata.listeners.StatisticListener;
import com.shotdevs.shotdata.util.FileStorage;
import com.shotdevs.shotdata.util.StatisticManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

/**
 * Main class for the ShotData plugin.
 * Handles plugin lifecycle, configuration, and scheduling.
 */
public class ShotDataPlugin extends JavaPlugin {

    private static ShotDataPlugin instance;
    private ShotDevsClient shotDevsClient;
    private FileStorage fileStorage;
    private StatisticManager statisticManager;
    private BukkitTask serverDataTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Initialize storage for pending requests
        String pendingFolderPath = getConfig().getString("shotdevs-plugin.storage.pending-folder", "plugins/shot_data/pending");
        this.fileStorage = new FileStorage(new File(pendingFolderPath));

        // Initialize StatisticManager
        this.statisticManager = new StatisticManager();

        // Initialize API client
        this.shotDevsClient = new ShotDevsClient(this);

        // Register listeners and commands
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new StatisticListener(this.statisticManager), this);
        getCommand("shotdata").setExecutor(new ShotDataCommand(this));

        // Schedule periodic server data task
        scheduleServerDataTask();

        getLogger().info("ShotData plugin enabled.");
    }

    @Override
    public void onDisable() {
        if (serverDataTask != null && !serverDataTask.isCancelled()) {
            serverDataTask.cancel();
        }
        getLogger().info("ShotData plugin disabled.");
    }

    /**
     * Reloads the plugin's configuration from disk.
     */
    public void reload() {
        reloadConfig();
        if (serverDataTask != null && !serverDataTask.isCancelled()) {
            serverDataTask.cancel();
        }
        scheduleServerDataTask();
    }

    private void scheduleServerDataTask() {
        if (!getConfig().getBoolean("shotdevs-plugin.data-collection.server-data.enabled", true)) {
            return;
        }

        long intervalSeconds = getConfig().getLong("shotdevs-plugin.data-collection.server-data.update-interval", 60);
        long intervalTicks = intervalSeconds * 20; // 20 ticks per second

        this.serverDataTask = getServer().getScheduler().runTaskTimerAsynchronously(this,
                () -> shotDevsClient.sendServerData(),
                intervalTicks, intervalTicks);
    }

    /**
     * Gets the singleton instance of the ShotDataPlugin.
     *
     * @return The ShotDataPlugin instance.
     */
    public static ShotDataPlugin getInstance() {
        return instance;
    }

    /**
     * Gets the ShotDevs API client.
     *
     * @return The ShotDevsClient instance.
     */
    public ShotDevsClient getShotDevsClient() {
        return shotDevsClient;
    }

    /**
     * Gets the file storage utility for pending requests.
     *
     * @return The FileStorage instance.
     */
    public FileStorage getFileStorage() {
        return fileStorage;
    }

    /**
     * Gets the statistic manager.
     *
     * @return The StatisticManager instance.
     */
    public StatisticManager getStatisticManager() {
        return statisticManager;
    }
}
