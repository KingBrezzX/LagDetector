package me.itzbrezz.lagdetector.action;

import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.LagSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class ActionManager {

    private final LagDetector plugin;
    private final SignManager signManager;

    public ActionManager(LagDetector plugin) {
        this.plugin = plugin;
        this.signManager = new SignManager(plugin);
    }

    public boolean teleport(
            Player staff,
            LagSnapshot snapshot
    ) {

        if (!hasPermission(
                staff,
                "lagdetector.action.tp"
        )) {
            noPermission(staff);
            return false;
        }

        Location location =
                getLoadedLocation(snapshot);

        if (location == null) {
            staff.sendMessage(
                    plugin.color(
                            "&cThe detected chunk is no longer loaded."
                    )
            );
            return false;
        }

        location.setY(
                location.getY() + 1.0D
        );

        staff.teleport(location);

        staff.sendMessage(
                plugin.color(
                        plugin.getConfig().getString(
                                "actions.tp.message",
                                "&aTeleported to the redstone core."
                        )
                )
        );

        return true;
    }

    public boolean breakCore(
            Player staff,
            LagSnapshot snapshot
    ) {

        if (!hasPermission(
                staff,
                "lagdetector.action.break"
        )) {
            noPermission(staff);
            return false;
        }

        Block block =
                getLoadedBlock(snapshot);

        if (block == null) {
            staff.sendMessage(
                    plugin.color(
                            "&cThe detected chunk is no longer loaded."
                    )
            );
            return false;
        }

        if (block.getType() == Material.AIR) {
            staff.sendMessage(
                    plugin.color(
                            "&cThe detected core no longer exists."
                    )
            );
            return false;
        }

        block.setType(
                Material.AIR,
                false
        );

        /*
         * Protection sign is created at the
         * destroyed core location.
         */
        signManager.createProtectionSign(
                block,
                snapshot
        );

        staff.sendMessage(
                plugin.color(
                        plugin.getConfig().getString(
                                "actions.break.message",
                                "&aRedstone core destroyed."
                        )
                )
        );

        logAction(
                "BREAK",
                staff,
                snapshot
        );

        return true;
    }

    public boolean explodeCore(
            Player staff,
            LagSnapshot snapshot
    ) {

        if (!hasPermission(
                staff,
                "lagdetector.action.explode"
        )) {
            noPermission(staff);
            return false;
        }

        Location location =
                getLoadedLocation(snapshot);

        if (location == null) {
            staff.sendMessage(
                    plugin.color(
                            "&cThe detected chunk is no longer loaded."
                    )
            );
            return false;
        }

        float power =
                (float) plugin.getConfig()
                        .getDouble(
                                "actions.explode.power",
                                2.0D
                        );

        boolean fire =
                plugin.getConfig().getBoolean(
                        "actions.explode.fire",
                        false
                );

        boolean breakBlocks =
                plugin.getConfig().getBoolean(
                        "actions.explode.break-blocks",
                        true
                );

        location.getWorld()
                .createExplosion(
                        location,
                        power,
                        fire,
                        breakBlocks
                );

        staff.sendMessage(
                plugin.color(
                        plugin.getConfig().getString(
                                "actions.explode.message",
                                "&aThe redstone core was exploded."
                        )
                )
        );

        logAction(
                "EXPLODE",
                staff,
                snapshot
        );

        return true;
    }

    public boolean ban(
            Player staff,
            LagSnapshot snapshot
    ) {

        if (!hasPermission(
                staff,
                "lagdetector.action.ban"
        )) {
            noPermission(staff);
            return false;
        }

        if (!snapshot.hasPlayer()) {
            staff.sendMessage(
                    plugin.color(
                            "&cNo player is associated with this detection."
                    )
            );
            return false;
        }

        String target =
                snapshot.getPlayer();

        String reason =
                plugin.getConfig().getString(
                        "actions.ban.reason",
                        "Lag machine detected"
                );

        String command =
                plugin.getConfig().getString(
                        "actions.ban.command",
                        "ban %player% %reason%"
                );

        command =
                command
                        .replace(
                                "%player%",
                                target
                        )
                        .replace(
                                "%reason%",
                                reason
                        );

        Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                command
        );

        String message =
                plugin.getConfig().getString(
                        "actions.ban.staff-message",
                        "&cPlayer &f%player% &chas been banned."
                );

        staff.sendMessage(
                plugin.color(
                        message.replace(
                                "%player%",
                                target
                        )
                )
        );

        logAction(
                "BAN",
                staff,
                snapshot
        );

        return true;
    }

    private Block getLoadedBlock(
            LagSnapshot snapshot
    ) {

        World world =
                Bukkit.getWorld(
                        snapshot.getWorld()
                );

        if (world == null) {
            return null;
        }

        int chunkX =
                snapshot.getX() >> 4;

        int chunkZ =
                snapshot.getZ() >> 4;

        /*
         * IMPORTANT:
         * Never load an unloaded chunk.
         */
        if (!world.isChunkLoaded(
                chunkX,
                chunkZ
        )) {
            return null;
        }

        return world.getBlockAt(
                snapshot.getX(),
                snapshot.getY(),
                snapshot.getZ()
        );
    }

    private Location getLoadedLocation(
            LagSnapshot snapshot
    ) {

        World world =
                Bukkit.getWorld(
                        snapshot.getWorld()
                );

        if (world == null) {
            return null;
        }

        int chunkX =
                snapshot.getX() >> 4;

        int chunkZ =
                snapshot.getZ() >> 4;

        if (!world.isChunkLoaded(
                chunkX,
                chunkZ
        )) {
            return null;
        }

        return new Location(
                world,
                snapshot.getX() + 0.5D,
                snapshot.getY(),
                snapshot.getZ() + 0.5D
        );
    }

    private boolean hasPermission(
            Player player,
            String permission
    ) {

        return player.hasPermission(
                permission
        )
                || player.hasPermission(
                "lagdetector.admin"
        );
    }

    private void noPermission(
            Player player
    ) {

        player.sendMessage(
                plugin.color(
                        plugin.getConfig().getString(
                                "messages.no-permission",
                                "&cYou don't have permission."
                        )
                )
        );
    }

    private void logAction(
            String action,
            Player staff,
            LagSnapshot snapshot
    ) {

        plugin.getLogger().info(
                "[ACTION] "
                        + action
                        + " | Staff="
                        + staff.getName()
                        + " | World="
                        + snapshot.getWorld()
                        + " | XYZ="
                        + snapshot.getCoordinates()
        );
    }
              }
