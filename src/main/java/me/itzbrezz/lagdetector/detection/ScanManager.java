package me.itzbrezz.lagdetector.detection;

import me.itzbrezz.lagdetector.LagDetector;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ScanManager {

    private final LagDetector plugin;

    private BukkitTask scanTask;

    private volatile boolean scanning;
    private volatile boolean enabled;

    private final Deque<ChunkCoordinate> queue = new ArrayDeque<>();
    private final Set<ChunkCoordinate> queued = new HashSet<>();
    private final Set<ChunkCoordinate> processed = new HashSet<>();

    private Player scanStarter;
    private World scanWorld;

    private int totalChunks;
    private int processedChunks;

    /*
     * NOTE: not populated by ScanManager itself yet â€” this
     * manager only walks already-loaded chunks, it doesn't
     * run redstone/lag-source analysis. Wire this up (e.g.
     * incrementDetectedBlocks()) once RedstoneDetector reports
     * findings back here, otherwise this will always read 0.
     */
    private int detectedBlocks;

    private int lastProgress = -1;

    public ScanManager(LagDetector plugin) {
        this.plugin = plugin;

        this.enabled = plugin.getConfig().getBoolean(
                "scan.enabled",
                true
        );
    }

    /**
     * Starts the scan manager.
     *
     * This manager itself does not scan continuously.
     * It only prepares manual scans started through /lag scan.
     */
    public void start() {
        enabled = plugin.getConfig().getBoolean(
                "scan.enabled",
                true
        );

        plugin.getLogger().info(
                "Scan manager initialized."
        );
    }

    /**
     * Stops an active scan safely.
     */
    public void stop() {

        cancelScan();

        plugin.getLogger().info(
                "Scan manager stopped."
        );
    }

    /**
     * Reload configuration.
     */
    public void reload() {

        enabled = plugin.getConfig().getBoolean(
                "scan.enabled",
                true
        );

        if (!enabled && scanning) {
            cancelScan();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isScanning() {
        return scanning;
    }

    /**
     * Starts a scan around the player's current location.
     *
     * IMPORTANT:
     * Only chunks that are already loaded are considered.
     *
     * This method NEVER calls:
     *
     * world.getChunkAt(...).load()
     *
     * and NEVER calls:
     *
     * world.loadChunk(...)
     */
    public boolean startScan(Player player) {

        if (!enabled) {
            player.sendMessage(color(
                    plugin.getConfig().getString(
                            "command-messages.scan-disabled",
                            "&cScanning is disabled."
                    )
            ));

            return false;
        }

        if (scanning) {
            player.sendMessage(color(
                    plugin.getConfig().getString(
                            "command-messages.scan-running",
                            "&eA scan is already running."
                    )
            ));

            return false;
        }

        if (player == null || !player.isOnline()) {
            return false;
        }

        World world = player.getWorld();

        if (!isWorldAllowed(world)) {

            player.sendMessage(color(
                    plugin.getConfig().getString(
                            "command-messages.world-disabled",
                            "&cThis world is disabled for scanning."
                    )
            ));

            return false;
        }

        Location center = player.getLocation();

        int radius = Math.max(
                0,
                plugin.getConfig().getInt(
                        "scan.radius",
                        128
                )
        );

        int centerChunkX =
                center.getBlockX() >> 4;

        int centerChunkZ =
                center.getBlockZ() >> 4;

        clearState();

        scanStarter = player;
        scanWorld = world;

        /*
         * Radius is specified in blocks.
         *
         * Convert it to chunk radius.
         */
        int chunkRadius = Math.max(
                0,
                (radius + 15) >> 4
        );

        /*
         * We DO NOT load chunks here.
         *
         * We inspect only chunks which are already loaded.
         */
        for (int chunkX =
                     centerChunkX - chunkRadius;
             chunkX <= centerChunkX + chunkRadius;
             chunkX++) {

            for (int chunkZ =
                         centerChunkZ - chunkRadius;
                 chunkZ <= centerChunkZ + chunkRadius;
                 chunkZ++) {

                /*
                 * This is intentionally a loaded-state check.
                 *
                 * No chunk generation is triggered.
                 */
                if (!world.isChunkLoaded(
                        chunkX,
                        chunkZ
                )) {
                    continue;
                }

                ChunkCoordinate coordinate =
                        new ChunkCoordinate(
                                world.getName(),
                                chunkX,
                                chunkZ
                        );

                if (queued.add(coordinate)) {
                    queue.add(coordinate);
                }
            }
        }

        totalChunks = queue.size();

        if (totalChunks == 0) {

            player.sendMessage(color(
                    plugin.getConfig().getString(
                            "command-messages.no-loaded-chunks",
                            "&eNo loaded chunks were found."
                    )
            ));

            clearState();

            return false;
        }

        scanning = true;
        processedChunks = 0;
        detectedBlocks = 0;
        lastProgress = -1;

        sendStartMessage();

        scheduleScanTask();

        return true;
    }

    /**
     * Creates the asynchronous-looking incremental scan loop.
     *
     * Bukkit world/block access is deliberately performed
     * on the main server thread.
     *
     * Work is split across ticks so one huge loop cannot
     * freeze the server.
     */
    private void scheduleScanTask() {

        int chunksPerBatch = Math.max(
                1,
                plugin.getConfig().getInt(
                        "scan.chunks-per-batch",
                        2
                )
        );

        long delay = Math.max(
                1L,
                plugin.getConfig().getLong(
                        "scan.delay-ticks",
                        2L
                )
        );

        scanTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> processBatch(chunksPerBatch),
                1L,
                delay
        );
    }

    /**
     * Processes only a small number of already-loaded chunks.
     */
    private void processBatch(int chunksPerBatch) {

        if (!scanning) {
            cancelScanTask();
            return;
        }

        if (scanWorld == null) {
            finishScan();
            return;
        }

        int processedThisBatch = 0;

        while (
                processedThisBatch < chunksPerBatch
                        && !queue.isEmpty()
        ) {

            ChunkCoordinate coordinate =
                    queue.poll();

            if (coordinate == null) {
                break;
            }

            queued.remove(coordinate);

            /*
             * Safety check:
             *
             * A chunk could have become unloaded while
             * waiting in our queue.
             *
             * If it is no longer loaded, skip it.
             */
            if (!scanWorld.isChunkLoaded(
                    coordinate.x,
                    coordinate.z
            )) {

                processedChunks++;
                processedThisBatch++;

                updateProgress();

                continue;
            }

            /*
             * Get the already-loaded chunk.
             *
             * This does NOT force-load it because the
             * isChunkLoaded check above succeeded.
             */
            Chunk chunk = scanWorld.getChunkAt(
                    coordinate.x,
                    coordinate.z
            );

            scanLoadedChunk(chunk);

            processed.add(coordinate);

            processedChunks++;
            processedThisBatch++;

            updateProgress();
        }

        if (queue.isEmpty()) {
            finishScan();
        }
    }

    /**
     * Performs lightweight inspection of one loaded chunk.
     *
     * This implementation intentionally does not iterate
     * every block in the chunk.
     *
     * Heavy block scanning will be handled by RedstoneDetector
     * with strict per-tick budgets.
     */
    private void scanLoadedChunk(Chunk chunk) {

        if (chunk == null) {
            return;
        }

        /*
         * At this stage we only collect lightweight information.
         *
         * The actual redstone analysis is delegated later to
         * RedstoneDetector.
         */
        int blockEntities =
                chunk.getTileEntities().length;

        /*
         * Keep the variable intentionally local.
         *
         * This prevents unnecessary storage for every chunk.
         */
        if (blockEntities < 0) {
            return;
        }
    }

    /**
     * Updates visible scan progress.
     */
    private void updateProgress() {

        if (!plugin.getConfig().getBoolean(
                "scan.progress.enabled",
                true
        )) {
            return;
        }

        if (totalChunks <= 0) {
            return;
        }

        int percent =
                (int) Math.floor(
                        (processedChunks * 100.0D)
                                / totalChunks
                );

        percent = Math.min(
                100,
                Math.max(
                        0,
                        percent
                )
        );

        int step = Math.max(
                1,
                plugin.getConfig().getInt(
                        "scan.progress.percent-step",
                        10
                )
        );

        int progressBucket =
                (percent / step) * step;

        if (progressBucket == lastProgress) {
            return;
        }

        /*
         * Do not report 100% here.
         * finishScan() handles the final message.
         */
        if (progressBucket >= 100) {
            return;
        }

        lastProgress = progressBucket;

        String message =
                "&e[LagDetector] Scan progress: &f"
                        + percent
                        + "% &7("
                        + processedChunks
                        + "/"
                        + totalChunks
                        + ")";

        if (plugin.getConfig().getBoolean(
                "scan.progress.console",
                true
        )) {

            plugin.getLogger().info(
                    ChatColorStrip(message)
            );
        }

        if (plugin.getConfig().getBoolean(
                "scan.progress.player",
                true
        )
                && scanStarter != null
                && scanStarter.isOnline()) {

            scanStarter.sendMessage(
                    color(message)
            );
        }
    }

    /**
     * Finishes the scan and cleans all temporary state.
     */
    private void finishScan() {

        if (!scanning) {
            return;
        }

        scanning = false;

        cancelScanTask();

        int scanned = processedChunks;
        int total = totalChunks;

        if (plugin.getConfig().getBoolean(
                "scan.progress.console",
                true
        )) {

            plugin.getLogger().info(
                    "Lag scan completed: "
                            + scanned
                            + "/"
                            + total
                            + " loaded chunks processed."
            );
        }

        if (plugin.getConfig().getBoolean(
                "scan.progress.player",
                true
        )
                && scanStarter != null
                && scanStarter.isOnline()) {

            scanStarter.sendMessage(color(
                    plugin.getConfig().getString(
                            "command-messages.scan-completed",
                            "&aLag scan completed."
                    )
            ));

            scanStarter.sendMessage(color(
                    "&7Loaded chunks processed: &f"
                            + scanned
                            + "&7/&f"
                            + total
            ));
        }

        clearState();
    }

    /**
     * Cancels an active scan.
     */
    public void cancelScan() {

        scanning = false;

        cancelScanTask();

        clearState();
    }

    private void cancelScanTask() {

        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
    }

    private void clearState() {

        queue.clear();
        queued.clear();
        processed.clear();

        scanStarter = null;
        scanWorld = null;

        totalChunks = 0;
        processedChunks = 0;
        detectedBlocks = 0;

        lastProgress = -1;
    }

    /**
     * Checks world whitelist and blacklist.
     */
    private boolean isWorldAllowed(World world) {

        if (world == null) {
            return false;
        }

        String worldName = world.getName();

        boolean blacklistEnabled =
                plugin.getConfig().getBoolean(
                        "worlds.blacklist-enabled",
                        true
                );

        if (blacklistEnabled) {

            List<String> blacklist =
                    plugin.getConfig().getStringList(
                            "worlds.blacklist"
                    );

            for (String blocked : blacklist) {

                if (blocked.equalsIgnoreCase(
                        worldName
                )) {
                    return false;
                }
            }
        }

        boolean whitelistEnabled =
                plugin.getConfig().getBoolean(
                        "worlds.whitelist-enabled",
                        true
                );

        if (!whitelistEnabled) {
            return true;
        }

        List<String> whitelist =
                plugin.getConfig().getStringList(
                        "worlds.whitelist"
                );

        for (String allowed : whitelist) {

            if (allowed.equalsIgnoreCase(
                    worldName
            )) {
                return true;
            }
        }

        return false;
    }

    private void sendStartMessage() {

        if (scanStarter != null
                && scanStarter.isOnline()) {

            scanStarter.sendMessage(color(
                    plugin.getConfig().getString(
                            "command-messages.scan-started",
                            "&aLag scan started."
                    )
            ));

            scanStarter.sendMessage(color(
                    "&7World: &f"
                            + scanWorld.getName()
                            + " &7| Loaded chunks: &f"
                            + totalChunks
            ));

            scanStarter.sendMessage(color(
                    "&7Unloaded chunks are ignored."
            ));
        }

        if (plugin.getConfig().getBoolean(
                "scan.progress.console",
                true
        )) {

            plugin.getLogger().info(
                    "Lag scan started | World="
                            + scanWorld.getName()
                            + " | Loaded chunks="
                            + totalChunks
                            + " | Radius="
                            + plugin.getConfig().getInt(
                            "scan.radius",
                            128
                    )
            );
        }
    }

    private String color(String text) {

        if (text == null) {
            return "";
        }

        return org.bukkit.ChatColor
                .translateAlternateColorCodes(
                        '&',
                        text
                );
    }

    private String ChatColorStrip(String text) {

        return org.bukkit.ChatColor
                .stripColor(
                        color(text)
                );
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public int getProcessedChunks() {
        return processedChunks;
    }

    public int getProgressPercent() {

        if (totalChunks <= 0) {
            return 0;
        }

        return (int) Math.floor(
                (processedChunks * 100.0D)
                        / totalChunks
        );
    }

    /*
     * ------------------------------------------------------
     * Aliases below exist because ProgressDisplay.java calls
     * these specific method names. Kept as thin wrappers over
     * the original fields/getters above so nothing else in
     * this class had to change.
     * ------------------------------------------------------
     */

    /**
     * Alias for {@link #getProgressPercent()}.
     */
    public int getProgress() {
        return getProgressPercent();
    }

    /**
     * Alias for {@link #getProcessedChunks()}.
     */
    public int getScannedChunks() {
        return getProcessedChunks();
    }

    /**
     * Number of lag-causing blocks detected during the current
     * (or most recently completed) scan.
     *
     * NOTE: ScanManager does not currently run redstone/lag
     * analysis itself (see the detectedBlocks field comment),
     * so this always returns 0 until that wiring exists.
     */
    public int getDetectedBlocks() {
        return detectedBlocks;
    }

    /**
     * Call this once ScanManager (or whatever it delegates to)
     * actually finds a lag-causing block, so getDetectedBlocks()
     * reflects real data.
     */
    public void incrementDetectedBlocks() {
        detectedBlocks++;
    }

    /**
     * Immutable chunk coordinate.
     */
    private static final class ChunkCoordinate {

        private final String world;
        private final int x;
        private final int z;

        private ChunkCoordinate(
                String world,
                int x,
                int z
        ) {
            this.world = world;
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object object) {

            if (this == object) {
                return true;
            }

            if (!(object instanceof ChunkCoordinate other)) {
                return false;
            }

            return x == other.x
                    && z == other.z
                    && world.equals(other.world);
        }

        @Override
        public int hashCode() {

            int result = world.hashCode();

            result = 31 * result + x;
            result = 31 * result + z;

            return result;
        }
    }
            }
