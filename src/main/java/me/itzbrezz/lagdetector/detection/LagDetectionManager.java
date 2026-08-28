￼package me.itzbrezz.lagdetector.detection;

import me.itzbrezz.lagdetector.LagDetector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LagDetectionManager {

    private final LagDetector plugin;

    private final Map<String, LagSnapshot> activeDetections =
            new ConcurrentHashMap<>();

    private BukkitTask detectionTask;

    private volatile boolean enabled;
    private volatile boolean running;

    private long totalDetectionCount;

    private double lastTps = 20.0D;
    private double lastMspt = 0.0D;

    private long lastReportTime = 0L;

    public LagDetectionManager(LagDetector plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean(
                "detector.enabled",
                true
        );
    }

    /**
     * Starts the lightweight performance monitor.
     *
     * This monitor does NOT scan every chunk.
     * It only observes server performance and the
     * redstone activity counters maintained by the scanner/listener.
     */
    public void start() {

        if (running) {
            return;
        }

        running = true;

        long interval = 20L;

        detectionTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::checkPerformance,
                interval,
                interval
        );

        plugin.getLogger().info(
                "Lag detection monitor started."
        );
    }

    /**
     * Stops the monitor.
     */
    public void stop() {

        running = false;

        if (detectionTask != null) {
            detectionTask.cancel();
            detectionTask = null;
        }

        plugin.getLogger().info(
                "Lag detection monitor stopped."
        );
    }

    /**
     * Reloads detector configuration.
     */
    public void reload() {

        enabled = plugin.getConfig().getBoolean(
                "detector.enabled",
                true
        );

        if (enabled && !running) {
            start();
        }
    }

    /**
     * Enables/disables detection.
     */
    public void setEnabled(boolean enabled) {

        this.enabled = enabled;

        if (enabled) {

            if (!running) {
                start();
            }

        } else {

            activeDetections.clear();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Lightweight server performance check.
     */
    private void checkPerformance() {

        if (!enabled) {
            return;
        }

        double tps = getServerTps();
        double mspt = getServerMspt();

        lastTps = tps;
        lastMspt = mspt;

        double tpsThreshold = plugin.getConfig().getDouble(
                "detection.performance.tps-threshold",
                18.0D
        );

        double msptThreshold = plugin.getConfig().getDouble(
                "detection.performance.mspt-threshold",
                50.0D
        );

        boolean tpsLagging = tps < tpsThreshold;
        boolean msptLagging = mspt > msptThreshold;

        if (tpsLagging || msptLagging) {

            createPerformanceSnapshot(
                    tpsLagging,
                    msptLagging
            );
        }

        sendPeriodicReportIfNeeded();
    }

    /**
     * Creates a performance snapshot.
     *
     * It does not automatically destroy blocks,
     * kick players, ban players, or modify redstone.
     */
    private void createPerformanceSnapshot(
            boolean tpsLagging,
            boolean msptLagging
    ) {

        String type;

        if (tpsLagging && msptLagging) {
            type = "TPS + MSPT";
        } else if (tpsLagging) {
            type = "LOW TPS";
        } else {
            type = "HIGH MSPT";
        }

        String key = "SERVER_PERFORMANCE";

        LagSnapshot snapshot = new LagSnapshot(
                type,
                "Server",
                0,
                0,
                0,
                null,
                System.currentTimeMillis(),
                lastTps,
                lastMspt
        );

        activeDetections.put(key, snapshot);
        totalDetectionCount++;

        if (plugin.getConfig().getBoolean(
                "alert.console",
                true
        )) {

            plugin.getLogger().warning(
                    "Lag detected: "
                            + type
                            + " | TPS="
                            + String.format("%.2f", lastTps)
                            + " | MSPT="
                            + String.format("%.2f", lastMspt)
            );
        }
    }

    /**
     * Registers a detected lag location.
     *
     * Used later by ScanManager/RedstoneDetector.
     */
    public void registerDetection(LagSnapshot snapshot) {

        if (!enabled || snapshot == null) {
            return;
        }

        String key = createKey(snapshot);

        activeDetections.put(
                key,
                snapshot
        );

        totalDetectionCount++;

        sendLagAlert(snapshot);
    }

    /**
     * Removes a detection after it has been handled.
     */
    public void removeDetection(LagSnapshot snapshot) {

        if (snapshot == null) {
            return;
        }

        activeDetections.remove(
                createKey(snapshot)
        );
    }

    /**
     * Sends a staff alert.
     */
    private void sendLagAlert(LagSnapshot snapshot) {

        if (!plugin.getConfig().getBoolean(
                "alert.enabled",
                true
        )) {
            return;
        }

        String permission = plugin.getConfig().getString(
                "alert.permission",
                "lagdetector.alert"
        );

        for (org.bukkit.entity.Player player
                : Bukkit.getOnlinePlayers()) {

            if (!player.hasPermission(permission)
                    && !player.hasPermission(
                    "lagdetector.admin"
            )) {
                continue;
            }

            player.sendMessage(
                    buildAlert(snapshot)
            );
        }
    }

    /**
     * Builds the configurable lag alert.
     */
    private String buildAlert(LagSnapshot snapshot) {

        java.util.List<String> lines =
                plugin.getConfig().getStringList(
                        "messages.lag-alert"
                );

        if (lines.isEmpty()) {
            return ChatColor.RED
                    + "Lag detected at "
                    + snapshot.getWorld()
                    + " "
                    + snapshot.getX()
                    + " "
                    + snapshot.getY()
                    + " "
                    + snapshot.getZ();
        }

        String playerName =
                snapshot.getPlayer() == null
                        ? "Unknown"
                        : snapshot.getPlayer();

        String lastOnline =
                playerName.equals("Unknown")
                        ? "Unknown"
                        : getLastOnline(playerName);

        String playTime =
                playerName.equals("Unknown")
                        ? "Unknown"
                        : getPlayTime(playerName);

        String result = String.join(
                "\n",
                lines
        );

        result = result.replace(
                "%lag_type%",
                snapshot.getLagType()
        );

        result = result.replace(
                "%world%",
                snapshot.getWorld()
        );

        result = result.replace(
                "%x%",
                String.valueOf(snapshot.getX())
        );

        result = result.replace(
                "%y%",
                String.valueOf(snapshot.getY())
        );

        result = result.replace(
                "%z%",
                String.valueOf(snapshot.getZ())
        );

        result = result.replace(
                "%player%",
                playerName
        );

        result = result.replace(
                "%last_online%",
                lastOnline
        );

        result = result.replace(
                "%play_time%",
                playTime
        );

        return ChatColor.translateAlternateColorCodes(
                '&',
                result
        );
    }

    private String getLastOnline(String playerName) {

        org.bukkit.OfflinePlayer player =
                Bukkit.getOfflinePlayer(playerName);

        if (player.isOnline()) {
            return "Online";
        }

        long lastPlayed = player.getLastPlayed();

        if (lastPlayed <= 0L) {
            return "Unknown";
        }

        long difference =
                System.currentTimeMillis()
                        - lastPlayed;

        return formatDuration(difference)
                + " ago";
    }

    private String getPlayTime(String playerName) {

        org.bukkit.OfflinePlayer player =
                Bukkit.getOfflinePlayer(playerName);

        /*
         * OfflinePlayer does not expose reliable total playtime
         * through the Bukkit API on every Paper version.
         *
         * Therefore the plugin uses a safe fallback until
         * the player activity tracker is added.
         */
        if (player.isOnline()) {

            return "Online";
        }

        return "Unknown";
    }

    private String formatDuration(long milliseconds) {

        long seconds =
                milliseconds / 1000L;

        long minutes =
                seconds / 60L;

        long hours =
                minutes / 60L;

        if (hours > 0L) {
            return hours + "h "
                    + (minutes % 60L)
                    + "m";
        }

        if (minutes > 0L) {
            return minutes + "m";
        }

        return seconds + "s";
    }

    /**
     * Sends the configurable console/stack report.
     */
    private void sendPeriodicReportIfNeeded() {

        if (!plugin.getConfig().getBoolean(
                "reports.enabled",
                true
        )) {
            return;
        }

        long intervalSeconds =
                plugin.getConfig().getLong(
                        "reports.interval-seconds",
                        60L
                );

        if (intervalSeconds <= 0L) {
            return;
        }

        long now =
                System.currentTimeMillis();

        if (now - lastReportTime
                < intervalSeconds * 1000L) {
            return;
        }

        lastReportTime = now;

        if (plugin.getConfig().getBoolean(
                "reports.console",
                true
        )) {

            plugin.getLogger().warning(
                    "Periodic lag report | "
                            + "TPS="
                            + String.format(
                            "%.2f",
                            lastTps
                    )
                            + " | MSPT="
                            + String.format(
                            "%.2f",
                            lastMspt
                    )
                            + " | Active detections="
                            + activeDetections.size()
            );
        }

        if (plugin.getConfig().getBoolean(
                "reports.stacktrace",
                true
        )) {

            printStackTrace();
        }
    }

    /**
     * Prints the current server stack when configured.
     *
     * This is diagnostic only.
     */
    private void printStackTrace() {

        plugin.getLogger().warning(
                "========== LAG DETECTOR STACK =========="
        );

        for (StackTraceElement element
                : Thread.currentThread().getStackTrace()) {

            plugin.getLogger().warning(
                    "    at " + element
            );
        }

        plugin.getLogger().warning(
                "========================================="
        );
    }

    /**
     * Gets TPS safely.
     *
     * Paper exposes TPS through Bukkit#getTPS().
     */
    private double getServerTps() {

        double[] tps = Bukkit.getTPS();

        if (tps.length == 0) {
            return 20.0D;
        }

        return Math.min(
                20.0D,
                Math.max(
                        0.0D,
                        tps[0]
                )
        );
    }

    /**
     * Gets MSPT using the Paper server tick timing API
     * where available through Bukkit#getServerTickManager().
     */
    private double getServerMspt() {

        try {
            return Bukkit.getServerTickManager()
                    .getTickAverageDuration();
        } catch (Throwable ignored) {
            /*
             * Safe fallback.
             */
            return 1000.0D / Math.max(
                    0.1D,
                    getServerTps()
            );
        }
    }

    private String createKey(LagSnapshot snapshot) {

        return snapshot.getWorld()
                + ":"
                + snapshot.getX()
                + ":"
                + snapshot.getY()
                + ":"
                + snapshot.getZ();
    }

    public int getActiveDetectionCount() {
        return activeDetections.size();
    }

    public long getTotalDetectionCount() {
        return totalDetectionCount;
    }

    public Map<String, LagSnapshot> getActiveDetections() {
        return Map.copyOf(activeDetections);
    }

    public double getLastTps() {
        return lastTps;
    }

    public double getLastMspt() {
        return lastMspt;
    }
      }
