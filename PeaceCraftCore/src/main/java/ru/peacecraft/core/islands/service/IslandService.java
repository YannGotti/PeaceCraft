package ru.peacecraft.core.islands.service;

import java.util.Collection;
import ru.peacecraft.core.config.ConfigManager;
import ru.peacecraft.core.islands.model.IslandData;

public final class IslandService {

    private final IslandRegistry islandRegistry;
    private final ConfigManager configManager;

    public IslandService(IslandRegistry islandRegistry, ConfigManager configManager) {
        this.islandRegistry = islandRegistry;
        this.configManager = configManager;
    }

    public boolean islandExists(String islandId) {
        return islandRegistry.contains(islandId);
    }

    public IslandData getIslandById(String islandId) {
        return islandRegistry.getById(islandId);
    }

    public Collection<IslandData> getAllIslands() {
        return islandRegistry.getAll();
    }

    public IslandData getStarterIsland() {
        String starterIslandId = configManager.getStarterIslandId();
        return islandRegistry.getById(starterIslandId);
    }

    public int getLoadedIslandCount() {
        return islandRegistry.size();
    }
}
