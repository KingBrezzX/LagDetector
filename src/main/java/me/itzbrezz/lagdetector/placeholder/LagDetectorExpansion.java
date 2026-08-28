package me.itzbrezz.lagdetector.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.LagSnapshot;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LagDetectorExpansion
        extends PlaceholderExpansion {

    private final LagDetector plugin;

    public LagDetectorExpansion(
            LagDetector plugin
    ) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "lagdetector";
    }

    @Override
    public @NotNull String getAuthor() {
        return "itzbrezz";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription()
                .getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(
            Player player,
            @NotNull String params
    ) {

        switch (params.toLowerCase()) {

            case "enabled":
                return String.valueOf(
                        plugin.isDetectorEnabled()
                );

            case "scanning":
                return String.valueOf(
                        plugin.getScanManager()
                                .isScanning()
                );

            case "scan_progress":
                return String.format(
                        "%.1f",
                        plugin.getScanManager()
                                .getProgress()
                );

            case "scanned_chunks":
                return String.valueOf(
                        plugin.getScanManager()
                                .getScannedChunks()
                );

            case "total_chunks":
                return String.valueOf(
                        plugin.getScanManager()
                                .getTotalChunks()
                );

            case "detections":
                return String.valueOf(
                        plugin.getScanManager()
                                .getDetectedBlocks()
                );

            case "tracked_players":
                return String.valueOf(
                        plugin.getPlayerTracker()
                                .getTrackedPlayers()
                );

            case "last_lag_type": {

                LagSnapshot snapshot =
                        plugin.getLagDetectionManager()
                                .getLatestSnapshot();

                return snapshot == null
                        ? "None"
                        : snapshot.getLagType();
            }

            case "last_world": {

                LagSnapshot snapshot =
                        plugin.getLagDetectionManager()
                                .getLatestSnapshot();

                return snapshot == null
                        ? "None"
                        : snapshot.getWorld();
            }

            case "last_x": {

                LagSnapshot snapshot =
                        plugin.getLagDetectionManager()
                                .getLatestSnapshot();

                return snapshot == null
                        ? "0"
                        : String.valueOf(
                                snapshot.getX()
                        );
            }

            case "last_y": {

                LagSnapshot snapshot =
                        plugin.getLagDetectionManager()
                                .getLatestSnapshot();

                return snapshot == null
                        ? "0"
                        : String.valueOf(
                                snapshot.getY()
                        );
            }

            case "last_z": {

                LagSnapshot snapshot =
                        plugin.getLagDetectionManager()
                                .getLatestSnapshot();

                return snapshot == null
                        ? "0"
                        : String.valueOf(
                                snapshot.getZ()
                        );
            }

            case "last_player": {

                LagSnapshot snapshot =
                        plugin.getLagDetectionManager()
                                .getLatestSnapshot();

                if (snapshot == null
                        || !snapshot.hasPlayer()) {
                    return "None";
                }

                return snapshot.getPlayer();
            }

            case "last_tps": {

                LagSnapshot snapshot =
                        plugin.getLagDetectionManager()
                                .getLatestSnapshot();

                return snapshot == null
                        ? "0.00"
                        : String.format(
                                "%.2f",
                                snapshot.getTps()
                        );
            }

            case "last_mspt": {

                LagSnapshot snapshot =
                        plugin.getLagDetectionManager()
                                .getLatestSnapshot();

                return snapshot == null
                        ? "0.00"
                        : String.format(
                                "%.2f",
                                snapshot.getMspt()
                        );
            }

            default:
                return null;
        }
    }
    }
