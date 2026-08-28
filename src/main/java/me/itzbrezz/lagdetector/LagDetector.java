package me.itzbrezz.lagdetector;

import me.itzbrezz.lagdetector.command.LagCommand;
import me.itzbrezz.lagdetector.detection.LagDetectionManager;
import me.itzbrezz.lagdetector.detection.ScanManager;
import me.itzbrezz.lagdetector.history.HistoryManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class LagDetector extends JavaPlugin {

    private static LagDetector instance;

    private HistoryManager historyManager;
    private LagDetectionManager detectionManager;
    private ScanManager scanManager;

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

        LagCommand lagCommand = new LagCommand(this);

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
    }

    /**
     * Returns the number of online players.
     */
    public int getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().size();
    }
  }
