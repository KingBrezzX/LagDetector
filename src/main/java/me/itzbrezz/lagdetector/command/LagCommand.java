package me.itzbrezz.lagdetector.command;

import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.ScanManager;
import me.itzbrezz.lagdetector.history.HistoryManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class LagCommand implements CommandExecutor, TabCompleter {

    private final LagDetector plugin;
    private final ScanManager scanManager;
    private final HistoryManager historyManager;

    public LagCommand(
            LagDetector plugin,
            ScanManager scanManager,
            HistoryManager historyManager
    ) {
        this.plugin = plugin;
        this.scanManager = scanManager;
        this.historyManager = historyManager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!hasPermission(sender, "lagdetector.use")) {
            send(sender, "messages.no-permission");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand =
                args[0].toLowerCase();

        switch (subCommand) {

            case "gui":
                handleGui(sender);
                break;

            case "scan":
                handleScan(sender);
                break;

            case "history":
                handleHistory(sender, args);
                break;

            case "reload":
                handleReload(sender);
                break;

            case "info":
                handleInfo(sender);
                break;

            case "help":
                sendHelp(sender);
                break;

            case "toggle":
                handleToggle(sender, args);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    /*
     * ============================================================
     * /lag gui
     * ============================================================
     */

    private void handleGui(
            CommandSender sender
    ) {

        if (!(sender instanceof Player player)) {

            send(
                    sender,
                    "command-messages.player-only"
            );

            return;
        }

        if (!hasPermission(
                sender,
                "lagdetector.gui"
        )) {
            send(
                    sender,
                    "messages.no-permission"
            );
            return;
        }

        /*
         * GUI manager will be connected in the next file.
         *
         * We intentionally use a safe hook here so the command
         * can be completed without forcing chunk loading or
         * performing any scan.
         */
        if (plugin.getLagGui() == null) {

            player.sendMessage(
                    color(
                            "&cLag GUI is not initialized yet."
                    )
            );

            return;
        }

        plugin.getLagGui().open(player);
    }

    /*
     * ============================================================
     * /lag scan
     * ============================================================
     */

    private void handleScan(
            CommandSender sender
    ) {

        if (!(sender instanceof Player player)) {

            send(
                    sender,
                    "command-messages.player-only"
            );

            return;
        }

        if (!hasPermission(
                sender,
                "lagdetector.scan"
        )) {
            send(
                    sender,
                    "messages.no-permission"
            );
            return;
        }

        if (scanManager.isScanning()) {

            send(
                    sender,
                    "command-messages.scan-running"
            );

            return;
        }

        scanManager.startScan(player);
    }

    /*
     * ============================================================
     * /lag history
     * ============================================================
     */

    private void handleHistory(
            CommandSender sender,
            String[] args
    ) {

        if (!hasPermission(
                sender,
                "lagdetector.history"
        )) {
            send(
                    sender,
                    "messages.no-permission"
            );
            return;
        }

        int page = 1;

        if (args.length >= 2) {

            try {

                page = Integer.parseInt(
                        args[1]
                );

            } catch (NumberFormatException exception) {

                send(
                        sender,
                        "command-messages.invalid-page"
                );

                return;
            }
        }

        if (page < 1) {
            page = 1;
        }

        historyManager.sendHistory(
                sender,
                page
        );
    }

    /*
     * ============================================================
     * /lag reload
     * ============================================================
     */

    private void handleReload(
            CommandSender sender
    ) {

        if (!hasPermission(
                sender,
                "lagdetector.reload"
        )) {
            send(
                    sender,
                    "messages.no-permission"
            );
            return;
        }

        plugin.reloadPlugin();

        send(
                sender,
                "command-messages.reload"
        );
    }

    /*
     * ============================================================
     * /lag info
     * ============================================================
     */

    private void handleInfo(
            CommandSender sender
    ) {

        if (!hasPermission(
                sender,
                "lagdetector.info"
        )) {
            send(
                    sender,
                    "messages.no-permission"
            );
            return;
        }

        sender.sendMessage(
                color("&8&m--------------------------------")
        );

        sender.sendMessage(
                color("&b&lLAG DETECTOR")
        );

        sender.sendMessage("");

        sender.sendMessage(
                color("&7Status: "
                        + (
                        plugin.isDetectorEnabled()
                                ? "&aENABLED"
                                : "&cDISABLED"
                ))
        );

        sender.sendMessage(
                color("&7TPS: &f"
                        + String.format(
                        "%.2f",
                        plugin.getLagDetectionManager()
                                .getLastTps()
                ))
        );

        sender.sendMessage(
                color("&7MSPT: &f"
                        + String.format(
                        "%.2f",
                        plugin.getLagDetectionManager()
                                .getLastMspt()
                ))
        );

        sender.sendMessage(
                color("&7Active detections: &f"
                        + plugin.getLagDetectionManager()
                                .getActiveDetectionCount())
        );

        sender.sendMessage(
                color("&7Total detections: &f"
                        + plugin.getLagDetectionManager()
                                .getTotalDetectionCount())
        );

        sender.sendMessage(
                color("&7Tracked redstone locations: &f"
                        + plugin.getRedstoneDetector()
                                .getTrackedLocations())
        );

        sender.sendMessage(
                color("&7History entries: &f"
                        + historyManager.size())
        );

        sender.sendMessage("");

        sender.sendMessage(
                color("&8&m--------------------------------")
        );
    }

    /*
     * ============================================================
     * /lag toggle
     * ============================================================
     */

    private void handleToggle(
            CommandSender sender,
            String[] args
    ) {

        if (!hasPermission(
                sender,
                "lagdetector.toggle"
        )) {
            send(
                    sender,
                    "messages.no-permission"
            );
            return;
        }

        if (args.length < 2) {

            sender.sendMessage(
                    color(
                            "&cUsage: /lag toggle <enable|disable>"
                    )
            );

            return;
        }

        String mode =
                args[1].toLowerCase();

        switch (mode) {

            case "enable":

                plugin.setDetectorEnabled(
                        true
                );

                send(
                        sender,
                        "command-messages.toggle-enabled"
                );

                break;

            case "disable":

                plugin.setDetectorEnabled(
                        false
                );

                send(
                        sender,
                        "command-messages.toggle-disabled"
                );

                break;

            default:

                sender.sendMessage(
                        color(
                                "&cUsage: /lag toggle <enable|disable>"
                        )
                );

                break;
        }
    }

    /*
     * ============================================================
     * HELP
     * ============================================================
     */

    private void sendHelp(
            CommandSender sender
    ) {

        sender.sendMessage(
                color("&8&m--------------------------------")
        );

        sender.sendMessage(
                color("&b&lLAG DETECTOR &7Commands")
        );

        sender.sendMessage("");

        sender.sendMessage(
                color("&f/lag gui &7- Open lag detector GUI")
        );

        sender.sendMessage(
                color("&f/lag scan &7- Scan loaded chunks")
        );

        sender.sendMessage(
                color("&f/lag history [page] &7- View lag history")
        );

        sender.sendMessage(
                color("&f/lag reload &7- Reload configuration")
        );

        sender.sendMessage(
                color("&f/lag info &7- Show detector information")
        );

        sender.sendMessage(
                color("&f/lag toggle enable &7- Enable detector")
        );

        sender.sendMessage(
                color("&f/lag toggle disable &7- Disable detector")
        );

        sender.sendMessage(
                color("&f/lag help &7- Show this help")
        );

        sender.sendMessage("");

        sender.sendMessage(
                color("&8&m--------------------------------")
        );
    }

    /*
     * ============================================================
     * PERMISSION
     * ============================================================
     */

    private boolean hasPermission(
            CommandSender sender,
            String permission
    ) {

        return sender.hasPermission(
                permission
        )
                || sender.hasPermission(
                "lagdetector.admin"
        );
    }

    /*
     * ============================================================
     * MESSAGES
     * ============================================================
     */

    private void send(
            CommandSender sender,
            String path
    ) {

        String message =
                plugin.getConfig().getString(
                        path
                );

        if (message == null) {

            message =
                    switch (path) {

                        case "messages.no-permission" ->
                                "&cYou don't have permission.";

                        case "command-messages.player-only" ->
                                "&cThis command can only be used by a player.";

                        case "command-messages.scan-running" ->
                                "&eA scan is already running.";

                        case "command-messages.invalid-page" ->
                                "&cInvalid page number.";

                        case "command-messages.reload" ->
                                "&aLagDetector configuration reloaded.";

                        case "command-messages.toggle-enabled" ->
                                "&aLagDetector has been enabled.";

                        case "command-messages.toggle-disabled" ->
                                "&cLagDetector has been disabled.";

                        default ->
                                "&cMessage not configured.";
                    };
        }

        sender.sendMessage(
                color(message)
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

    @Override
    public java.util.List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        if (args.length == 1) {
            java.util.List<String> options = new java.util.ArrayList<>(
                    java.util.List.of(
                            "gui",
                            "scan",
                            "history",
                            "reload",
                            "info",
                            "help",
                            "toggle"
                    )
            );

            String input = args[0].toLowerCase();
            options.removeIf(option -> !option.startsWith(input));
            return options;
        }

        if (args.length == 2
                && args[0].equalsIgnoreCase("toggle")) {
            String input = args[1].toLowerCase();
            java.util.List<String> options =
                    new java.util.ArrayList<>(
                            java.util.List.of("enable", "disable")
                    );
            options.removeIf(option -> !option.startsWith(input));
            return options;
        }

        return java.util.Collections.emptyList();
    }


