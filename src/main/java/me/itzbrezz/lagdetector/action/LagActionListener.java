package me.itzbrezz.lagdetector.action;

import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.LagSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

public final class LagActionListener implements Listener {

    private final LagDetector plugin;

    public LagActionListener(LagDetector plugin) {
        this.plugin = plugin;
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onInventoryClick(
            InventoryClickEvent event
    ) {

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        LagSnapshot snapshot =
                plugin.getGuiSnapshot(player);

        if (snapshot == null) {
            return;
        }

        String title =
                event.getView().getTitle();

        String configuredTitle =
                color(
                        plugin.getConfig().getString(
                                "gui.detection-title",
                                "&8&lLAG DETECTOR"
                        )
                );

        if (!title.equals(configuredTitle)) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();

        switch (slot) {

            case 10:
                teleport(
                        player,
                        snapshot
                );
                break;

            case 12:
                breakCore(
                        player,
                        snapshot
                );
                break;

            case 14:
                explodeCore(
                        player,
                        snapshot
                );
                break;

            case 16:
                banPlayer(
                        player,
                        snapshot
                );
                break;

            case 22:
                plugin.removeGuiSnapshot(player);

                if (plugin.getLagGui() != null) {
                    plugin.getLagGui().open(player);
                }

                break;

            default:
                break;
        }
    }

    /**
     * TELEPORT
     */
    private void teleport(
            Player staff,
            LagSnapshot snapshot
    ) {

        if (!hasPermission(
                staff,
                "lagdetector.action.tp"
        )) {
            noPermission(staff);
            return;
        }

        World world =
                Bukkit.getWorld(
                        snapshot.getWorld()
                );

        if (world == null) {

            staff.sendMessage(
                    color(
                            "&cWorld is no longer available."
                    )
            );

            return;
        }

        /*
         * Never force-load the chunk.
         */
        if (!world.isChunkLoaded(
                snapshot.getX() >> 4,
                snapshot.getZ() >> 4
        )) {

            staff.sendMessage(
                    color(
                            "&cThe detected chunk is no longer loaded."
                    )
            );

            return;
        }

        staff.teleport(
                new org.bukkit.Location(
                        world,
                        snapshot.getX() + 0.5,
                        snapshot.getY() + 1.0,
                        snapshot.getZ() + 0.5
                )
        );

        staff.sendMessage(
                color(
                        plugin.getConfig().getString(
                                "actions.tp.message",
                                "&aTeleported to the redstone core."
                        )
                )
        );
    }

    /**
     * BREAK CORE
     */
    private void breakCore(
            Player staff,
            LagSnapshot snapshot
    ) {

        if (!hasPermission(
                staff,
                "lagdetector.action.break"
        )) {
            noPermission(staff);
            return;
        }

        Block block =
                getLoadedBlock(snapshot);

        if (block == null) {

            staff.sendMessage(
                    color(
                            "&cThe detected chunk is no longer loaded."
                    )
            );

            return;
        }

        if (block.getType() == Material.AIR) {

            staff.sendMessage(
                    color(
                            "&cThe detected core no longer exists."
                    )
            );

            return;
        }

        Material previous =
                block.getType();

        /*
         * Only the detected core is removed.
         *
         * This is NOT a world-wide cleanup.
         */
        block.breakNaturally();

        createProtectSign(
                block,
                snapshot
        );

        staff.sendMessage(
                color(
                        plugin.getConfig().getString(
                                "actions.break.message",
                                "&aRedstone core destroyed."
                        )
                )
        );

        plugin.removeGuiSnapshot(staff);

        staff.closeInventory();

        plugin.getHistoryManager().save();

        plugin.getLogger().info(
                "BREAK action executed by "
                        + staff.getName()
                        + " at "
                        + snapshot.getWorld()
                        + " "
                        + snapshot.getCoordinates()
                        + " | Previous block="
                        + previous.name()
        );
    }

    /**
     * EXPLODE
     */
    private void explodeCore(
            Player staff,
            LagSnapshot snapshot
    ) {

        if (!hasPermission(
                staff,
                "lagdetector.action.explode"
        )) {
            noPermission(staff);
            return;
        }

        World world =
                Bukkit.getWorld(
                        snapshot.getWorld()
                );

        if (world == null) {

            staff.sendMessage(
                    color(
                            "&cWorld is no longer available."
                    )
            );

            return;
        }

        /*
         * Never load an unloaded chunk.
         */
        if (!world.isChunkLoaded(
                snapshot.getX() >> 4,
                snapshot.getZ() >> 4
        )) {

            staff.sendMessage(
                    color(
                            "&cThe detected chunk is no longer loaded."
                    )
            );

            return;
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

        org.bukkit.Location location =
                new org.bukkit.Location(
                        world,
                        snapshot.getX() + 0.5,
                        snapshot.getY(),
                        snapshot.getZ() + 0.5
                );

        /*
         * Paper/Bukkit explosion.
         */
        world.createExplosion(
                location,
                power,
                fire,
                breakBlocks
        );

        staff.sendMessage(
                color(
                        plugin.getConfig().getString(
                                "actions.explode.message",
                                "&aThe redstone core was exploded."
                        )
                )
        );

        plugin.removeGuiSnapshot(staff);

        staff.closeInventory();

        plugin.getHistoryManager().save();

        plugin.getLogger().info(
                "EXPLODE action executed by "
                        + staff.getName()
                        + " at "
                        + snapshot.getWorld()
                        + " "
                        + snapshot.getCoordinates()
        );
    }

    /**
     * BAN
     */
    private void banPlayer(
            Player staff,
            LagSnapshot snapshot
    ) {

        if (!hasPermission(
                staff,
                "lagdetector.action.ban"
        )) {
            noPermission(staff);
            return;
        }

        String target =
                snapshot.getPlayer();

        if (target == null
                || target.isBlank()) {

            staff.sendMessage(
                    color(
                            "&cNo player is associated with this detection."
                    )
            );

            return;
        }

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

        message =
                message.replace(
                        "%player%",
                        target
                );

        staff.sendMessage(
                color(message)
        );

        plugin.removeGuiSnapshot(staff);

        staff.closeInventory();

        plugin.getHistoryManager().save();

        plugin.getLogger().info(
                "BAN action executed by "
                        + staff.getName()
                        + " | Target="
                        + target
                        + " | Reason="
                        + reason
        );
    }

    /**
     * Finds the detected block only if its chunk
     * is currently loaded.
     */
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

    /**
     * Creates the configurable protection sign
     * after BREAK.
     *
     * The sign is placed only when the block immediately
     * below the destroyed core is suitable.
     */
    private void createProtectSign(
            Block destroyedBlock,
            LagSnapshot snapshot
    ) {

        if (!plugin.getConfig().getBoolean(
                "actions.break.sign.enabled",
                true
        )) {
            return;
        }

        World world =
                destroyedBlock.getWorld();

        int x =
                destroyedBlock.getX();

        int y =
                destroyedBlock.getY();

        int z =
                destroyedBlock.getZ();

        /*
         * Prefer the destroyed block's position.
         * If it is air, a sign can occupy that position.
         */
        Block signBlock =
                world.getBlockAt(
                        x,
                        y,
                        z
                );

        if (!signBlock.getType().isAir()) {
            return;
        }

        signBlock.setType(
                Material.OAK_SIGN,
                false
        );

        org.bukkit.block.Sign sign =
                (org.bukkit.block.Sign)
                        signBlock.getState();

        String[] configuredLines =
                plugin.getConfig()
                        .getStringList(
                                "actions.break.sign.lines"
                        )
                        .toArray(
                                new String[0]
                        );

        for (int i = 0; i < 4; i++) {

            String line =
                    i < configuredLines.length
                            ? configuredLines[i]
                            : "";

            line =
                    line
                            .replace(
                                    "%world%",
                                    snapshot.getWorld()
                            )
                            .replace(
                                    "%x%",
                                    String.valueOf(
                                            snapshot.getX()
                                    )
                            )
                            .replace(
                                    "%y%",
                                    String.valueOf(
                                            snapshot.getY()
                                    )
                            )
                            .replace(
                                    "%z%",
                                    String.valueOf(
                                            snapshot.getZ()
                                    )
                            );

            sign.line(
                    i,
                    net.kyori.adventure.text.Component
                            .text(
                                    ChatColor.stripColor(
                                            color(line)
                                    )
                            )
            );
        }

        sign.update(
                true,
                false
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
                color(
                        plugin.getConfig().getString(
                                "messages.no-permission",
                                "&cYou don't have permission."
                        )
                )
        );
    }

    private String color(
            String text
    ) {

        if (text == null) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes(
                '&',
                text
        );
    }
                          }
