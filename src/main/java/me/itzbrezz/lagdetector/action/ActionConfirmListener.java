package me.itzbrezz.lagdetector.action;

import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.LagSnapshot;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActionConfirmListener implements Listener {

    private final LagDetector plugin;
    private final ActionManager actionManager;

    private final Map<UUID, PendingAction> pending =
            new ConcurrentHashMap<>();

    public ActionConfirmListener(
            LagDetector plugin,
            ActionManager actionManager
    ) {
        this.plugin = plugin;
        this.actionManager = actionManager;
    }

    public void request(
            Player player,
            LagSnapshot snapshot,
            ActionType action
    ) {

        if (player == null || snapshot == null) {
            return;
        }

        pending.put(
                player.getUniqueId(),
                new PendingAction(
                        snapshot,
                        action,
                        System.currentTimeMillis()
                )
        );

        player.sendMessage(
                color(
                        "&c&lCONFIRM ACTION"
                )
        );

        player.sendMessage(
                color(
                        "&7Action: &f"
                                + action.name()
                )
        );

        player.sendMessage(
                color(
                        "&7Location: &f"
                                + snapshot.getWorld()
                                + " "
                                + snapshot.getCoordinates()
                )
        );

        player.sendMessage(
                color(
                        "&7Type &a/lag confirm &7to continue."
                )
        );

        player.sendMessage(
                color(
                        "&7Type &c/lag cancel &7to cancel."
                )
        );
    }

    public boolean confirm(
            Player player
    ) {

        PendingAction action =
                pending.remove(
                        player.getUniqueId()
                );

        if (action == null) {
            player.sendMessage(
                    color(
                            "&eThere is no pending action."
                    )
            );
            return false;
        }

        if (isExpired(action)) {
            player.sendMessage(
                    color(
                            "&cThe confirmation has expired."
                    )
            );
            return false;
        }

        switch (action.type) {

            case EXPLODE:
                return actionManager.explodeCore(
                        player,
                        action.snapshot
                );

            case BAN:
                return actionManager.ban(
                        player,
                        action.snapshot
                );

            case BREAK:
                return actionManager.breakCore(
                        player,
                        action.snapshot
                );

            case TP:
                return actionManager.teleport(
                        player,
                        action.snapshot
                );

            default:
                return false;
        }
    }

    public void cancel(
            Player player
    ) {

        pending.remove(
                player.getUniqueId()
        );

        player.sendMessage(
                color(
                        "&aPending action cancelled."
                )
        );
    }

    public boolean hasPending(
            Player player
    ) {

        return player != null
                && pending.containsKey(
                player.getUniqueId()
        );
    }

    private boolean isExpired(
            PendingAction action
    ) {

        long timeout =
                plugin.getConfig().getLong(
                        "actions.confirmation-timeout-seconds",
                        30L
                );

        return System.currentTimeMillis()
                - action.createdAt
                > timeout * 1000L;
    }

    private String color(
            String text
    ) {

        return ChatColor.translateAlternateColorCodes(
                '&',
                text
        );
    }

    public enum ActionType {
        TP,
        BREAK,
        EXPLODE,
        BAN
    }

    private static final class PendingAction {

        private final LagSnapshot snapshot;
        private final ActionType type;
        private final long createdAt;

        private PendingAction(
                LagSnapshot snapshot,
                ActionType type,
                long createdAt
        ) {
            this.snapshot = snapshot;
            this.type = type;
            this.createdAt = createdAt;
        }
    }
          }
