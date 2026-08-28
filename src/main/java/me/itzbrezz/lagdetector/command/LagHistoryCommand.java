package me.itzbrezz.lagdetector.command;

import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.history.HistoryManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class LagHistoryCommand implements CommandExecutor {

    private final LagDetector plugin;
    private final HistoryManager historyManager;

    public LagHistoryCommand(
            LagDetector plugin,
            HistoryManager historyManager
    ) {
        this.plugin = plugin;
        this.historyManager = historyManager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!sender.hasPermission(
                "lagdetector.history"
        )
                && !sender.hasPermission(
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

        int page = 1;

        if (args.length >= 1) {

            try {

                page = Integer.parseInt(args[0]);

            } catch (NumberFormatException exception) {

                sender.sendMessage(
                        plugin.color(
                                plugin.getConfig().getString(
                                        "command-messages.invalid-page",
                                        "&cInvalid page number."
                                )
                        )
                );

                return true;
            }
        }

        if (page < 1) {
            page = 1;
        }

        historyManager.sendHistory(
                sender,
                page
        );

        return true;
    }
}
