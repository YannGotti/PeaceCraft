package ru.peacecraft.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import ru.peacecraft.core.PeaceCraftPlugin;

public final class ConfigManager {

    private final PeaceCraftPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(PeaceCraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public boolean isDebugEnabled() {
        return config.getBoolean("plugin.debug", false);
    }

    public String getLanguage() {
        return config.getString("plugin.language", "ru");
    }

    public String getPrefix() {
        return config.getString("plugin.prefix", "&6PeaceCraft &8» &f");
    }

    public boolean isAutosaveEnabled() {
        return config.getBoolean("autosave.enabled", true);
    }

    public int getAutosaveIntervalSeconds() {
        return config.getInt("autosave.interval-seconds", 300);
    }

    public boolean shouldGiveStarterLogPose() {
        return config.getBoolean("starter.give-log-pose-on-first-join", true);
    }

    public String getStarterIslandId() {
        return config.getString("starter.island-id", "starter_island");
    }

    public boolean shouldPrintBootstrapSteps() {
        return config.getBoolean("development.print-bootstrap-steps", true);
    }
}
