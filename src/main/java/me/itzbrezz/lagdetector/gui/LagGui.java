package me.itzbrezz.lagdetector.gui;

import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.LagSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class LagGui implements Listener {

    private final LagDetector plugin;

    private static final String TITLE =
            "Â§8Â§lLAG DETECTOR";

    public LagGui(LagDetector plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the main LagDetector GUI.
     */
    public void open(Player player) {

        int size = 27;

        String title = plugin.getConfig().getString(
                "gui.title",
                "&8&lLAG DETECTOR"
        );

        Inventory inventory =
                Bukkit.createInventory(
                        null,
                        size,
                        color(title)
                );

        fillBackground(inventory);

        /*
         * Detection status
         */
        inventory.setItem(
                10,
                createItem(
                        plugin.getConfig().getString(
                                "gui.items.status.material",
                                "COMPASS"
                        ),
                        plugin.getConfig().getString(
                                "gui.items.status.name",
                                "&b&lDetector Status"
                        ),
                        List.of(
                                "&7Status: "
                                        + (
                                        plugin.isDetectorEnabled()
                                                ? "&aENABLED"
                                                : "&cDISABLED"
                                ),
                                "",
                                "&7Click to toggle."
                        )
                )
        );

        /*
         * Scan
         */
        inventory.setItem(
                12,
                createItem(
                        plugin.getConfig().getString(
                                "gui.items.scan.material",
                                "SCANNER"
                        ),
                        plugin.getConfig().getString(
                                "gui.items.scan.name",
                                "&e&lScan"
                        ),
                        List.of(
                                "&7Scan only already-loaded chunks.",
                                "&7Unloaded chunks are ignored.",
                                "",
                                "&eClick to start scan."
                        )
                )
        );

        /*
         * History
         */
        inventory.setItem(
                14,
                createItem(
                        plugin.getConfig().getString(
                                "gui.items.history.material",
                                "BOOK"
                        ),
                        plugin.getConfig().getString(
                                "gui.items.history.name",
                                "&6&lLag History"
                        ),
                        List.of(
                                "&7Stored detections: &f"
                                        + plugin.getHistoryManager()
                                        .size(),
                                "",
                                "&eClick to view history."
                        )
                )
        );

        /*
         * Information
         */
        inventory.setItem(
                16,
                createItem(
                        plugin.getConfig().getString(
                                "gui.items.info.material",
                                "PAPER"
                        ),
                        plugin.getConfig().getString(
                                "gui.items.info.name",
                                "&b&lInformation"
                        ),
                        List.of(
                                "&7TPS: &f"
                                        + String.format(
                                        "%.2f",
                                        plugin.getLagDetectionManager()
                                                .getLastTps()
                                ),
                                "&7MSPT: &f"
                                        + String.format(
                                        "%.2f",
                                        plugin.getLagDetectionManager()
                                                .getLastMspt()
                                ),
                                "",
                                "&eClick for information."
                        )
                )
        );

        /*
         * Close
         */
        inventory.setItem(
                22,
                createItem(
                        "BARRIER",
                        "&c&lClose",
                        List.of(
                                "&7Close this menu."
                        )
                )
        );

        player.openInventory(inventory);
    }

    /**
     * Opens the detection action GUI.
     */
    public void openDetection(
            Player player,
            LagSnapshot snapshot
    ) {

        if (snapshot == null) {
            return;
        }

        Inventory inventory =
                Bukkit.createInventory(
                        null,
                        27,
                        color(
                                plugin.getConfig().getString(
                                        "gui.detection-title",
                                        "&8&lLAG DETECTOR"
                                )
                        )
                );

        fillBackground(inventory);

        /*
         * Detection information
         */
        inventory.setItem(
                4,
                createItem(
                        "REDSTONE",
                        "&c&lLAG SUS",
                        List.of(
                                "&7Lag Type: &f"
                                        + snapshot.getLagType(),
                                "&7World: &f"
                                        + snapshot.getWorld(),
                                "&7Coords: &f"
                                        + snapshot.getCoordinates(),
                                "&7Player: &f"
                                        + (
                                        snapshot.hasPlayer()
                                                ? snapshot.getPlayer()
                                                : "Unknown"
                                ),
                                "&7TPS: &f"
                                        + String.format(
                                        "%.2f",
                                        snapshot.getTps()
                                ),
                                "&7MSPT: &f"
                                        + String.format(
                                        "%.2f",
                                        snapshot.getMspt()
                                )
                        )
                )
        );

        /*
         * TP
         */
        inventory.setItem(
                10,
                createItem(
                        "ENDER_PEARL",
                        "&b&lTELEPORT",
                        List.of(
                                "&7Teleport to the detected",
                                "&7redstone core.",
                                "",
                                "&eClick to teleport."
                        )
                )
        );

        /*
         * BREAK
         */
        inventory.setItem(
                12,
                createItem(
                        "DIAMOND_PICKAXE",
                        "&c&lBREAK CORE",
                        List.of(
                                "&7Destroy the detected",
                                "&7redstone core.",
                                "",
                                "&cClick to break."
                        )
                )
        );

        /*
         * EXPLODE
         */
        inventory.setItem(
                14,
                createItem(
                        "TNT",
                        "&4&lEXPLODE",
                        List.of(
                                "&7Explode the detected",
                                "&7redstone core.",
                                "",
                                "&cClick to explode."
                        )
                )
        );

        /*
         * BAN
         */
        inventory.setItem(
                16,
                createItem(
                        "IRON_BARS",
                        "&4&lBAN",
                        List.of(
                                "&7Ban the suspected player.",
                                "",
                                "&cClick to ban."
                        )
                )
        );

        /*
         * Back
         */
        inventory.setItem(
                22,
                createItem(
                        "ARROW",
                        "&e&lBACK",
                        List.of(
                                "&7Return to main menu."
                        )
                )
        );

        /*
         * Store snapshot in the player's temporary
         * GUI session.
         */
        plugin.setGuiSnapshot(
                player,
                snapshot
        );

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(
            InventoryClickEvent event
    ) {

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        String title =
                event.getView()
                        .getTitle();

        if (!title.equals(TITLE)
                && !title.equals(
                color(
                        plugin.getConfig().getString(
                                "gui.title",
                                "&8&lLAG DETECTOR"
                        )
                ))
                && !title.equals(
                color(
                        plugin.getConfig().getString(
                                "gui.detection-title",
                                "&8&lLAG DETECTOR"
                        )
                ))) {

            return;
        }

        event.setCancelled(true);

        int slot =
                event.getRawSlot();

        if (slot < 0
                || slot >= event.getView()
                .getTopInventory()
                .getSize()) {
            return;
        }

        /*
         * Main GUI
         */
        if (isMainGui(title)) {

            switch (slot) {

                case 10:
                    plugin.setDetectorEnabled(
                            !plugin.isDetectorEnabled()
                    );
                    open(player);
                    break;

                case 12:
                    player.closeInventory();

                    if (!plugin.getScanManager()
                            .isScanning()) {

                        plugin.getScanManager()
                                .startScan(player);
                    } else {

                        player.sendMessage(
                                color(
                                        "&eA scan is already running."
                                )
                        );
                    }
                    break;

                case 14:
                    player.closeInventory();

                    plugin.getHistoryManager()
                            .sendHistory(
                                    player,
                                    1
                            );
                    break;

                case 16:
                    player.closeInventory();

                    player.sendMessage(
                            color(
                                    "&bTPS: &f"
                                            + String.format(
                                            "%.2f",
                                            plugin.getLagDetectionManager()
                                                    .getLastTps()
                                    )
                            )
                    );

                    player.sendMessage(
                            color(
                                    "&bMSPT: &f"
                                            + String.format(
                                            "%.2f",
                                            plugin.getLagDetectionManager()
                                                    .getLastMspt()
                                    )
                            )
                    );
                    break;

                case 22:
                    player.closeInventory();
                    break;

                default:
                    break;
            }

            return;
        }

        /*
         * Detection GUI
         */
        LagSnapshot snapshot =
                plugin.getGuiSnapshot(player);

        if (snapshot == null) {
            player.closeInventory();
            return;
        }

        switch (slot) {

            case 10:
                handleTeleport(
                        player,
                        snapshot
                );
                break;

            case 12:
                handleBreak(
                        player,
                        snapshot
                );
                break;

            case 14:
                handleExplode(
                        player,
                        snapshot
                );
                break;

            case 16:
                handleBan(
                        player,
                        snapshot
                );
                break;

            case 22:
                plugin.removeGuiSnapshot(player);
                open(player);
                break;

            default:
                break;
        }
    }

    private void handleTeleport(
            Player player,
            LagSnapshot snapshot
    ) {

        WorldCheckResult result =
                getLocation(snapshot);

        if (!result.valid) {
            player.sendMessage(
                    color("&cThe world or location is unavailable.")
            );
            return;
        }

        player.teleport(
                result.location
        );

        player.sendMessage(
                color(
                        "&aTeleported to the detected redstone core."
                )
        );
    }

    private void handleBreak(
            Player player,
            LagSnapshot snapshot
    ) {

        if (!player.hasPermission(
                "lagdetector.action.break"
        )
                && !player.hasPermission(
                "lagdetector.admin"
        )) {

            player.sendMessage(
                    color("&cYou don't have permission.")
            );
            return;
        }

        WorldCheckResult result =
                getLocation(snapshot);

        if (!result.valid) {
            player.sendMessage(
                    color("&cThe detected location is unavailable.")
            );
            return;
        }

        Block block =
                result.location.getBlock();

        if (block.getType()
                == Material.AIR) {

            player.sendMessage(
                    color("&cThe detected block no longer exists.")
            );
            return;
        }

        /*
         * Only the detected core block is broken.
         */
        block.breakNaturally();

        player.sendMessage(
                color(
                        "&aThe detected redstone core was broken."
                )
        );

        plugin.removeGuiSnapshot(player);
        player.closeInventory();
    }

    private void handleExplode(
            Player player,
            LagSnapshot snapshot
    ) {

        if (!player.hasPermission(
                "lagdetector.action.explode"
        )
                && !player.hasPermission(
                "lagdetector.admin"
        )) {

            player.sendMessage(
                    color("&cYou don't have permission.")
            );
            return;
        }

        WorldCheckResult result =
                getLocation(snapshot);

        if (!result.valid) {
            player.sendMessage(
                    color("&cThe detected location is unavailable.")
            );
            return;
        }

        boolean fire =
                plugin.getConfig().getBoolean(
                        "actions.explode.fire",
                        false
                );

        float power =
                (float) plugin.getConfig()
                        .getDouble(
                                "actions.explode.power",
                                2.0D
                        );

        /*
         * EXPLODE is intentionally limited to the configured
         * power and location.
         */
        result.location.getWorld()
                .createExplosion(
                        result.location,
                        power,
                        fire
                );

        player.sendMessage(
                color(
                        "&aThe redstone core was exploded."
                )
        );

        plugin.removeGuiSnapshot(player);
        player.closeInventory();
    }

    private void handleBan(
            Player player,
            LagSnapshot snapshot
    ) {

        if (!player.hasPermission(
                "lagdetector.action.ban"
        )
                && !player.hasPermission(
                "lagdetector.admin"
        )) {

            player.sendMessage(
                    color("&cYou don't have permission.")
            );
            return;
        }

        if (!snapshot.hasPlayer()) {

            player.sendMessage(
                    color(
                            "&cNo player is associated with this detection."
                    )
            );

            return;
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

        player.sendMessage(
                color(
                        "&aPlayer &f"
                                + target
                                + " &ahas been processed."
                )
        );

        plugin.removeGuiSnapshot(player);
        player.closeInventory();
    }

    private boolean isMainGui(
            String title
    ) {

        return title.equals(TITLE)
                || title.equals(
                color(
                        plugin.getConfig().getString(
                                "gui.title",
                                "&8&lLAG DETECTOR"
                        )
                ));
    }

    private WorldCheckResult getLocation(
            LagSnapshot snapshot
    ) {

        World world =
                Bukkit.getWorld(
                        snapshot.getWorld()
                );

        if (world == null) {
            return new WorldCheckResult(
                    false,
                    null
            );
        }

        /*
         * Never load a chunk just for an action.
         */
        if (!world.isChunkLoaded(
                snapshot.getX() >> 4,
                snapshot.getZ() >> 4
        )) {

            return new WorldCheckResult(
                    false,
                    null
            );
        }

        Location location =
                new Location(
                        world,
                        snapshot.getX() + 0.5,
                        snapshot.getY(),
                        snapshot.getZ() + 0.5
                );

        return new WorldCheckResult(
                true,
                location
        );
    }

    private void fillBackground(
            Inventory inventory
    ) {

        ItemStack item =
                createItem(
                        "GRAY_STAINED_GLASS_PANE",
                        " ",
                        List.of()
                );

        for (int i = 0;
             i < inventory.getSize();
             i++) {

            inventory.setItem(
                    i,
                    item
            );
        }
    }

    private ItemStack createItem(
            String materialName,
            String name,
            List<String> lore
    ) {

        Material material;

        try {

            material =
                    Material.valueOf(
                            materialName.toUpperCase()
                    );

        } catch (IllegalArgumentException exception) {

            /*
             * SCANNER does not exist as a normal Bukkit
             * Material on many Paper versions.
             *
             * Use COMPASS as safe fallback.
             */
            material = Material.COMPASS;
        }

        ItemStack item =
                new ItemStack(material);

        ItemMeta meta =
                item.getItemMeta();

        if (meta != null) {

            meta.setDisplayName(
                    color(name)
            );

            List<String> coloredLore =
                    new ArrayList<>();

            for (String line : lore) {

                coloredLore.add(
                        color(line)
                );
            }

            meta.setLore(
                    coloredLore
            );

            item.setItemMeta(meta);
        }

        return item;
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

    private static final class WorldCheckResult {

        private final boolean valid;
        private final Location location;

        private WorldCheckResult(
                boolean valid,
                Location location
        ) {
            this.valid = valid;
            this.location = location;
        }
    }
            }
