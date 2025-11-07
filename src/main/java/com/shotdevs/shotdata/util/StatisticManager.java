package com.shotdevs.shotdata.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatisticManager {

    private final Map<UUID, Map<String, Double>> playerStatistics = new ConcurrentHashMap<>();

    public void incrementStatistic(UUID playerUuid, String statistic, double amount) {
        playerStatistics.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>()).merge(statistic, amount, Double::sum);
    }

    public Map<String, Double> getStatistics(UUID playerUuid) {
        return playerStatistics.getOrDefault(playerUuid, new ConcurrentHashMap<>());
    }

    public void clearStatistics(UUID playerUuid) {
        playerStatistics.remove(playerUuid);
    }
}
