package com.shotdevs.shotdata.listeners;

import com.shotdevs.shotdata.ShotDataPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener for player join and quit events.
 */
public class PlayerListener implements Listener {

    private final ShotDataPlugin plugin;
    private final Map<UUID, Long> debounceMap = new ConcurrentHashMap<>();

    public PlayerListener(ShotDataPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        long debouncerMs = plugin.getConfig().getLong("shotdevs-plugin.data-collection.player-data.debouncer-ms", 2000);

        debounceMap.compute(player.getUniqueId(), (uuid, lastSent) -> {
            long now = System.currentTimeMillis();
            if (lastSent == null || (now - lastSent) > debouncerMs) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    plugin.getShotDevsClient().sendPlayerData(player);
                });
                return now;
            }
            return lastSent;
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            // Update the player's data one last time before sending
            plugin.getShotDevsClient().sendPlayerQuitData(player);
        });
    }
}
