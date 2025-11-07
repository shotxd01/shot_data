package com.shotdevs.shotdata.util;

import org.bukkit.Bukkit;

/**
 * Provides a way to get the server's TPS, with a fallback for non-Paper servers.
 */
public class TPSProvider {

    /**
     * Gets the server's TPS.
     *
     * @return An array of doubles containing the TPS for the last 1, 5, and 15 minutes.
     */
    public static double[] getTPS() {
        try {
            // Paper API
            return Bukkit.getServer().getTPS();
        } catch (Throwable t) {
            // Fallback for non-Paper servers
            return new double[]{-1, -1, -1};
        }
    }

    /**
     * Gets the server's average tick time.
     * @return The average tick time.
     */
    public static double getAverageTickTime() {
        try {
            // Paper API
            return Bukkit.getServer().getAverageTickTime();
        } catch (Throwable t) {
            // Fallback for non-Paper servers
            return -1;
        }
    }
}
