package me.itzbrezz.lagdetector.player;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player activity: last-seen timestamps and
 * approximate session (play) time. Used by detection
 * and notification logic.
 */
public final class PlayerTracker {

    private final Map<UUID, Long> lastActivityByUuid = new ConcurrentHashMap<>();

    private final Map<String, Long> lastSeenByName = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionStartByName = new ConcurrentHashMap<>();

    /**
     * Records that the given player is active right now.
     * Called from event listeners (movement, interaction, etc.)
     * to keep tracking data current.
     */
    public void recordActivity(Player player) {
        if (player == null) {
            return;
        }

        long now = System.currentTimeMillis();

        lastActivityByUuid.put(player.getUniqueId(), now);

        String name = player.getName();

        lastSeenByName.put(name, now);
        sessionStartByName.putIfAbsent(name, now);
    }

    /**
     * Alias for {@link #recordActivity(Player)} â€” kept in case
     * other call sites use this name instead.
     */
    public void updateActivity(Player player) {
        recordActivity(player);
    }

    /**
     * Returns a human-readable "last online" description for
     * the given player name (e.g. "Just now", "5m ago"),
     * or "Unknown" if no activity has been recorded.
     */
    public String getLastOnline(String playerName) {

        if (playerName == null) {
            return "Unknown";
        }

        Long lastSeen = lastSeenByName.get(playerName);

        if (lastSeen == null) {
            return "Unknown";
        }

        long elapsed = System.currentTimeMillis() - lastSeen;

        return formatDuration(elapsed) + " ago";
    }

    /**
     * Returns a human-readable approximate play time for the
     * given player name, measured from the first activity we
     * recorded for their current session, or "Unknown" if no
     * activity has been recorded.
     *
     * NOTE: this is an approximation based on tracked activity,
     * not the player's real total playtime (that would require
     * hooking PlayerJoinEvent / statistics separately).
     */
    public String getPlayTime(String playerName) {

        if (playerName == null) {
            return "Unknown";
        }

        Long sessionStart = sessionStartByName.get(playerName);

        if (sessionStart == null) {
            return "Unknown";
        }

        long elapsed = System.currentTimeMillis() - sessionStart;

        return formatDuration(elapsed);
    }

    /**
     * Returns the timestamp (millis) of the player's last
     * recorded activity, or -1 if none is recorded.
     */
    public long getLastActivity(UUID uuid) {
        return lastActivityByUuid.getOrDefault(uuid, -1L);
    }

    /**
     * Returns true if the player has been active within
     * the given window (in milliseconds).
     */
    public boolean isActive(UUID uuid, long windowMillis) {
        long last = getLastActivity(uuid);

        if (last < 0) {
            return false;
        }

        return (System.currentTimeMillis() - last) <= windowMillis;
    }

    /**
     * Removes all tracking data for a specific player
     * (e.g. on quit), keyed by UUID and name.
     */
    public void remove(UUID uuid, String playerName) {
        if (uuid != null) {
            lastActivityByUuid.remove(uuid);
        }

        if (playerName != null) {
            lastSeenByName.remove(playerName);
            sessionStartByName.remove(playerName);
        }
    }

    /**
     * Clears all tracked activity data.
     */
    public void clear() {
        lastActivityByUuid.clear();
        lastSeenByName.clear();
        sessionStartByName.clear();
    }

    private String formatDuration(long millis) {

        long seconds = millis / 1000;

        if (seconds < 60) {
            return seconds + "s";
        }

        long minutes = seconds / 60;

        if (minutes < 60) {
            return minutes + "m";
        }

        long hours = minutes / 60;
        long remMinutes = minutes % 60;

        if (hours < 24) {
            return hours + "h " + remMinutes + "m";
        }

        long days = hours / 24;
        long remHours = hours % 24;

        return days + "d " + remHours + "h";
    }
                }
