package ru.peacecraft.core.islands.listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.islands.model.IslandData;
import ru.peacecraft.core.persistence.model.PlayerData;

public final class IslandDiscoveryListener implements Listener {

    private final PeaceCraftPlugin plugin;
    private final Set<UUID> teleportCooldown;

    public IslandDiscoveryListener(PeaceCraftPlugin plugin) {
        this.plugin = plugin;
        this.teleportCooldown = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player.getUniqueId());

        if (playerData.getDiscoveredIslands().isEmpty()) {
            IslandData starterIsland = plugin.getIslandService().getStarterIsland();
            if (starterIsland != null) {
                Location spawnLocation = new Location(
                    player.getWorld(),
                    starterIsland.getSpawn().getX(),
                    starterIsland.getSpawn().getY(),
                    starterIsland.getSpawn().getZ()
                );
                player.teleportAsync(spawnLocation);
                playerData.addDiscoveredIsland(starterIsland.getId());
                playerData.addUnlockedIsland(starterIsland.getId());

                if (plugin.getMainConfigManager().shouldGiveStarterLogPose()) {
                    plugin.getLogPoseService().giveLogPose(player);

                    var target = ru.peacecraft.core.logpose.model.LogPoseTarget.fromIsland(starterIsland, true);
                    plugin.getLogPoseService().setTargetForPlayer(player, target);
                }

                plugin.getLogger().info(() -> "Teleported new player " + player.getName() + " to starter island: " + starterIsland.getId());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Кулдаун проверки (каждую секунду максимум)
        if (teleportCooldown.contains(playerId)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        // Проверяем только если игрок переместился в другой чанк
        if (from.getBlockX() >> 4 == to.getBlockX() >> 4 && from.getBlockZ() >> 4 == to.getBlockZ() >> 4) {
            return;
        }

        teleportCooldown.add(playerId);
        plugin
            .getServer()
            .getScheduler()
            .runTaskLater(plugin, () -> teleportCooldown.remove(playerId), 20L);

        checkIslandDiscovery(player, to);
    }

    private void checkIslandDiscovery(Player player, Location location) {
        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player.getUniqueId());

        for (IslandData island : plugin.getIslandService().getAllIslands()) {
            // Пропускаем уже обнаруженные острова
            if (playerData.isIslandDiscovered(island.getId())) {
                continue;
            }

            // Проверяем расстояние до центра острова
            double distance = getHorizontalDistance(location, island.getCenter());

            // Если игрок в радиусе 100 блоков от центра - обнаруживаем остров
            if (distance <= 100.0) {
                discoverIsland(player, island);
                break;
            }
        }
    }

    private double getHorizontalDistance(Location loc1, ru.peacecraft.core.islands.model.IslandCoordinate loc2) {
        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private void discoverIsland(Player player, IslandData island) {
        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player.getUniqueId());

        playerData.addDiscoveredIsland(island.getId());
        plugin.getPlayerDataService().savePlayer(player);

        player.sendMessage("§6§l=== Новый остров обнаружен! ===");
        player.sendMessage("§e" + island.getDisplayName() + " §7(" + island.getId() + ")");
        player.sendMessage("§7Исследуйте остров, чтобы найти сокровища и победить босса!");
        player.sendMessage("§6==============================");

        plugin.getLogger().info(() -> "Player " + player.getName() + " discovered island: " + island.getId());
    }
}
