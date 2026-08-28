package me.itzbrezz.lagdetector.notification;

import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.LagSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class LagNotificationManager {

    private final LagDetector plugin;

    public LagNotificationManager(LagDetector plugin) {
        this.plugin = plugin;
    }

    public void notifyStaff(
            LagSnapshot snapshot
    ) {

        if (snapshot == null) {
            return;
        }

        if (!plugin.getConfig().getBoolean(
                "notifications.staff.enabled",
                true
        )) {
            return;
        }

        String message =
                plugin.getConfig().getString(
                        "notifications.staff.message",
                        "&c&lLAG DETECTOR\n"
                                + "&7LAG TYPE: &f%lag-type%\n"
                                + "&7WORLD: &f%world%\n"
                                + "&7COORDS: &f%x% %y% %z%\n"
                                + "&7PLAYER: &f%player%\n"
                                + "&7LAST ONLINE: &f%last-online%\n"
                                + "&7PLAY TIME: &f%play-time%"
                );

        message =
                replace(
                        message,
                        snapshot
                );

        for (Player player :
                Bukkit.getOnlinePlayers()) {

            if (!player.hasPermission(
                    "lagdetector.notify"
            )
                    && !player.hasPermission(
                    "lagdetector.admin"
            )) {
                continue;
            }

            for (String line :
                    message.split("\n")) {

                player.sendMessage(
                        plugin.color(line)
                );
            }
        }
    }

    private String replace(
            String text,
            LagSnapshot snapshot
    ) {

        String playerName =
                snapshot.hasPlayer()
                        ? snapshot.getPlayer()
                        : "Unknown";

        String lastOnline =
                plugin.getPlayerTracker()
                        .getLastOnline(
                                playerName
                        );

        String playTime =
                plugin.getPlayerTracker()
                        .getPlayTime(
                                playerName
                        );

        return text
                .replace(
                        "%lag-type%",
                        snapshot.getLagType()
                )
                .replace(
                        "%world%",
                        snapshot.getWorld()
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
                        playerName
                )
                .replace(
                        "%last-online%",
                        lastOnline
                )
                .replace(
                        "%play-time%",
                        playTime
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
          }
