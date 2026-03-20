package ru.peacecraft.core.islands.service;

import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.peacecraft.core.config.ConfigManager;
import ru.peacecraft.core.islands.model.IslandData;
import ru.peacecraft.core.logpose.model.LogPoseTarget;
import ru.peacecraft.core.logpose.service.LogPoseService;
import ru.peacecraft.core.persistence.model.PlayerData;
import ru.peacecraft.core.persistence.service.PlayerDataService;

public final class PlayerOnboardingService {

    private final ConfigManager configManager;
    private final IslandService islandService;
    private final PlayerDataService playerDataService;
    private final LogPoseService logPoseService;

    @SuppressWarnings("NonConstantLogger")
    private final Logger logger;

    public PlayerOnboardingService(
        ConfigManager configManager,
        IslandService islandService,
        PlayerDataService playerDataService,
        LogPoseService logPoseService,
        Logger logger
    ) {
        this.configManager = configManager;
        this.islandService = islandService;
        this.playerDataService = playerDataService;
        this.logPoseService = logPoseService;
        this.logger = logger;
    }

    public void handleFirstJoin(Player player) {
        PlayerData playerData = playerDataService.getPlayerData(player);

        if (playerData.hasAnyDiscoveredIsland()) {
            return;
        }

        IslandData starterIsland = islandService.getStarterIsland();
        if (starterIsland == null) {
            throw new IllegalStateException("Starter island was not found.");
        }

        islandService.discoverIsland(player.getUniqueId(), starterIsland.getId());
        islandService.unlockIsland(player.getUniqueId(), starterIsland.getId());
        islandService.setCurrentIsland(player.getUniqueId(), starterIsland.getId());

        Location spawnLocation = new Location(
            player.getWorld(),
            starterIsland.getSpawn().getX(),
            starterIsland.getSpawn().getY(),
            starterIsland.getSpawn().getZ()
        );

        player.teleportAsync(spawnLocation);

        if (configManager.shouldGiveStarterLogPose()) {
            logPoseService.giveLogPose(player);

            LogPoseTarget target = LogPoseTarget.fromIsland(starterIsland, true);
            logPoseService.setTargetForPlayer(player, target);

            playerDataService.markDirty(player);
        }

        playerDataService.save(player);

        logger.info(() -> "Teleported new player " + player.getName() + " to starter island: " + starterIsland.getId());
    }
}
