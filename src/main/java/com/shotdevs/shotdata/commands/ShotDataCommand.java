package com.shotdevs.shotdata.commands;

import com.shotdevs.shotdata.ShotDataPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the /shotdata command.
 */
public class ShotDataCommand implements CommandExecutor {

    private final ShotDataPlugin plugin;

    public ShotDataCommand(ShotDataPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "ShotData Plugin - Usage: /shotdata <status|sendnow|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "status":
                sendStatus(sender);
                break;
            case "sendnow":
                if (!sender.hasPermission("shotdata.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                sendNow(sender);
                break;
            case "reload":
                if (!sender.hasPermission("shotdata.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                reloadConfig(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /shotdata <status|sendnow|reload>");
                break;
        }
        return true;
    }

    private void sendStatus(CommandSender sender) {
        // This is a simplified status. A more detailed status could be implemented.
        sender.sendMessage(ChatColor.GREEN + "ShotData Plugin Status:");
        sender.sendMessage(ChatColor.GRAY + " - Server data sending enabled: " + plugin.getConfig().getBoolean("shotdevs-plugin.data-collection.server-data.enabled"));
        sender.sendMessage(ChatColor.GRAY + " - Player data sending enabled: " + plugin.getConfig().getBoolean("shotdevs-plugin.data-collection.player-data.enabled"));
        sender.sendMessage(ChatColor.GRAY + " - Pending files: " + plugin.getFileStorage().getPendingFiles().size());
    }

    private void sendNow(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Manually triggering server data send...");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getShotDevsClient().sendServerData();
            sender.sendMessage(ChatColor.GREEN + "Server data sent.");
        });
    }

    private void reloadConfig(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Reloading ShotData configuration...");
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
    }
}
