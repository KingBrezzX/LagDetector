package me.itzbrezz.lagdetector.scan;

import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.ScanManager;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class ProgressDisplay {

    private final LagDetector plugin;

    public ProgressDisplay(LagDetector plugin) {
        this.plugin = plugin;
    }

    public void send(
            Player player,
            ScanManager scanManager
    ) {

        if (player == null || scanManager == null) {
            return;
        }

        double progress =
                scanManager.getProgress();

        String message =
                plugin.getConfig().getString(
                        "scan.progress-message",
                        "&eLag scan: &f%progress%% &7[%scanned%/%total% chunks]"
                );

        message =
                message
                        .replace(
                                "%progress%",
                                String.format(
                                        Locale.US,
                                        "%.1f",
                                        progress
                                )
                        )
                        .replace(
                                "%scanned%",
                                String.valueOf(
                                        scanManager.getScannedChunks()
                                )
                        )
                        .replace(
                                "%total%",
                                String.valueOf(
                                        scanManager.getTotalChunks()
                                )
                        )
                        .replace(
                                "%detections%",
                                String.valueOf(
                                        scanManager.getDetectedBlocks()
                                )
                        );

        player.sendMessage(
                plugin.color(message)
        );
    }

    public String createBar(
            ScanManager scanManager
    ) {

        int length =
                Math.max(
                        5,
                        plugin.getConfig().getInt(
                                "scan.progress-bar.length",
                                20
                        )
                );

        String completed =
                plugin.getConfig().getString(
                        "scan.progress-bar.completed",
                        "&a█"
                );

        String remaining =
                plugin.getConfig().getString(
                        "scan.progress-bar.remaining",
                        "&7█"
                );

        double progress =
                Math.max(
                        0.0D,
                        Math.min(
                                100.0D,
                                scanManager.getProgress()
                        )
                );

        int completedCount =
                (int) Math.round(
                        length
                                * progress
                                / 100.0D
                );

        StringBuilder bar =
                new StringBuilder();

        for (int i = 0; i < length; i++) {

            if (i < completedCount) {
                bar.append(completed);
            } else {
                bar.append(remaining);
            }
        }

        return plugin.color(
                bar.toString()
        );
    }

    public String format(
            ScanManager scanManager
    ) {

        if (scanManager == null) {
            return "0%";
        }

        return String.format(
                Locale.US,
                "%.1f%%",
                scanManager.getProgress()
        );
    }
                          }
