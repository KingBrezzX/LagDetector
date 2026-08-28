package me.itzbrezz.lagdetector.command;

import me.itzbrezz.lagdetector.LagDetector;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LagScanCommand implements CommandExecutor {

    private final LagDetector plugin;

    public LagScanCommand(LagDetector plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!sender.hasPermission(
                "lagdetector.scan"
        ) && !sender.hasPermission(
                "lagdetector.admin"
        )) {

            sender.sendMessage(
                    plugin.color(
                            plugin.getConfig().getString(
                                    "messages.no-permission",
                                    "&cYou don't have permission."
                            )
                    )
            );

            return true;
        }

        if (!(sender instanceof Player player)) {

            sender.sendMessage(
                    plugin.color(
                            "&cThis command must be executed by a player."
                    )
            );

            return true;
        }

        if (args.length > 0
                && args[0].equalsIgnoreCase("stop")) {

            if (!plugin.getScanManager()
                    .isScanning()) {

                player.sendMessage(
                        plugin.color(
                                "&eThere is no active scan."
                        )
                );

                return true;
            }

            plugin.getScanManager()
                    .cancelScan();

            player.sendMessage(
                    plugin.color(
                            plugin.getConfig().getString(
                                    "messages.scan-cancelled",
                                    "&cLag scan cancelled."
                            )
                    )
            );

            return true;
        }

        if (plugin.getScanManager()
                .isScanning()) {

            player.sendMessage(
                    plugin.color(
                            plugin.getConfig().getString(
                                    "messages.scan-already-running",
                                    "&eA scan is already running."
                            )
                    )
            );

            return true;
        }

        plugin.getScanManager()
                .startScan(player);

        return true;
    }
                      }
