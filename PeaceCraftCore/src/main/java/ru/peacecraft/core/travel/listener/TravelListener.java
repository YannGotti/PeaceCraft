package ru.peacecraft.core.travel.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.islands.model.IslandData;
import ru.peacecraft.core.persistence.model.PlayerData;

public final class TravelListener implements Listener {

    private final PeaceCraftPlugin plugin;

    public TravelListener(PeaceCraftPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Обновление текущего острова при перемещении между островами
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND && event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
            return;
        }

        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(event.getPlayer().getUniqueId());

        // Определить остров по координатам
        String newIslandId = detectIslandByLocation(event.getTo());
        if (newIslandId != null && !newIslandId.equals(playerData.getCurrentIslandId())) {
            playerData.setCurrentIslandId(newIslandId);
            plugin.getPlayerDataService().savePlayer(event.getPlayer());
        }
    }

    /**
     * Определить остров по координатам
     */
    private String detectIslandByLocation(org.bukkit.Location location) {
        for (IslandData island : plugin.getIslandService().getAllIslands()) {
            if (!island.getWorldName().equals(location.getWorld().getName())) {
                continue;
            }

            double dx = location.getX() - island.getCenter().getX();
            double dz = location.getZ() - island.getCenter().getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance <= 500.0) {
                return island.getId();
            }
        }
        return null;
    }
}
