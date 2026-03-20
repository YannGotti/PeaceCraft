package ru.peacecraft.core.persistence.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.persistence.model.PlayerData;
import ru.peacecraft.core.persistence.repository.PlayerDataRepository;

public final class PlayerDataService implements Listener {

    private final PeaceCraftPlugin plugin;
    private final PlayerDataRepository repository;
    private final Map<UUID, PlayerData> cache;
    private final int autosaveIntervalSeconds;

    public PlayerDataService(PeaceCraftPlugin plugin, PlayerDataRepository repository, int autosaveIntervalSeconds) {
        this.plugin = plugin;
        this.repository = repository;
        this.cache = new ConcurrentHashMap<>();
        this.autosaveIntervalSeconds = autosaveIntervalSeconds;
    }

    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        if (autosaveIntervalSeconds > 0) {
            startAutosaveTask();
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return cache.computeIfAbsent(uuid, repository::load);
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public void savePlayer(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            repository.save(data);
        }
    }

    public void savePlayer(Player player) {
        savePlayer(player.getUniqueId());
    }

    public void unloadPlayer(UUID uuid) {
        savePlayer(uuid);
        cache.remove(uuid);
    }

    public void unloadPlayer(Player player) {
        unloadPlayer(player.getUniqueId());
    }

    private void startAutosaveTask() {
        long intervalTicks = autosaveIntervalSeconds * 20L;
        plugin
            .getServer()
            .getScheduler()
            .runTaskTimerAsynchronously(
                plugin,
                () -> {
                    for (UUID uuid : cache.keySet()) {
                        savePlayer(uuid);
                    }
                    plugin.getLogger().info(() -> "Autosaved " + cache.size() + " player(s).");
                },
                intervalTicks,
                intervalTicks
            );
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerData data = getPlayerData(event.getPlayer());
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
        unloadPlayer(event.getPlayer());
        plugin.getLogger().info(() -> "Saved and unloaded data for " + event.getPlayer().getName());
    }
}
