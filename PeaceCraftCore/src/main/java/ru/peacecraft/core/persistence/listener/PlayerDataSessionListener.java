package ru.peacecraft.core.persistence.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.persistence.model.PlayerData;
import ru.peacecraft.core.persistence.service.PlayerDataService;

public final class PlayerDataSessionListener implements Listener {

    private final PeaceCraftPlugin plugin;
    private final PlayerDataService playerDataService;

    public PlayerDataSessionListener(PeaceCraftPlugin plugin, PlayerDataService playerDataService) {
        this.plugin = plugin;
        this.playerDataService = playerDataService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerData data = playerDataService.getPlayerData(event.getPlayer());

        plugin
            .getLogger()
            .info(
                () ->
                    "Loaded data for " +
                    event.getPlayer().getName() +
                    " | Island: " +
                    data.getCurrentIslandId() +
                    " | Unlocked: " +
                    data.getUnlockedIslands().size()
            );
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerDataService.unload(event.getPlayer());
        plugin.getLogger().info(() -> "Saved and unloaded data for " + event.getPlayer().getName());
    }
}
