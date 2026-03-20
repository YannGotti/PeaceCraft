package ru.peacecraft.core.islands.service;

import java.util.Collection;
import org.bukkit.Location;
import ru.peacecraft.core.config.ConfigManager;
import ru.peacecraft.core.islands.model.IslandCoordinate;
import ru.peacecraft.core.islands.model.IslandData;
import ru.peacecraft.core.islands.model.IslandEnterResult;
import ru.peacecraft.core.persistence.model.PlayerData;
import ru.peacecraft.core.persistence.service.PlayerDataService;

public final class IslandService {

    private static final double DEFAULT_DISCOVERY_RADIUS = 100.0D;

    private final IslandRegistry islandRegistry;
    private final ConfigManager configManager;
    private final PlayerDataService playerDataService;

    public IslandService(IslandRegistry islandRegistry, ConfigManager configManager, PlayerDataService playerDataService) {
        this.islandRegistry = islandRegistry;
        this.configManager = configManager;
        this.playerDataService = playerDataService;
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
        return starterIslandId == null ? null : islandRegistry.getById(starterIslandId);
    }

    public int getLoadedIslandCount() {
        return islandRegistry.size();
    }

    public boolean discoverIsland(java.util.UUID playerId, String islandId) {
        if (!islandExists(islandId)) {
            return false;
        }

        PlayerData playerData = playerDataService.getPlayerData(playerId);
        return playerData.addDiscoveredIsland(islandId);
    }

    public boolean unlockIsland(java.util.UUID playerId, String islandId) {
        if (!islandExists(islandId)) {
            return false;
        }

        PlayerData playerData = playerDataService.getPlayerData(playerId);
        return playerData.addUnlockedIsland(islandId);
    }

    public boolean setCurrentIsland(java.util.UUID playerId, String islandId) {
        if (!islandExists(islandId)) {
            return false;
        }

        PlayerData playerData = playerDataService.getPlayerData(playerId);
        return playerData.setCurrentIslandId(islandId);
    }

    public IslandEnterResult handlePlayerMovement(java.util.UUID playerId, Location location) {
        if (location == null) {
            return IslandEnterResult.none();
        }

        PlayerData playerData = playerDataService.getPlayerData(playerId);

        for (IslandData island : islandRegistry.getAll()) {
            if (playerData.isIslandDiscovered(island.getId())) {
                continue;
            }

            if (isWithinDiscoveryRadius(location, island, DEFAULT_DISCOVERY_RADIUS)) {
                boolean discoveredNow = discoverIsland(playerId, island.getId());
                boolean changedCurrent = setCurrentIsland(playerId, island.getId());

                if (discoveredNow || changedCurrent) {
                    playerDataService.save(playerId);
                }

                return new IslandEnterResult(discoveredNow, changedCurrent, island.getId());
            }
        }

        IslandData currentIsland = findIslandByLocation(location, DEFAULT_DISCOVERY_RADIUS);
        if (currentIsland != null) {
            boolean changedCurrent = setCurrentIsland(playerId, currentIsland.getId());
            if (changedCurrent) {
                return new IslandEnterResult(false, true, currentIsland.getId());
            }
        }

        return IslandEnterResult.none();
    }

    public IslandData findIslandByLocation(Location location, double radius) {
        if (location == null) {
            return null;
        }

        for (IslandData island : islandRegistry.getAll()) {
            if (isWithinDiscoveryRadius(location, island, radius)) {
                return island;
            }
        }

        return null;
    }

    private boolean isWithinDiscoveryRadius(Location location, IslandData island, double radius) {
        return getHorizontalDistance(location, island.getCenter()) <= radius;
    }

    private double getHorizontalDistance(Location location, IslandCoordinate coordinate) {
        double dx = location.getX() - coordinate.getX();
        double dz = location.getZ() - coordinate.getZ();
        return Math.sqrt((dx * dx) + (dz * dz));
    }
}
