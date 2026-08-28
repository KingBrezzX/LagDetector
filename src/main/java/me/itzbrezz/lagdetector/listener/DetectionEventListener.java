package me.itzbrezz.lagdetector.listener;

import me.itzbrezz.lagdetector.LagDetector;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class DetectionEventListener implements Listener {

    private final LagDetector plugin;

    public DetectionEventListener(LagDetector plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();

        /*
         * Ignore movement that only changes rotation.
         */
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        plugin.getPlayerTracker()
                .recordActivity(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {

        plugin.getPlayerTracker()
                .recordActivity(event.getPlayer());
    }
          }
