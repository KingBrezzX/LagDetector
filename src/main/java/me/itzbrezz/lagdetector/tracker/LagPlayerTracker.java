package me.itzbrezz.lagdetector.tracker;

import me.itzbrezz.lagdetector.LagDetector;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LagPlayerTracker {

    private final LagDetector plugin;

    private final Map<UUID, PlayerData> players =
            new ConcurrentHashMap<>();

    public LagPlayerTracker(LagDetector plugin) {
        this.plugin = plugin;
    }

    public void update(Player player) {

        if (player == null) {
            return;
        }

        players.compute(
                player.getUniqueId(),
                (uuid, old) -> {

                    if (old == null) {

                        return new PlayerData(
                                uuid,
                                player.getName(),
                                System.currentTimeMillis(),
                                System.currentTimeMillis(),
                                System.currentTimeMillis(),
                                0L
                        );
                    }

                    old.name = player.getName();
                    old.lastSeen =
                            System.currentTimeMillis();

                    return old;
                }
        );
    }

    public void recordActivity(Player player) {

        if (player == null) {
            return;
        }

        update(player);

        PlayerData data =
                players.get(
                        player.getUniqueId()
                );

        if (data != null) {
            data.lastActivity =
                    System.currentTimeMillis();
        }
    }

    public PlayerData get(Player player) {

        if (player == null) {
            return null;
        }

        return players.get(
                player.getUniqueId()
        );
    }

    public PlayerData get(UUID uuid) {

        if (uuid == null) {
            return null;
        }

        return players.get(uuid);
    }

    public String getLastOnline(
            String playerName
    ) {

        if (playerName == null
                || playerName.isBlank()) {
            return "Unknown";
        }

        Player online =
                plugin.getServer()
                        .getPlayerExact(
                                playerName
                        );

        if (online != null
                && online.isOnline()) {

            return "Online";
        }

        PlayerData data =
                players.values()
                        .stream()
                        .filter(entry ->
                                entry.name.equalsIgnoreCase(
                                        playerName
                                )
                        )
                        .findFirst()
                        .orElse(null);

        if (data == null) {
            return "Unknown";
        }

        return formatAgo(
                System.currentTimeMillis()
                        - data.lastSeen
        );
    }

    public String getPlayTime(
            String playerName
    ) {

        if (playerName == null
                || playerName.isBlank()) {
            return "Unknown";
        }

        PlayerData data =
                players.values()
                        .stream()
                        .filter(entry ->
                                entry.name.equalsIgnoreCase(
                                        playerName
                                )
                        )
                        .findFirst()
                        .orElse(null);

        if (data == null) {
            return "Unknown";
        }

        long total =
                data.totalPlayTime;

        Player online =
                plugin.getServer()
                        .getPlayerExact(
                                playerName
                        );

        if (online != null
                && online.isOnline()) {

            total +=
                    System.currentTimeMillis()
                            - data.sessionStart;
        }

        return formatDuration(total);
    }

    /**
     * Finds the nearest online player without loading
     * any new chunk.
     */
    public String findNearestPlayer(
            Location location,
            double maxDistance
    ) {

        if (location == null
                || location.getWorld() == null) {
            return null;
        }

        Player nearest = null;

        double nearestDistance =
                maxDistance * maxDistance;

        for (Player player :
                plugin.getServer().getOnlinePlayers()) {

            if (!player.getWorld()
                    .equals(location.getWorld())) {
                continue;
            }

            Location playerLocation =
                    player.getLocation();

            /*
             * Distance is calculated only.
             * No chunk loading occurs here.
             */
            double distance =
                    playerLocation.distanceSquared(
                            location
                    );

            if (distance <= nearestDistance) {

                nearestDistance = distance;
                nearest = player;
            }
        }

        return nearest == null
                ? null
                : nearest.getName();
    }

    public void remove(UUID uuid) {

        if (uuid == null) {
            return;
        }

        players.remove(uuid);
    }

    public int getTrackedPlayers() {
        return players.size();
    }

    private String formatAgo(
            long milliseconds
    ) {

        if (milliseconds < 0L) {
            milliseconds = 0L;
        }

        long seconds =
                milliseconds / 1000L;

        long minutes =
                seconds / 60L;

        long hours =
                minutes / 60L;

        long days =
                hours / 24L;

        if (days > 0L) {

            return days
                    + "d "
                    + (hours % 24L)
                    + "h ago";
        }

        if (hours > 0L) {

            return hours
                    + "h "
                    + (minutes % 60L)
                    + "m ago";
        }

        if (minutes > 0L) {

            return minutes
                    + "m "
                    + (seconds % 60L)
                    + "s ago";
        }

        return seconds + "s ago";
    }

    private String formatDuration(
            long milliseconds
    ) {

        if (milliseconds < 0L) {
            milliseconds = 0L;
        }

        long seconds =
                milliseconds / 1000L;

        long minutes =
                seconds / 60L;

        long hours =
                minutes / 60L;

        long days =
                hours / 24L;

        seconds %= 60L;
        minutes %= 60L;
        hours %= 24L;

        if (days > 0L) {

            return days
                    + "d "
                    + hours
                    + "h "
                    + minutes
                    + "m";
        }

        if (hours > 0L) {

            return hours
                    + "h "
                    + minutes
                    + "m";
        }

        if (minutes > 0L) {

            return minutes
                    + "m "
                    + seconds
                    + "s";
        }

        return seconds + "s";
    }

    public static final class PlayerData {

        private final UUID uuid;

        private String name;

        private final long firstSeen;

        private long lastSeen;

        private long lastActivity;

        private final long sessionStart;

        private long totalPlayTime;

        private PlayerData(
                UUID uuid,
                String name,
                long firstSeen,
                long lastSeen,
                long sessionStart,
                long totalPlayTime
        ) {

            this.uuid = uuid;
            this.name = name;
            this.firstSeen = firstSeen;
            this.lastSeen = lastSeen;
            this.sessionStart = sessionStart;
            this.totalPlayTime =
                    totalPlayTime;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public long getFirstSeen() {
            return firstSeen;
        }

        public long getLastSeen() {
            return lastSeen;
        }

        public long getLastActivity() {
            return lastActivity;
        }

        public long getSessionStart() {
            return sessionStart;
        }

        public long getTotalPlayTime() {
            return totalPlayTime;
        }
    }
                  }
