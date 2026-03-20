package ru.peacecraft.core.islands.config;

import java.io.File;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.islands.model.IslandCoordinate;
import ru.peacecraft.core.islands.model.IslandData;
import ru.peacecraft.core.islands.service.IslandRegistry;

public final class IslandsConfig {

    private static final String FILE_NAME = "islands.yml";

    private final PeaceCraftPlugin plugin;
    private final IslandRegistry islandRegistry;

    private File islandsFile;
    private FileConfiguration islandsConfig;

    public IslandsConfig(PeaceCraftPlugin plugin, IslandRegistry islandRegistry) {
        this.plugin = plugin;
        this.islandRegistry = islandRegistry;
    }

    public void load() {
        ensureBundledResourceExists();
        ensureFileExistsInDataFolder();

        this.islandsFile = new File(plugin.getDataFolder(), FILE_NAME);
        this.islandsConfig = YamlConfiguration.loadConfiguration(this.islandsFile);

        loadIslandsIntoRegistry();
    }

    public void reload() {
        if (this.islandsFile == null) {
            load();
            return;
        }

        this.islandsConfig = YamlConfiguration.loadConfiguration(this.islandsFile);
        loadIslandsIntoRegistry();
    }

    private void ensureBundledResourceExists() {
        if (plugin.getResource(FILE_NAME) == null) {
            throw new IllegalStateException("Embedded resource not found in jar: " + FILE_NAME);
        }
    }

    private void ensureFileExistsInDataFolder() {
        File targetFile = new File(plugin.getDataFolder(), FILE_NAME);

        if (!targetFile.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
    }

    private void loadIslandsIntoRegistry() {
        islandRegistry.clear();

        ConfigurationSection islandsSection = islandsConfig.getConfigurationSection("islands");
        if (islandsSection == null) {
            throw new IllegalStateException("Missing 'islands' root section in islands.yml");
        }

        Set<String> islandIds = islandsSection.getKeys(false);
        if (islandIds.isEmpty()) {
            throw new IllegalStateException("No islands defined in islands.yml");
        }

        for (String islandId : islandIds) {
            String basePath = "islands." + islandId;

            IslandData islandData = new IslandData(
                islandId,
                requireString(basePath + ".display-name"),
                requireString(basePath + ".world"),
                requireString(basePath + ".region-id"),
                islandsConfig.getString(basePath + ".currency-id", "scrap"),
                requireString(basePath + ".unlock-requirement"),
                requireCoordinate(basePath + ".center"),
                requireCoordinate(basePath + ".spawn"),
                requireCoordinate(basePath + ".port"),
                requireCoordinate(basePath + ".compass-target")
            );

            islandRegistry.register(islandData);
        }
    }

    private String requireString(String path) {
        String value = islandsConfig.getString(path);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing or blank string in islands.yml: " + path);
        }
        return value;
    }

    private IslandCoordinate requireCoordinate(String path) {
        if (!islandsConfig.isConfigurationSection(path)) {
            throw new IllegalStateException("Missing coordinate section in islands.yml: " + path);
        }

        double x = islandsConfig.getDouble(path + ".x");
        double y = islandsConfig.getDouble(path + ".y");
        double z = islandsConfig.getDouble(path + ".z");

        return new IslandCoordinate(x, y, z);
    }
}
