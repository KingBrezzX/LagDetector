package me.itzbrezz.lagdetector.notification;

import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.LagSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public final class ConsoleNotificationManager {

    private final LagDetector plugin;

    private BukkitTask task;
    private LagSnapshot latestSnapshot;

    private long lastNotification = 0L;

    public ConsoleNotificationManager(LagDetector plugin) {
        this.plugin = plugin;
    }

    public void start() {

        stop();

        if (!plugin.getConfig().getBoolean(
                "notifications.console.enabled",
                true
        )) {
            return;
        }

        long intervalSeconds =
                Math.max(
                        10L,
                        plugin.getConfig().getLong(
                                "notifications.console.interval-seconds",
                                60L
                        )
                );

        long intervalTicks =
                intervalSeconds * 20L;

        task =
                Bukkit.getScheduler().runTaskTimer(
                        plugin,
                        this::sendNotification,
                        intervalTicks,
                        intervalTicks
                );
    }

    public void stop() {

        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void update(
            LagSnapshot snapshot
    ) {

        if (snapshot == null) {
            return;
        }

        latestSnapshot = snapshot;
    }

    private void sendNotification() {

        if (latestSnapshot == null) {
            return;
        }

        if (!plugin.getConfig().getBoolean(
                "notifications.console.enabled",
                true
        )) {
            return;
        }

        long now =
                System.currentTimeMillis();

        long interval =
                Math.max(
                        10L,
                        plugin.getConfig().getLong(
                                "notifications.console.interval-seconds",
                                60L
                        )
                ) * 1000L;

        /*
         * Safety check:
         * never send the same notification more than
         * once during the configured interval.
         */
        if (now - lastNotification < interval) {
            return;
        }

        lastNotification = now;

        LagSnapshot snapshot =
                latestSnapshot;

        String header =
                plugin.getConfig().getString(
                        "notifications.console.header",
                        "========== LAG DETECTOR =========="
                );

        String message =
                plugin.getConfig().getString(
                        "notifications.console.message",
                        "Lag detected | Type=%lag-type% | World=%world% | XYZ=%x% %y% %z% | Player=%player%"
                );

        header =
                replace(
                        header,
                        snapshot
                );

        message =
                replace(
                        message,
                        snapshot
                );

        plugin.getLogger().warning(header);
        plugin.getLogger().warning(message);

        if (plugin.getConfig().getBoolean(
                "notifications.console.stack-trace",
                true
        )) {

            sendStack(snapshot);
        }

        plugin.getLogger().warning(
                "================================="
        );
    }

    private void sendStack(
            LagSnapshot snapshot
    ) {

        plugin.getLogger().warning(
                "LagDetector stack information:"
        );

        StackTraceElement[] stack =
                Thread.currentThread()
                        .getStackTrace();

        int max =
                Math.max(
                        1,
                        plugin.getConfig().getInt(
                                "notifications.console.stack-lines",
                                8
                        )
                );

        int sent = 0;

        for (StackTraceElement element :
                stack) {

            if (sent >= max) {
                break;
            }

            plugin.getLogger().warning(
                    "    at "
                            + element
            );

            sent++;
        }

        plugin.getLogger().warning(
                "Detection location: "
                        + snapshot.getWorld()
                        + " "
                        + snapshot.getCoordinates()
        );
    }

    private String replace(
            String text,
            LagSnapshot snapshot
    ) {

        if (text == null) {
            return "";
        }

        String player =
                snapshot.hasPlayer()
                        ? snapshot.getPlayer()
                        : "Unknown";

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

    public LagSnapshot getLatestSnapshot() {
        return latestSnapshot;
    }
          }
