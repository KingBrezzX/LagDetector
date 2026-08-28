package me.itzbrezz.lagdetector;

import me.itzbrezz.lagdetector.command.LagCommand;
import me.itzbrezz.lagdetector.detection.LagDetectionManager;
import me.itzbrezz.lagdetector.detection.ScanManager;
import me.itzbrezz.lagdetector.history.HistoryManager;
import me.itzbrezz.lagdetector.gui.LagGui;
import me.itzbrezz.lagdetector.detection.RedstoneDetector;
import me.itzbrezz.lagdetector.placeholder.LagDetectorExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class LagDetector extends JavaPlugin {

    private static LagDetector instance;

    private HistoryManager historyManager;
    private LagDetectionManager detectionManager;
    private ScanManager scanManager;
    private RedstoneDetector redstoneDetector;
    private LagGui lagGui;
    private final Map<UUID, me.itzbrezz.lagdetector.detection.LagSnapshot> guiSnapshots =
            new ConcurrentHashMap<>();

    private boolean detectorEnabled;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        detectorEnabled = getConfig().getBoolean("detector.enabled", true);

        createDataFolder();

        historyManager = new HistoryManager(this);
        detectionManager = new LagDetectionManager(this);
        scanManager = new ScanManager(this);
        redstoneDetector = new RedstoneDetector(this, detectionManager);
        lagGui = new LagGui(this);

        Bukkit.getPluginManager().registerEvents(lagGui, this);
        redstoneDetector.start();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new LagDetectorExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }

        LagCommand lagCommand = new LagCommand(
                this,
                scanManager,
                historyManager
        );

        if (getCommand("lag") != null) {
            getCommand("lag").setExecutor(lagCommand);
            getCommand("lag").setTabCompleter(lagCommand);
        } else {
            getLogger().severe("Command 'lag' was not found in plugin.yml!");
        }

        detectionManager.start();
        scanManager.start();

        getLogger().info("========================================");
        getLogger().info("LagDetector has been enabled.");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("Detector: " + (detectorEnabled ? "ENABLED" : "DISABLED"));
        getLogger().info("Loaded-chunks-only scan: ENABLED");
        getLogger().info("Force chunk loading: DISABLED");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {

        if (detectionManager != null) {
            detectionManager.stop();
        }

        if (scanManager != null) {
            scanManager.stop();
        }

        if (redstoneDetector != null) {
            redstoneDetector.stop();
        }

        if (historyManager != null) {
            historyManager.save();
        }

        getLogger().info("LagDetector has been disabled.");

        instance = null;
    }

    /**
     * Reloads the plugin configuration.
     */
    public void reloadPlugin() {
        reloadConfig();

        detectorEnabled = getConfig().getBoolean("detector.enabled", true);

        if (detectionManager != null) {
            detectionManager.reload();
        }

        if (scanManager != null) {
            scanManager.reload();
        }

        getLogger().info("LagDetector configuration reloaded.");
    }

    /**
     * Creates the plugin data folder if it does not exist.
     */
    private void createDataFolder() {
        File folder = getDataFolder();

        if (!folder.exists() && !folder.mkdirs()) {
            getLogger().warning(
                    "Could not create plugin data folder: "
                            + folder.getAbsolutePath()
            );
        }
    }

    public static LagDetector getInstance() {
        return instance;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public LagDetectionManager getDetectionManager() {
        return detectionManager;
    }

    public ScanManager getScanManager() {
        return scanManager;
    }

    public boolean isDetectorEnabled() {
        return detectorEnabled;
    }

    public void setDetectorEnabled(boolean enabled) {
        this.detectorEnabled = enabled;

        if (detectionManager != null) {
            detectionManager.setEnabled(enabled);
        }

        if (redstoneDetector != null) {
            redstoneDetector.setEnabled(enabled);
        }
    }

    public LagDetectionManager getLagDetectionManager() {
        return detectionManager;
    }

    public RedstoneDetector getRedstoneDetector() {
        return redstoneDetector;
    }

    public LagGui getLagGui() {
        return lagGui;
    }

    public void setGuiSnapshot(
            Player player,
            me.itzbrezz.lagdetector.detection.LagSnapshot snapshot
    ) {
        if (player != null && snapshot != null) {
            guiSnapshots.put(player.getUniqueId(), snapshot);
        }
    }

    public me.itzbrezz.lagdetector.detection.LagSnapshot getGuiSnapshot(
            Player player
    ) {
        if (player == null) {
            return null;
        }

        return guiSnapshots.get(player.getUniqueId());
    }

    public void removeGuiSnapshot(Player player) {
        if (player != null) {
            guiSnapshots.remove(player.getUniqueId());
        }
    }

    public String color(String text) {
        if (text == null) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Returns the number of online players.
     */
    public int getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().size();
    }
  }

