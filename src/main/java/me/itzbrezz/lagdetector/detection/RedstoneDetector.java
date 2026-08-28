package me.itzbrezz.lagdetector.detection;

import me.itzbrezz.lagdetector.LagDetector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RedstoneDetector implements Listener {

    private final LagDetector plugin;
    private final LagDetectionManager detectionManager;

    private final Map<String, Counter> counters =
            new ConcurrentHashMap<>();

    private final Map<String, Long> lastDetections =
            new ConcurrentHashMap<>();

    private BukkitTask analysisTask;

    private volatile boolean enabled = true;

    public RedstoneDetector(
            LagDetector plugin,
            LagDetectionManager detectionManager
    ) {
        this.plugin = plugin;
        this.detectionManager = detectionManager;

        this.enabled = plugin.getConfig().getBoolean(
                "detector.redstone.enabled",
                true
        );
    }

    /**
     * Starts the redstone activity monitor.
     */
    public void start() {

        if (analysisTask != null) {
            return;
        }

        Bukkit.getPluginManager()
                .registerEvents(this, plugin);

        analysisTask = Bukkit.getScheduler()
                .runTaskTimer(
                        plugin,
                        this::analyseCounters,
                        20L,
                        20L
                );

        plugin.getLogger().info(
                "Redstone detector started."
        );
    }

    /**
     * Stops the detector.
     */
    public void stop() {

        if (analysisTask != null) {
            analysisTask.cancel();
            analysisTask = null;
        }

        counters.clear();
        lastDetections.clear();

        enabled = false;
    }

    /**
     * Reload detector settings.
     */
    public void reload() {

        enabled = plugin.getConfig()
                .getBoolean(
                        "detector.redstone.enabled",
                        true
                );

        if (enabled && analysisTask == null) {
            start();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (!enabled) {
            counters.clear();
        }
    }

    /*
     * ============================================================
     * REDSTONE EVENTS
     * ============================================================
     */

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onRedstone(BlockRedstoneEvent event) {

        if (!enabled) {
            return;
        }

        Block block = event.getBlock();

        if (!isRelevantBlock(block)) {
            return;
        }

        recordActivity(
                block,
                "REDSTONE"
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onPistonExtend(
            BlockPistonExtendEvent event
    ) {

        if (!enabled) {
            return;
        }

        Block piston = event.getBlock();

        recordActivity(
                piston,
                "PISTON"
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onPistonRetract(
            BlockPistonRetractEvent event
    ) {

        if (!enabled) {
            return;
        }

        Block piston = event.getBlock();

        recordActivity(
                piston,
                "PISTON"
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onPhysics(BlockPhysicsEvent event) {

        if (!enabled) {
            return;
        }

        Block block = event.getBlock();

        if (!isRelevantBlock(block)) {
            return;
        }

        recordActivity(
                block,
                "REDSTONE PHYSICS"
        );
    }

    /*
     * ============================================================
     * ACTIVITY
     * ============================================================
     */

    private void recordActivity(
            Block block,
            String type
    ) {

        if (block == null
                || block.getWorld() == null) {
            return;
        }

        Location location =
                block.getLocation();

        String key = createLocationKey(
                location
        );

        counters.compute(
                key,
                (ignored, existing) -> {

                    if (existing == null) {
                        existing = new Counter(
                                location.clone(),
                                type
                        );
                    }

                    existing.increment();

                    return existing;
                }
        );
    }

    /**
     * Analyses activity once per second.
     *
     * No blocks are modified here.
     */
    private void analyseCounters() {

        if (!enabled) {
            return;
        }

        if (!plugin.isDetectorEnabled()) {
            return;
        }

        int threshold =
                Math.max(
                        1,
                        plugin.getConfig().getInt(
                                "detector.redstone.threshold-per-second",
                                500
                        )
                );

        int requiredChecks =
                Math.max(
                        1,
                        plugin.getConfig().getInt(
                                "detector.redstone.required-checks",
                                3
                        )
                );

        for (Counter counter :
                counters.values()) {

            int activity =
                    counter.getAndReset();

            if (activity <= 0) {
                counter.resetSuspicion();
                continue;
            }

            if (activity >= threshold) {

                counter.incrementSuspicion();

            } else {

                counter.resetSuspicion();
                continue;
            }

            if (counter.getSuspicion()
                    < requiredChecks) {
                continue;
            }

            createDetection(
                    counter,
                    activity
            );
        }

        /*
         * Remove counters belonging to chunks that are no
         * longer loaded.
         *
         * This prevents the detector from keeping unnecessary
         * locations forever.
         */
        cleanupUnloadedLocations();
    }

    /**
     * Creates a lag detection after the configured number
     * of consecutive high-activity checks.
     */
    private void createDetection(
            Counter counter,
            int activity
    ) {

        Location location =
                counter.getLocation();

        if (location == null
                || location.getWorld() == null) {
            return;
        }

        World world =
                location.getWorld();

        /*
         * Never act on an unloaded chunk.
         */
        if (!world.isChunkLoaded(
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4
        )) {
            return;
        }

        String key =
                createLocationKey(location);

        long now =
                System.currentTimeMillis();

        /*
         * Prevent the same machine from producing
         * an alert every single second.
         */
        long cooldown =
                30_000L;

        Long previous =
                lastDetections.get(key);

        if (previous != null
                && now - previous < cooldown) {
            return;
        }

        lastDetections.put(
                key,
                now
        );

        String player =
                findNearestPlayer(location);

        double tps =
                detectionManager.getLastTps();

        double mspt =
                detectionManager.getLastMspt();

        LagSnapshot snapshot =
                new LagSnapshot(
                        "REDSTONE",
                        world.getName(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        player,
                        now,
                        tps,
                        mspt
                );

        detectionManager.registerDetection(
                snapshot
        );

        plugin.getLogger().warning(
                "REDSTONE LAG SUSPECTED | "
                        + "World="
                        + world.getName()
                        + " | Location="
                        + location.getBlockX()
                        + " "
                        + location.getBlockY()
                        + " "
                        + location.getBlockZ()
                        + " | Activity="
                        + activity
                        + "/s"
                        + " | Player="
                        + player
        );

        /*
         * IMPORTANT:
         *
         * MONITOR ONLY.
         *
         * No redstone block is disabled.
         * No piston is stopped.
         * No block is broken.
         * No chunk is loaded.
         * No player is banned.
         *
         * Actions are handled separately by the staff GUI.
         */
    }

    /**
     * Attempts to find the nearest online player.
     */
    private String findNearestPlayer(
            Location location
    ) {

        Player nearest = null;

        double nearestDistance =
                Double.MAX_VALUE;

        for (Player player :
                Bukkit.getOnlinePlayers()) {

            if (!player.getWorld()
                    .equals(location.getWorld())) {
                continue;
            }

            double distance =
                    player.getLocation()
                            .distanceSquared(location);

            if (distance < nearestDistance) {

                nearestDistance = distance;
                nearest = player;
            }
        }

        return nearest == null
                ? null
                : nearest.getName();
    }

    /**
     * Checks whether a block is relevant to redstone
     * monitoring.
     */
    private boolean isRelevantBlock(
            Block block
    ) {

        if (block == null) {
            return false;
        }

        Material material =
                block.getType();

        return switch (material) {

            case REDSTONE_WIRE,
                 REDSTONE_TORCH,
                 REDSTONE_WALL_TORCH,
                 REPEATER,
                 COMPARATOR,
                 PISTON,
                 STICKY_PISTON,
                 OBSERVER,
                 TARGET,
                 DISPENSER,
                 DROPPER,
                 HOPPER,
                 REDSTONE_BLOCK -> true;

            default -> false;
        };
    }

    /**
     * Removes activity records whose chunks are no longer loaded.
     */
    private void cleanupUnloadedLocations() {

        counters.entrySet().removeIf(entry -> {

            Counter counter =
                    entry.getValue();

            Location location =
                    counter.getLocation();

            if (location == null
                    || location.getWorld() == null) {
                return true;
            }

            World world =
                    location.getWorld();

            return !world.isChunkLoaded(
                    location.getBlockX() >> 4,
                    location.getBlockZ() >> 4
            );
        });
    }

    private String createLocationKey(
            Location location
    ) {

        return location.getWorld()
                .getUID()
                .toString()
                + ":"
                + location.getBlockX()
                + ":"
                + location.getBlockY()
                + ":"
                + location.getBlockZ();
    }

    public int getTrackedLocations() {
        return counters.size();
    }

    public int getSuspectedMachines() {

        int result = 0;

        for (Counter counter :
                counters.values()) {

            if (counter.getSuspicion() > 0) {
                result++;
            }
        }

        return result;
    }

    /*
     * ============================================================
     * COUNTER
     * ============================================================
     */

    private static final class Counter {

        private final Location location;
        private final String type;

        private int activity;
        private int suspicion;

        private Counter(
                Location location,
                String type
        ) {
            this.location = location;
            this.type = type;
        }

        private synchronized void increment() {
            activity++;
        }

        private synchronized int getAndReset() {

            int result = activity;

            activity = 0;

            return result;
        }

        private synchronized void incrementSuspicion() {
            suspicion++;
        }

        private synchronized void resetSuspicion() {
            suspicion = 0;
        }

        private synchronized int getSuspicion() {
            return suspicion;
        }

        private Location getLocation() {
            return location;
        }

        @SuppressWarnings("unused")
        private String getType() {
            return type;
        }
    }
          }
