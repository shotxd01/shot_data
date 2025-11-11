package com.shotdevs.shotdata.listeners;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shotdevs.shotdata.ShotDataPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Listener for player quit events.
 */
public class PlayerListener implements Listener {

    private final ShotDataPlugin plugin;
    private final Gson gson = new Gson();

    public PlayerListener(ShotDataPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            File statsFile = new File(player.getWorld().getWorldFolder(), "stats" + File.separator + player.getUniqueId() + ".json");
            if (!statsFile.exists()) {
                plugin.getLogger().warning("Could not find stats file for player " + player.getName() + ". Sending data without file-based stats.");
                plugin.getShotDevsClient().sendPlayerQuitData(player, Collections.emptyMap());
                return;
            }

            try (FileReader reader = new FileReader(statsFile)) {
                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                Map<String, Object> root = gson.fromJson(reader, type);
                Map<String, Object> stats = (Map<String, Object>) root.get("stats");
                Map<String, Number> flattenedStats = new HashMap<>();
                if (stats != null) {
                    for (Map.Entry<String, Object> entry : stats.entrySet()) {
                        Map<String, Number> categoryStats = (Map<String, Number>) entry.getValue();
                        for (Map.Entry<String, Number> statEntry : categoryStats.entrySet()) {
                            flattenedStats.put(statEntry.getKey().replace("minecraft:", ""), statEntry.getValue());
                        }
                    }
                }
                plugin.getShotDevsClient().sendPlayerQuitData(player, flattenedStats);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to read or parse stats file for player " + player.getName() + ". Sending data without file-based stats.", e);
                plugin.getShotDevsClient().sendPlayerQuitData(player, Collections.emptyMap());
            }
        });
    }
}
