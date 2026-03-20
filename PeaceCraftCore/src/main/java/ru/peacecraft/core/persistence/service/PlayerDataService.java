package ru.peacecraft.core.persistence.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import ru.peacecraft.core.persistence.model.PlayerData;
import ru.peacecraft.core.persistence.repository.PlayerDataRepository;

public final class PlayerDataService {

    private final PlayerDataRepository repository;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    private volatile boolean shutdown;

    public PlayerDataService(PlayerDataRepository repository) {
        this.repository = repository;
    }

    public PlayerData getOrLoad(UUID uuid) {
        ensureNotShutdown();
        return cache.computeIfAbsent(uuid, repository::load);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return getOrLoad(uuid);
    }

    public PlayerData getPlayerData(Player player) {
        return getOrLoad(player.getUniqueId());
    }

    public boolean isLoaded(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public int getLoadedPlayerCount() {
        return cache.size();
    }

    public void markDirty(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            data.markDirty();
        }
    }

    public void markDirty(Player player) {
        markDirty(player.getUniqueId());
    }

    public boolean save(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) {
            return false;
        }

        boolean saved = repository.save(data);
        if (saved) {
            data.markClean();
        }
        return saved;
    }

    public boolean save(Player player) {
        return save(player.getUniqueId());
    }

    public boolean savePlayer(UUID uuid) {
        return save(uuid);
    }

    public boolean savePlayer(Player player) {
        return save(player);
    }

    public boolean saveIfDirty(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null || !data.isDirty()) {
            return false;
        }

        return save(uuid);
    }

    public int saveAllDirty() {
        int savedCount = 0;
        List<UUID> uuids = new ArrayList<>(cache.keySet());

        for (UUID uuid : uuids) {
            if (saveIfDirty(uuid)) {
                savedCount++;
            }
        }

        return savedCount;
    }

    public int saveAll() {
        int savedCount = 0;
        List<UUID> uuids = new ArrayList<>(cache.keySet());

        for (UUID uuid : uuids) {
            if (save(uuid)) {
                savedCount++;
            }
        }

        return savedCount;
    }

    public void unload(UUID uuid) {
        save(uuid);
        cache.remove(uuid);
    }

    public void unload(Player player) {
        unload(player.getUniqueId());
    }

    public void unloadPlayer(UUID uuid) {
        unload(uuid);
    }

    public void unloadPlayer(Player player) {
        unload(player);
    }

    public void shutdown() {
        if (shutdown) {
            return;
        }

        saveAll();
        cache.clear();
        shutdown = true;
    }

    private void ensureNotShutdown() {
        if (shutdown) {
            throw new IllegalStateException("PlayerDataService is already shut down.");
        }
    }
}
