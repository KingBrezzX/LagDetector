package me.itzbrezz.lagdetector.command;

import me.itzbrezz.lagdetector.LagDetector;
import me.itzbrezz.lagdetector.detection.LagDetectionManager;
import me.itzbrezz.lagdetector.detection.ScanManager;
import me.itzbrezz.lagdetector.history.HistoryManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class LagCommand implements CommandExecutor, TabCompleter {

    private final LagDetector plugin;

    public LagCommand(LagDetector plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!sender.hasPermission("lagdetector.use")
                && !sender.hasPermission("lagdetector.admin")) {
            send(sender, "command-messages.no-permission");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {

            case "detector":
                handleDetector(sender);
                break;

            case "gui":
                handleGui(sender);
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
                handleToggle(sender);
                break;

            case "scan":
                handleScan(sender);
                break;

            default:
                send(sender, "command-messages.unknown-command");
                break;
        }

        return true;
    }

    /*
     * ============================================================
     * DETECTOR
     * ============================================================
     */

    private void handleDetector(CommandSender sender) {

        LagDetectionManager manager = plugin.getDetectionManager();

        if (manager == null) {
            sender.sendMessage(color("&cLagDetectionManager is not available."));
            return;
        }

        sender.sendMessage(color("&8&m--------------------------------"));
        sender.sendMessage(color("&c&lLAG DETECTOR"));
        sender.sendMessage("");

        sender.sendMessage(
                color("&7Status: "
                        + (plugin.isDetectorEnabled()
                        ? "&aENABLED"
                        : "&cDISABLED"))
        );

        sender.sendMessage(
                color("&7Detection: "
                        + (manager.isRunning()
                        ? "&aRUNNING"
                        : "&cSTOPPED"))
        );

        sender.sendMessage(
                color("&7Current lag events: &f"
                        + manager.getActiveDetectionCount())
        );

        sender.sendMessage(
                color("&7Total detections: &f"
                        + manager.getTotalDetectionCount())
        );

        sender.sendMessage(color("&8&m--------------------------------"));
    }

    /*
     * ============================================================
     * GUI
     * ============================================================
     */

    private void handleGui(CommandSender sender) {

        if (!sender.hasPermission("lagdetector.gui")
                && !sender.hasPermission("lagdetector.admin")) {
            send(sender, "command-messages.no-permission");
            return;
        }

        if (!(sender instanceof Player player)) {
            send(sender, "command-messages.player-only");
            return;
        }

        /*
         * GUI implementation will be connected to LagGui
         * when the GUI files are added.
         */
        sender.sendMessage(color("&eLagDetector GUI is being initialized."));
    }

    /*
     * ============================================================
     * HISTORY
     * ============================================================
     */

    private void handleHistory(CommandSender sender, String[] args) {

        if (!sender.hasPermission("lagdetector.history")
                && !sender.hasPermission("lagdetector.admin")) {
            send(sender, "command-messages.no-permission");
            return;
        }

        int page = 1;

        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);

                if (page < 1) {
                    page = 1;
                }

            } catch (NumberFormatException exception) {
                send(sender, "command-messages.invalid-page");
                return;
            }
        }

        HistoryManager history = plugin.getHistoryManager();

        if (history == null) {
            sender.sendMessage(color("&cHistoryManager is not available."));
            return;
        }

        history.sendHistory(sender, page);
    }

    /*
     * ============================================================
     * RELOAD
     * ============================================================
     */

    private void handleReload(CommandSender sender) {

        if (!sender.hasPermission("lagdetector.reload")
                && !sender.hasPermission("lagdetector.admin")) {
            send(sender, "command-messages.no-permission");
            return;
        }

        plugin.reloadPlugin();

        send(sender, "command-messages.reload");
    }

    /*
     * ============================================================
     * INFO
     * ============================================================
     */

    private void handleInfo(CommandSender sender) {

        sender.sendMessage(color("&8&m--------------------------------"));
        sender.sendMessage(color("&c&lLagDetector"));
        sender.sendMessage("");

        sender.sendMessage(
                color("&7Version: &f"
                        + plugin.getDescription().getVersion())
        );

        sender.sendMessage(
                color("&7Detector: "
                        + (plugin.isDetectorEnabled()
                        ? "&aENABLED"
                        : "&cDISABLED"))
        );

        sender.sendMessage(color("&7Scan mode: &fLoaded chunks only"));
        sender.sendMessage(color("&7Force loading: &cDISABLED"));

        if (plugin.getHistoryManager() != null) {
            sender.sendMessage(
                    color("&7History entries: &f"
                            + plugin.getHistoryManager().size())
            );
        }

        sender.sendMessage(color("&8&m--------------------------------"));
    }

    /*
     * ============================================================
     * HELP
     * ============================================================
     */

    private void sendHelp(CommandSender sender) {

        List<String> help =
                plugin.getConfig().getStringList("help");

        if (help.isEmpty()) {
            sender.sendMessage(color("&cNo help messages configured."));
            return;
        }

        for (String line : help) {
            sender.sendMessage(color(line));
        }
    }

    /*
     * ============================================================
     * TOGGLE
     * ============================================================
     */

    private void handleToggle(CommandSender sender) {

        if (!sender.hasPermission("lagdetector.admin")) {
            send(sender, "command-messages.no-permission");
            return;
        }

        boolean current = plugin.isDetectorEnabled();

        if (current) {

            plugin.setDetectorEnabled(false);

            if (plugin.getDetectionManager() != null) {
                plugin.getDetectionManager().setEnabled(false);
            }

            send(sender, "command-messages.disabled");

        } else {

            plugin.setDetectorEnabled(true);

            if (plugin.getDetectionManager() != null) {
                plugin.getDetectionManager().setEnabled(true);
            }

            send(sender, "command-messages.enabled");
        }
    }

    /*
     * ============================================================
     * SCAN
     * ============================================================
     */

    private void handleScan(CommandSender sender) {

        if (!sender.hasPermission("lagdetector.scan")
                && !sender.hasPermission("lagdetector.admin")) {
            send(sender, "command-messages.no-permission");
            return;
        }

        ScanManager scanManager = plugin.getScanManager();

        if (scanManager == null) {
            sender.sendMessage(color("&cScanManager is not available."));
            return;
        }

        if (!scanManager.isEnabled()) {
            send(sender, "command-messages.scan-disabled");
            return;
        }

        if (scanManager.isScanning()) {
            send(sender, "command-messages.scan-running");
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    color("&cConsole cannot start a PLAYER centered scan.")
            );
            sender.sendMessage(
                    color("&7Change scan-center.mode to WORLD_SPAWN "
                            + "for console scanning.")
            );
            return;
        }

        boolean started = scanManager.startScan(player);

        if (started) {
            send(sender, "command-messages.scan-started");
        }
    }

    /*
     * ============================================================
     * TAB COMPLETER
     * ============================================================
     */

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {

        if (args.length == 1) {

            List<String> commands = Arrays.asList(
                    "detector",
                    "gui",
                    "history",
                    "reload",
                    "info",
                    "help",
                    "toggle",
                    "scan"
            );

            return filter(commands, args[0]);
        }

        if (args.length == 2
                && args[0].equalsIgnoreCase("history")) {

            return filter(
                    Arrays.asList(
                            "1",
                            "2",
                            "3",
                            "4",
                            "5"
                    ),
                    args[1]
            );
        }

        return Collections.emptyList();
    }

    private List<String> filter(
            List<String> values,
            String input
    ) {

        List<String> result = new ArrayList<>();

        for (String value : values) {

            if (value.toLowerCase()
                    .startsWith(input.toLowerCase())) {

                result.add(value);
            }
        }

        return result;
    }

    /*
     * ============================================================
     * MESSAGE HELPERS
     * ============================================================
     */

    private void send(
            CommandSender sender,
            String path
    ) {

        String message =
                plugin.getConfig().getString(
                        path,
                        "&cMessage not configured: " + path
                );

        sender.sendMessage(color(message));
    }

    private String color(String message) {

        if (message == null) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes(
                '&',
                message
        );
    }
            }
