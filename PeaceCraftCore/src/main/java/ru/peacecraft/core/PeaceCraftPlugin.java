package ru.peacecraft.core;

import org.bukkit.plugin.java.JavaPlugin;
import ru.peacecraft.core.config.ConfigManager;
import ru.peacecraft.core.islands.config.IslandsConfig;
import ru.peacecraft.core.islands.model.IslandData;
import ru.peacecraft.core.islands.service.IslandRegistry;
import ru.peacecraft.core.islands.service.IslandService;
import ru.peacecraft.core.message.MessageService;
import ru.peacecraft.core.persistence.repository.PlayerDataRepository;
import ru.peacecraft.core.persistence.service.PlayerDataService;

public final class PeaceCraftPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private MessageService messageService;
    private IslandRegistry islandRegistry;
    private IslandsConfig islandsConfig;
    private IslandService islandService;
    private PlayerDataRepository playerDataRepository;
    private PlayerDataService playerDataService;

    @Override
    public void onEnable() {
        createPluginDataFolder();
        getLogger().info("Bootstrapping PeaceCraftCore...");

        this.configManager = new ConfigManager(this);
        getLogger().info("Loading config.yml...");
        this.configManager.load();

        getLogger().info("Loading message file...");
        this.messageService = new MessageService(this, this.configManager);
        this.messageService.load();

        getLogger().info("Loading islands.yml...");
        this.islandRegistry = new IslandRegistry();
        this.islandsConfig = new IslandsConfig(this, this.islandRegistry);
        this.islandsConfig.load();

        this.islandService = new IslandService(this.islandRegistry, this.configManager);
        logLoadedIslands();

        getLogger().info("Initializing persistence layer...");
        this.playerDataRepository = new PlayerDataRepository(this);
        this.playerDataService = new PlayerDataService(this, this.playerDataRepository, this.configManager.getAutosaveIntervalSeconds());
        this.playerDataService.initialize();

        getLogger().info(messageService.getPlain("startup.plugin-enabled"));
    }

    @Override
    public void onDisable() {
        if (playerDataService != null) {
            getLogger().info("Saving all player data before shutdown...");
        }
        if (messageService != null && messageService.isLoaded()) {
            getLogger().info(messageService.getPlain("startup.plugin-disabled"));
        } else {
            getLogger().info("PeaceCraftCore disabled.");
        }
    }

    private void createPluginDataFolder() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("Could not create plugin data folder: " + getDataFolder().getAbsolutePath());
        }
    }

    private void logLoadedIslands() {
        getLogger().info(() -> "Loaded " + islandService.getLoadedIslandCount() + " islands.");
        for (IslandData islandData : islandService.getAllIslands()) {
            getLogger().info(() -> "- " + islandData.getId() + " (" + islandData.getDisplayName() + ")");
        }
        IslandData starterIsland = islandService.getStarterIsland();
        if (starterIsland == null) {
            throw new IllegalStateException("Starter island from config.yml was not found in islands.yml");
        }
        getLogger().info(() -> "Starter island: " + starterIsland.getId() + " (" + starterIsland.getDisplayName() + ")");
    }

    public ConfigManager getMainConfigManager() {
        return configManager;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public IslandRegistry getIslandRegistry() {
        return islandRegistry;
    }

    public IslandService getIslandService() {
        return islandService;
    }

    public IslandsConfig getIslandsConfig() {
        return islandsConfig;
    }

    public PlayerDataRepository getPlayerDataRepository() {
        return playerDataRepository;
    }

    public PlayerDataService getPlayerDataService() {
        return playerDataService;
    }
}
