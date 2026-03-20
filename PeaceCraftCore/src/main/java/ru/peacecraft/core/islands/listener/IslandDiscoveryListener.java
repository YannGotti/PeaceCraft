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
import ru.peacecraft.core.islands.model.IslandEnterResult;
import ru.peacecraft.core.islands.service.IslandService;
import ru.peacecraft.core.islands.service.PlayerOnboardingService;

public final class IslandDiscoveryListener implements Listener {

    private final PeaceCraftPlugin plugin;
    private final IslandService islandService;
    private final PlayerOnboardingService onboardingService;
    private final Set<UUID> movementCheckCooldown = new HashSet<>();

    public IslandDiscoveryListener(PeaceCraftPlugin plugin, IslandService islandService, PlayerOnboardingService onboardingService) {
        this.plugin = plugin;
        this.islandService = islandService;
        this.onboardingService = onboardingService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        onboardingService.handleFirstJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (movementCheckCooldown.contains(playerId)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if ((from.getBlockX() >> 4) == (to.getBlockX() >> 4) && (from.getBlockZ() >> 4) == (to.getBlockZ() >> 4)) {
            return;
        }

        movementCheckCooldown.add(playerId);
        plugin
            .getServer()
            .getScheduler()
            .runTaskLater(plugin, () -> movementCheckCooldown.remove(playerId), 20L);

        IslandEnterResult result = islandService.handlePlayerMovement(playerId, to);
        if (!result.hasChanges()) {
            return;
        }

        if (result.isDiscoveredNow()) {
            IslandData island = islandService.getIslandById(result.getIslandId());
            if (island != null) {
                player.sendMessage("§6§l=== Новый остров обнаружен! ===");
                player.sendMessage("§e" + island.getDisplayName() + " §7(" + island.getId() + ")");
                player.sendMessage("§7Исследуйте остров, чтобы найти сокровища и победить босса!");
                player.sendMessage("§6==============================");

                plugin.getLogger().info(() -> "Player " + player.getName() + " discovered island: " + island.getId());
            }
        }
    }
}
