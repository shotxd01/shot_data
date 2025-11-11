package com.shotdevs.shotdata.api;

import com.google.gson.Gson;
import com.shotdevs.shotdata.ShotDataPlugin;
import com.shotdevs.shotdata.util.PrivacyUtils;
import com.shotdevs.shotdata.util.TPSProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Handles communication with the ShotDevs API.
 */
public class ShotDevsClient {

    private final ShotDataPlugin plugin;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private long lastServerSend = 0;

    public ShotDevsClient(ShotDataPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(plugin.getConfig().getLong("shotdevs-plugin.api.timeout", 5000)))
                .build();
    }

    public void sendPlayerQuitData(Player player, Map<String, Number> stats) {
        if (!plugin.getConfig().getBoolean("shotdevs-plugin.data-collection.player-data.enabled", true)) {
            return;
        }

        Map<String, Object> playerData = buildPlayerData(player, false, stats);
        String jsonPayload = gson.toJson(playerData);

        sendRequestWithRetries("/player_data", jsonPayload);
    }

    public void sendServerData() {
        if (!plugin.getConfig().getBoolean("shotdevs-plugin.data-collection.server-data.enabled", true)) {
            return;
        }

        long now = System.currentTimeMillis();
        long interval = plugin.getConfig().getLong("shotdevs-plugin.data-collection.server-data.update-interval", 60) * 1000;
        if (now - lastServerSend < interval) {
            return; // Rate limit
        }
        lastServerSend = now;

        Map<String, Object> serverData = buildServerData();
        String jsonPayload = gson.toJson(serverData);

        sendRequestWithRetries("/server_data", jsonPayload);

        // Retry pending requests
        plugin.getFileStorage().getPendingFiles().forEach(file -> {
            String pendingJson = plugin.getFileStorage().readPendingFile(file);
            if(pendingJson != null) {
                String endpoint = file.getName().contains("player") ? "/player_data" : "/server_data";
                sendRequestWithRetries(endpoint, pendingJson).thenAccept(success -> {
                    if (success) {
                        plugin.getFileStorage().deletePendingFile(file);
                    }
                });
            }
        });
    }

    private CompletableFuture<Boolean> sendRequestWithRetries(String endpoint, String jsonPayload) {
        final String apiKey = plugin.getConfig().getString("shotdevs-plugin.api.api-key");
        final String baseUrl = plugin.getConfig().getString("shotdevs-plugin.api.base-url");
        final int maxRetries = plugin.getConfig().getInt("shotdevs-plugin.api.max-retries", 4);
        final long backoffBaseMs = plugin.getConfig().getLong("shotdevs-plugin.api.backoff-base-ms", 500);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        sendRequestAttempt(endpoint, jsonPayload, apiKey, baseUrl, 0, maxRetries, backoffBaseMs, future);
        return future;
    }

    private void sendRequestAttempt(String endpoint, String jsonPayload, String apiKey, String baseUrl, int attempt, int maxRetries, long backoffBaseMs, CompletableFuture<Boolean> future) {
        if (attempt > maxRetries) {
            plugin.getLogger().warning("Request failed after " + maxRetries + " retries. Caching payload.");
            plugin.getFileStorage().savePendingRequest(jsonPayload, endpoint.substring(1));
            future.complete(false);
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenCompleteAsync((response, throwable) -> {
                    if (throwable != null || response.statusCode() >= 300) {
                        plugin.getLogger().warning("Request to " + endpoint + " failed. Attempt " + (attempt + 1) + "/" + maxRetries);
                        long delay = backoffBaseMs * (1L << attempt); // Exponential backoff
                        executor.schedule(() -> sendRequestAttempt(endpoint, jsonPayload, apiKey, baseUrl, attempt + 1, maxRetries, backoffBaseMs, future), delay, TimeUnit.MILLISECONDS);
                    } else {
                        if (plugin.getConfig().getBoolean("shotdevs-plugin.logging.verbose", true)) {
                            plugin.getLogger().info("Successfully sent data to " + endpoint);
                        }
                        future.complete(true);
                    }
                }, executor);
    }

    private Map<String, Object> buildPlayerData(Player player, boolean isOnline, Map<String, Number> fileStats) {
        Map<String, Object> data = new HashMap<>();
        boolean anonymize = plugin.getConfig().getBoolean("shotdevs-plugin.privacy.anonymize-player-names");
        boolean collectIp = plugin.getConfig().getBoolean("shotdevs-plugin.privacy.collect-ip-addresses");

        data.put("uuid", player.getUniqueId().toString());
        data.put("name", anonymize ? PrivacyUtils.anonymizeName(player.getName()) : player.getName());
        data.put("displayName", anonymize ? PrivacyUtils.anonymizeName(player.getDisplayName()) : player.getDisplayName());
        data.put("playerListName", anonymize ? PrivacyUtils.anonymizeName(player.getPlayerListName()) : player.getPlayerListName());
        if (collectIp) {
            data.put("address", player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : null);
        }
        data.put("isOnline", isOnline);
        data.put("firstPlayed", player.getFirstPlayed());
        data.put("lastPlayed", isOnline ? player.getLastPlayed() : System.currentTimeMillis());
        data.put("hasPlayedBefore", player.hasPlayedBefore());

        data.put("health", player.getHealth());
        data.put("foodLevel", player.getFoodLevel());
        data.put("level", player.getLevel());
        data.put("exp", player.getExp());
        data.put("totalExperience", player.getTotalExperience());
        data.put("gameMode", player.getGameMode().name());

        Map<String, Object> location = new HashMap<>();
        location.put("world", player.getWorld().getName());
        location.put("x", player.getLocation().getX());
        location.put("y", player.getLocation().getY());
        location.put("z", player.getLocation().getZ());
        location.put("yaw", player.getLocation().getYaw());
        location.put("pitch", player.getLocation().getPitch());
        data.put("location", location);

        data.put("statistics", fileStats);

        return data;
    }

    private Map<String, Object> buildServerData() {
        Server server = Bukkit.getServer();
        Map<String, Object> data = new HashMap<>();

        // TPS
        double[] tps = TPSProvider.getTPS();
        Map<String, Double> tpsData = new HashMap<>();
        tpsData.put("oneMinute", tps[0]);
        tpsData.put("fiveMinutes", tps[1]);
        tpsData.put("fifteenMinutes", tps[2]);
        data.put("tps", tpsData);

        data.put("averageTickTime", TPSProvider.getAverageTickTime());

        // Memory
        Runtime runtime = Runtime.getRuntime();
        Map<String, Long> memoryData = new HashMap<>();
        memoryData.put("total", runtime.totalMemory() / (1024 * 1024));
        memoryData.put("used", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        memoryData.put("free", runtime.freeMemory() / (1024 * 1024));
        data.put("memory", memoryData);

        // Uptime
        data.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());

        // CPU Load
        try {
            com.sun.management.OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                    com.sun.management.OperatingSystemMXBean.class);
            data.put("cpuLoad", osBean.getProcessCpuLoad());
        } catch (Throwable t) {
            data.put("cpuLoad", -1); // Not available
        }

        // Players
        Map<String, Integer> playersData = new HashMap<>();
        playersData.put("online", server.getOnlinePlayers().size());
        playersData.put("max", server.getMaxPlayers());
        playersData.put("totalRegistered", server.getOfflinePlayers().length);
        data.put("players", playersData);

        return data;
    }
}
