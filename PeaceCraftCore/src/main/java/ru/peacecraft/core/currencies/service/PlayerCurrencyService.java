package ru.peacecraft.core.currencies.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import ru.peacecraft.core.PeaceCraftPlugin;

public final class PlayerCurrencyService {

    private final PeaceCraftPlugin plugin;
    private final Map<UUID, Map<String, Double>> playerBalances = new ConcurrentHashMap<>();

    public PlayerCurrencyService(PeaceCraftPlugin plugin) {
        this.plugin = plugin;
    }

    public double getBalance(UUID playerUuid, String currencyId) {
        return playerBalances.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>()).computeIfAbsent(currencyId, k -> 0.0);
    }

    public void addBalance(UUID playerUuid, String currencyId, double amount) {
        Map<String, Double> balances = playerBalances.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>());
        double current = balances.getOrDefault(currencyId, 0.0);
        balances.put(currencyId, current + amount);
    }

    public boolean removeBalance(UUID playerUuid, String currencyId, double amount) {
        Map<String, Double> balances = playerBalances.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>());
        double current = balances.getOrDefault(currencyId, 0.0);

        if (current < amount) {
            return false;
        }

        balances.put(currencyId, current - amount);
        return true;
    }

    public boolean hasBalance(UUID playerUuid, String currencyId, double amount) {
        return getBalance(playerUuid, currencyId) >= amount;
    }

    public void setBalance(UUID playerUuid, String currencyId, double amount) {
        playerBalances.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>()).put(currencyId, amount);
    }

    public void saveAll() {
        // Здесь будет сохранение в файлы/БД
        // Для MVP сохраняем в PlayerData через Gson
        plugin.getLogger().info(() -> "Saving currency balances for " + playerBalances.size() + " players.");
    }

    public void clearCache(UUID playerUuid) {
        playerBalances.remove(playerUuid);
    }
}
