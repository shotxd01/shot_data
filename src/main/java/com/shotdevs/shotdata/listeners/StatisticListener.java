package com.shotdevs.shotdata.listeners;

import com.shotdevs.shotdata.util.StatisticManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;

public class StatisticListener implements Listener {

    private final StatisticManager statisticManager;

    public StatisticListener(StatisticManager statisticManager) {
        this.statisticManager = statisticManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        statisticManager.incrementStatistic(event.getPlayer().getUniqueId(), "blocksPlaced", 1);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        statisticManager.incrementStatistic(event.getPlayer().getUniqueId(), "blocksBroken", 1);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            statisticManager.incrementStatistic(player.getUniqueId(), "itemsCrafted", event.getRecipe().getResult().getAmount());
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            statisticManager.incrementStatistic(player.getUniqueId(), "damageDealt", event.getDamage());
        }
    }
}
