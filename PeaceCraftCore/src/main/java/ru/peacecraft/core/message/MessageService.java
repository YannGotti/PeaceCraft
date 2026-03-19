package ru.peacecraft.core.message;

import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.config.ConfigManager;

public final class MessageService {

    private final PeaceCraftPlugin plugin;
    private final ConfigManager configManager;

    private File messageFile;
    private FileConfiguration messages;
    private boolean loaded;

    public MessageService(PeaceCraftPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void load() {
        String language = configManager.getLanguage();
        String fileName = "messages_" + language + ".yml";

        ensureBundledResourceExists(fileName);
        ensureFileExistsInDataFolder(fileName);

        this.messageFile = new File(plugin.getDataFolder(), fileName);
        this.messages = YamlConfiguration.loadConfiguration(this.messageFile);
        this.loaded = true;
    }

    public void reload() {
        if (this.messageFile == null) {
            load();
            return;
        }

        this.messages = YamlConfiguration.loadConfiguration(this.messageFile);
        this.loaded = true;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String get(String path) {
        String raw = messages.getString(path);

        if (raw == null) {
            String fallback = messages.getString("errors.missing-message", "Missing message: %path%");
            if (fallback == null || fallback.isEmpty()) {
                fallback = "Missing message: %path%";
            }

            return colorize(fallback.replace("%path%", path));
        }

        String prefix = messages.getString("prefix", "&6PeaceCraft &8» &f");
        if (prefix == null || prefix.isEmpty()) {
            prefix = "&6PeaceCraft &8» &f";
        }

        return colorize(raw.replace("%prefix%", prefix));
    }

    public String getPlain(String path) {
        return ChatColor.stripColor(get(path));
    }

    private void ensureBundledResourceExists(String fileName) {
        if (plugin.getResource(fileName) == null) {
            throw new IllegalStateException("Embedded resource not found in jar: " + fileName);
        }
    }

    private void ensureFileExistsInDataFolder(String fileName) {
        File targetFile = new File(plugin.getDataFolder(), fileName);

        if (!targetFile.exists()) {
            plugin.saveResource(fileName, false);
        }
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
