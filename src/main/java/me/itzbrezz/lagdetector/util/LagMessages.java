package me.itzbrezz.lagdetector.util;

import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.LagSnapshot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class LagMessages {

    private final LagDetector plugin;

    public LagMessages(LagDetector plugin) {
        this.plugin = plugin;
    }

    public void send(
            CommandSender sender,
            String path,
            String fallback
    ) {

        String message =
                plugin.getConfig().getString(
                        path,
                        fallback
                );

        sender.sendMessage(
                color(message)
        );
    }

    public void sendSnapshot(
            CommandSender sender,
            String path,
            String fallback,
            LagSnapshot snapshot
    ) {

        String message =
                plugin.getConfig().getString(
                        path,
                        fallback
                );

        sender.sendMessage(
                color(
                        replace(
                                message,
                                snapshot
                        )
                )
        );
    }

    public String replace(
            String text,
            LagSnapshot snapshot
    ) {

        if (text == null) {
            return "";
        }

        if (snapshot == null) {
            return color(text);
        }

        String player =
                snapshot.hasPlayer()
                        ? snapshot.getPlayer()
                        : "Unknown";

        return text
                .replace(
                        "%lag-type%",
                        safe(snapshot.getLagType())
                )
                .replace(
                        "%world%",
                        safe(snapshot.getWorld())
                )
                .replace(
                        "%x%",
                        String.valueOf(
                                snapshot.getX()
                        )
                )
                .replace(
                        "%y%",
                        String.valueOf(
                                snapshot.getY()
                        )
                )
                .replace(
                        "%z%",
                        String.valueOf(
                                snapshot.getZ()
                        )
                )
                .replace(
                        "%player%",
                        player
                )
                .replace(
                        "%tps%",
                        String.format(
                                "%.2f",
                                snapshot.getTps()
                        )
                )
                .replace(
                        "%mspt%",
                        String.format(
                                "%.2f",
                                snapshot.getMspt()
                        )
                );
    }

    public String color(
            String text
    ) {

        if (text == null) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes(
                '&',
                text
        );
    }

    private String safe(
            String value
    ) {

        return value == null
                ? "Unknown"
                : value;
    }
          }
