package me.itzbrezz.lagdetector.history;

import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.LagSnapshot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class HistoryManager {

    private final LagDetector plugin;

    private final List<HistoryEntry> history =
            new ArrayList<>();

    private File historyFile;
    private YamlConfiguration historyConfig;

    public HistoryManager(LagDetector plugin) {
        this.plugin = plugin;

        setupFile();
        load();
    }

    /**
     * Creates history.yml.
     */
    private void setupFile() {

        historyFile = new File(
                plugin.getDataFolder(),
                plugin.getConfig().getString(
                        "history.file",
                        "history.yml"
                )
        );

        File parent = historyFile.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        if (!historyFile.exists()) {

            try {
                if (!historyFile.createNewFile()) {
                    plugin.getLogger().warning(
                            "Could not create history file."
                    );
                }
            } catch (IOException exception) {

                plugin.getLogger().severe(
                        "Failed to create history.yml: "
                                + exception.getMessage()
                );
            }
        }

        historyConfig =
                YamlConfiguration.loadConfiguration(
                        historyFile
                );
    }

    /**
     * Loads all history entries from disk.
     */
    public synchronized void load() {

        history.clear();

        if (historyConfig == null) {
            historyConfig =
                    YamlConfiguration.loadConfiguration(
                            historyFile
                    );
        }

        int size =
                historyConfig.getInt(
                        "entries.size",
                        0
                );

        for (int i = 0; i < size; i++) {

            String path =
                    "entries." + i;

            String id =
                    historyConfig.getString(
                            path + ".id",
                            UUID.randomUUID().toString()
                    );

            String lagType =
                    historyConfig.getString(
                            path + ".lag-type",
                            "UNKNOWN"
                    );

            String world =
                    historyConfig.getString(
                            path + ".world",
                            "UNKNOWN"
                    );

            int x =
                    historyConfig.getInt(
                            path + ".x"
                    );

            int y =
                    historyConfig.getInt(
                            path + ".y"
                    );

            int z =
                    historyConfig.getInt(
                            path + ".z"
                    );

            String player =
                    historyConfig.getString(
                            path + ".player"
                    );

            long timestamp =
                    historyConfig.getLong(
                            path + ".timestamp",
                            System.currentTimeMillis()
                    );

            double tps =
                    historyConfig.getDouble(
                            path + ".tps",
                            20.0D
                    );

            double mspt =
                    historyConfig.getDouble(
                            path + ".mspt",
                            0.0D
                    );

            history.add(
                    new HistoryEntry(
                            id,
                            lagType,
                            world,
                            x,
                            y,
                            z,
                            player,
                            timestamp,
                            tps,
                            mspt
                    )
            );
        }

        plugin.getLogger().info(
                "Loaded "
                        + history.size()
                        + " lag history entries."
        );
    }

    /**
     * Adds a new lag event.
     */
    public synchronized void add(LagSnapshot snapshot) {

        if (snapshot == null) {
            return;
        }

        String id =
                UUID.randomUUID().toString();

        HistoryEntry entry =
                new HistoryEntry(
                        id,
                        snapshot.getLagType(),
                        snapshot.getWorld(),
                        snapshot.getX(),
                        snapshot.getY(),
                        snapshot.getZ(),
                        snapshot.getPlayer(),
                        snapshot.getTimestamp(),
                        snapshot.getTps(),
                        snapshot.getMspt()
                );

        /*
         * Newest event goes first.
         */
        history.add(
                0,
                entry
        );

        trim();

        if (plugin.getConfig().getBoolean(
                "history.persistent",
                true
        )) {
            save();
        }
    }

    /**
     * Keeps history within configured size.
     */
    private void trim() {

        int maximum =
                Math.max(
                        1,
                        plugin.getConfig().getInt(
                                "history.max-entries",
                                500
                        )
                );

        while (history.size() > maximum) {
            history.remove(
                    history.size() - 1
            );
        }
    }

    /**
     * Saves history to history.yml.
     */
    public synchronized void save() {

        if (historyFile == null) {
            return;
        }

        if (historyConfig == null) {
            historyConfig =
                    new YamlConfiguration();
        }

        historyConfig.set(
                "entries.size",
                history.size()
        );

        /*
         * Clear old entries first.
         */
        for (int i = 0; i < history.size() + 100; i++) {

            String path =
                    "entries." + i;

            if (!historyConfig.contains(path)) {
                break;
            }

            historyConfig.set(
                    path,
                    null
            );
        }

        for (int i = 0; i < history.size(); i++) {

            HistoryEntry entry =
                    history.get(i);

            String path =
                    "entries." + i;

            historyConfig.set(
                    path + ".id",
                    entry.id
            );

            historyConfig.set(
                    path + ".lag-type",
                    entry.lagType
            );

            historyConfig.set(
                    path + ".world",
                    entry.world
            );

            historyConfig.set(
                    path + ".x",
                    entry.x
            );

            historyConfig.set(
                    path + ".y",
                    entry.y
            );

            historyConfig.set(
                    path + ".z",
                    entry.z
            );

            historyConfig.set(
                    path + ".player",
                    entry.player
            );

            historyConfig.set(
                    path + ".timestamp",
                    entry.timestamp
            );

            historyConfig.set(
                    path + ".tps",
                    entry.tps
            );

            historyConfig.set(
                    path + ".mspt",
                    entry.mspt
            );
        }

        try {

            historyConfig.save(
                    historyFile
            );

        } catch (IOException exception) {

            plugin.getLogger().severe(
                    "Failed to save history.yml: "
                            + exception.getMessage()
            );
        }
    }

    /**
     * Sends paginated history to a staff member.
     */
    public synchronized void sendHistory(
            CommandSender sender,
            int requestedPage
    ) {

        if (history.isEmpty()) {

            send(
                    sender,
                    "command-messages.history-empty"
            );

            return;
        }

        int perPage =
                Math.max(
                        1,
                        plugin.getConfig().getInt(
                                "history.entries-per-page",
                                5
                        )
                );

        int totalPages =
                (int) Math.ceil(
                        history.size()
                                / (double) perPage
                );

        int page =
                Math.max(
                        1,
                        Math.min(
                                requestedPage,
                                totalPages
                        )
                );

        int start =
                (page - 1)
                        * perPage;

        int end =
                Math.min(
                        start + perPage,
                        history.size()
                );

        sender.sendMessage(
                color("&8&m--------------------------------")
        );

        sender.sendMessage(
                color("&c&lLAG HISTORY &7Page "
                        + page
                        + "/"
                        + totalPages)
        );

        sender.sendMessage("");

        for (int i = start; i < end; i++) {

            HistoryEntry entry =
                    history.get(i);

            sendEntry(
                    sender,
                    entry
            );
        }

        sender.sendMessage(
                color("&8&m--------------------------------")
        );

        if (page > 1) {

            sender.sendMessage(
                    color("&7Previous: &f/lag history "
                            + (page - 1))
            );
        }

        if (page < totalPages) {

            sender.sendMessage(
                    color("&7Next: &f/lag history "
                            + (page + 1))
            );
        }
    }

    /**
     * Sends one history event using the configurable format.
     */
    private void sendEntry(
            CommandSender sender,
            HistoryEntry entry
    ) {

        List<String> lines =
                plugin.getConfig().getStringList(
                        "history-message.entry"
                );

        if (lines.isEmpty()) {

            lines = defaultHistoryLines();
        }

        String player =
                entry.player == null
                        ? "Unknown"
                        : entry.player;

        String lastOnline =
                getLastOnline(player);

        for (String line : lines) {

            String result =
                    line
                            .replace(
                                    "%id%",
                                    entry.id
                            )
                            .replace(
                                    "%lag_type%",
                                    entry.lagType
                            )
                            .replace(
                                    "%world%",
                                    entry.world
                            )
                            .replace(
                                    "%x%",
                                    String.valueOf(entry.x)
                            )
                            .replace(
                                    "%y%",
                                    String.valueOf(entry.y)
                            )
                            .replace(
                                    "%z%",
                                    String.valueOf(entry.z)
                            )
                            .replace(
                                    "%player%",
                                    player
                            )
                            .replace(
                                    "%last_online%",
                                    lastOnline
                            )
                            .replace(
                                    "%timestamp%",
                                    String.valueOf(
                                            entry.timestamp
                                    )
                            )
                            .replace(
                                    "%tps%",
                                    String.format(
                                            "%.2f",
                                            entry.tps
                                    )
                            )
                            .replace(
                                    "%mspt%",
                                    String.format(
                                            "%.2f",
                                            entry.mspt
                                    )
                            );

            sender.sendMessage(
                    color(result)
            );
        }

        sender.sendMessage("");
    }

    private List<String> defaultHistoryLines() {

        return List.of(
                "&8&m--------------------------------",
                "&c&lLAG SUS",
                "&7LOCATION: &f%x% %y% %z%",
                "&7WORLD: &f%world%",
                "&7LAG TYPE: &f%lag_type%",
                "&7PLAYER: &f%player%",
                "&7LAST ONLINE: &f%last_online%",
                "",
                "&eACTION",
                "&f[ &bTP &f] [ &cBAN &f] [ &cBREAK &f]",
                "&8&m--------------------------------"
        );
    }

    /**
     * Gets the last time the player was online.
     */
    private String getLastOnline(
            String playerName
    ) {

        if (playerName == null
                || playerName.equalsIgnoreCase(
                "Unknown"
        )) {
            return "Unknown";
        }

        org.bukkit.OfflinePlayer player =
                plugin.getServer()
                        .getOfflinePlayer(
                                playerName
                        );

        if (player.isOnline()) {
            return "Online";
        }

        long lastPlayed =
                player.getLastPlayed();

        if (lastPlayed <= 0L) {
            return "Unknown";
        }

        long elapsed =
                System.currentTimeMillis()
                        - lastPlayed;

        return formatDuration(elapsed)
                + " ago";
    }

    private String formatDuration(
            long milliseconds
    ) {

        long seconds =
                Math.max(
                        0L,
                        milliseconds / 1000L
                );

        long minutes =
                seconds / 60L;

        long hours =
                minutes / 60L;

        long days =
                hours / 24L;

        if (days > 0) {

            return days + "d "
                    + (hours % 24)
                    + "h";
        }

        if (hours > 0) {

            return hours + "h "
                    + (minutes % 60)
                    + "m";
        }

        if (minutes > 0) {

            return minutes + "m "
                    + (seconds % 60)
                    + "s";
        }

        return seconds + "s";
    }

    private void send(
            CommandSender sender,
            String path
    ) {

        String message =
                plugin.getConfig().getString(
                        path,
                        "&cMessage not configured."
                );

        sender.sendMessage(
                color(message)
        );
    }

    private String color(String text) {

        if (text == null) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes(
                '&',
                text
        );
    }

    public synchronized int size() {
        return history.size();
    }

    public synchronized List<HistoryEntry> getEntries() {

        return Collections.unmodifiableList(
                new ArrayList<>(history)
        );
    }

    public synchronized HistoryEntry get(int index) {

        if (index < 0
                || index >= history.size()) {
            return null;
        }

        return history.get(index);
    }

    /**
     * Stored history entry.
     */
    public static final class HistoryEntry {

        private final String id;
        private final String lagType;
        private final String world;

        private final int x;
        private final int y;
        private final int z;

        private final String player;

        private final long timestamp;

        private final double tps;
        private final double mspt;

        public HistoryEntry(
                String id,
                String lagType,
                String world,
                int x,
                int y,
                int z,
                String player,
                long timestamp,
                double tps,
                double mspt
        ) {

            this.id = id;
            this.lagType = lagType;
            this.world = world;

            this.x = x;
            this.y = y;
            this.z = z;

            this.player = player;

            this.timestamp = timestamp;

            this.tps = tps;
            this.mspt = mspt;
        }

        public String getId() {
            return id;
        }

        public String getLagType() {
            return lagType;
        }

        public String getWorld() {
            return world;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public String getPlayer() {
            return player;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public double getTps() {
            return tps;
        }

        public double getMspt() {
            return mspt;
        }

        public String getLocation() {

            return x
                    + " "
                    + y
                    + " "
                    + z;
        }
    }
  }
