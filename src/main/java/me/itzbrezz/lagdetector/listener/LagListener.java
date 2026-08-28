package me.itzbrezz.lagdetector.listener;

import me.itzbrezz.lagdetector.LagDetector;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class LagListener implements Listener {

    private final LagDetector plugin;

    public LagListener(LagDetector plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        plugin.updatePlayerActivity(
                player
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        plugin.updatePlayerActivity(
                player
        );
    }
  }
