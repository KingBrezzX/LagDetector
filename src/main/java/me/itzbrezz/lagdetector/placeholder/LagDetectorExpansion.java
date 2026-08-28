package me.itzbrezz.lagdetector.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.LagSnapshot;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LagDetectorExpansion extends PlaceholderExpansion {

    private final LagDetector plugin;

    public LagDetectorExpansion(LagDetector plugin) {
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
        return plugin.getDescription().getVersion();
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
        String key = params.toLowerCase();

        switch (key) {
            case "enabled":
                return String.valueOf(plugin.isDetectorEnabled());

            case "scanning":
                return String.valueOf(
                        plugin.getScanManager().isScanning()
                );

            case "last_tps":
                return String.format(
                        "%.2f",
                        plugin.getLagDetectionManager().getLastTps()
                );

            case "last_mspt":
                return String.format(
                        "%.2f",
                        plugin.getLagDetectionManager().getLastMspt()
                );

            case "active_detections":
                return String.valueOf(
                        plugin.getLagDetectionManager()
                                .getActiveDetectionCount()
                );

            case "total_detections":
                return String.valueOf(
                        plugin.getLagDetectionManager()
                                .getTotalDetectionCount()
                );

            case "redstone_locations":
                return String.valueOf(
                        plugin.getRedstoneDetector()
                                .getTrackedLocations()
                );

            case "redstone_suspects":
                return String.valueOf(
                        plugin.getRedstoneDetector()
                                .getSuspectedMachines()
                );

            case "last_lag_type":
            case "last_world":
            case "last_x":
            case "last_y":
            case "last_z":
            case "last_player": {
                LagSnapshot snapshot =
                        plugin.getLagDetectionManager()
                                .getLatestSnapshot();

                if (snapshot == null) {
                    return "None";
                }

                return switch (key) {
                    case "last_lag_type" -> snapshot.getLagType();
                    case "last_world" -> snapshot.getWorld();
                    case "last_x" -> String.valueOf(snapshot.getX());
                    case "last_y" -> String.valueOf(snapshot.getY());
                    case "last_z" -> String.valueOf(snapshot.getZ());
                    case "last_player" ->
                            snapshot.hasPlayer()
                                    ? snapshot.getPlayer()
                                    : "None";
                    default -> null;
                };
            }

            default:
                return null;
        }
    }
}

