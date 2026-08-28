package me.itzbrezz.lagdetector.player;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the last-known activity timestamp for each
 * online player. Used by detection/notification logic
 * to decide whether a player was recently active.
 */
public final class PlayerTracker {

    private final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();

    /**
     * Marks the given player as active right now.
     */
    public void updateActivity(Player player) {
        if (player == null) {
            return;
        }

        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Returns the timestamp (millis) of the player's last
     * recorded activity, or -1 if none is recorded.
     */
    public long getLastActivity(UUID uuid) {
        return lastActivity.getOrDefault(uuid, -1L);
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
     * Removes tracking data for a specific player
     * (e.g. on quit).
     */
    public void remove(UUID uuid) {
        if (uuid != null) {
            lastActivity.remove(uuid);
        }
    }

    /**
     * Clears all tracked activity data.
     */
    public void clear() {
        lastActivity.clear();
    }
}
