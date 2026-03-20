package ru.peacecraft.core.travel.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.islands.model.IslandData;
import ru.peacecraft.core.persistence.model.PlayerData;
import ru.peacecraft.core.travel.model.TravelRequest;
import ru.peacecraft.core.travel.model.TravelResult;

public final class TravelService {

    private final PeaceCraftPlugin plugin;
    private final Map<UUID, Long> travelCooldowns;
    private final long cooldownMillis;
    private final int travelDelayTicks;

    public TravelService(PeaceCraftPlugin plugin) {
        this.plugin = plugin;
        this.travelCooldowns = new ConcurrentHashMap<>();
        this.cooldownMillis = 30000; // 30 секунд кулдаун
        this.travelDelayTicks = 60; // 3 секунды задержка путешествия
    }

    /**
     * Инициировать путешествие игрока
     */
    public TravelResult requestTravel(Player player, String targetIslandId) {
        // Проверка кулдауна
        if (isOnCooldown(player.getUniqueId())) {
            return TravelResult.failure(
                TravelResult.TravelErrorCode.ON_COOLDOWN,
                "Путешествие доступно через " + getCooldownRemaining(player.getUniqueId()) + " сек."
            );
        }

        // Получить текущий остров игрока
        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player.getUniqueId());
        String currentIslandId = playerData.getCurrentIslandId();

        // Проверка целевого острова
        IslandData targetIsland = plugin.getIslandService().getIslandById(targetIslandId);
        if (targetIsland == null) {
            return TravelResult.failure(TravelResult.TravelErrorCode.ISLAND_NOT_FOUND, "Остров не найден: " + targetIslandId);
        }

        // Проверка доступа к острову
        if (!playerData.isIslandUnlocked(targetIslandId) && !targetIsland.getUnlockRequirement().equals("none")) {
            return TravelResult.failure(
                TravelResult.TravelErrorCode.ISLAND_NOT_UNLOCKED,
                "Остров ещё не открыт! Выполните требования: " + targetIsland.getUnlockRequirement()
            );
        }

        // Нельзя путешествовать на тот же остров
        if (java.util.Objects.equals(currentIslandId, targetIslandId)) {
            return TravelResult.failure(TravelResult.TravelErrorCode.INVALID_PORT, "Вы уже находитесь на этом острове");
        }

        // Создать запрос на путешествие
        TravelRequest request = new TravelRequest(
            player.getUniqueId(),
            currentIslandId,
            targetIslandId,
            TravelRequest.TravelType.COMMAND_TRAVEL
        );

        // Запустить процесс путешествия
        startTravelProcess(player, request);

        return TravelResult.success();
    }

    /**
     * Запустить процесс путешествия
     */
    private void startTravelProcess(Player player, TravelRequest request) {
        Player targetPlayer = Bukkit.getPlayer(request.getPlayerId());
        if (targetPlayer == null) return;

        targetPlayer.sendMessage("§6PeaceCraft §8» §7Отплытие на §e" + request.getToIslandId() + "§7...");
        targetPlayer.sendMessage("§8[§e■■■■■§8] §7Пожалуйста, не двигайтесь!");

        playTravelEffect(targetPlayer.getLocation(), Particle.SMOKE, 20);
        targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_BOAT_PADDLE_WATER, 1.0f, 1.0f);

        plugin
            .getServer()
            .getScheduler()
            .runTaskLater(
                plugin,
                () -> {
                    completeTravel(targetPlayer, request);
                },
                travelDelayTicks
            );

        // Установить кулдаун
        travelCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Завершить путешествие (телепортация)
     */
    private void completeTravel(Player player, TravelRequest request) {
        IslandData targetIsland = plugin.getIslandService().getIslandById(request.getToIslandId());
        if (targetIsland == null) return;

        // Телепортация на порт целевого острова
        Location spawnLocation = new Location(
            Bukkit.getWorld(targetIsland.getWorldName()),
            targetIsland.getSpawn().getX(),
            targetIsland.getSpawn().getY(),
            targetIsland.getSpawn().getZ()
        );

        player.teleport(spawnLocation);

        // Эффект прибытия
        playTravelEffect(player.getLocation(), Particle.CLOUD, 30);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        // Обновить текущий остров в данных игрока
        plugin.getIslandService().setCurrentIsland(player.getUniqueId(), request.getToIslandId());
        plugin.getPlayerDataService().save(player);

        // Сообщение о прибытии
        player.sendMessage("§6PeaceCraft §8» §aВы прибыли на §e" + targetIsland.getDisplayName() + "§a!");
        player.sendMessage("§7Исследуйте остров и найдите сокровища!");
    }

    /**
     * Проверка кулдауна
     */
    private boolean isOnCooldown(UUID playerId) {
        if (!travelCooldowns.containsKey(playerId)) return false;
        return System.currentTimeMillis() - travelCooldowns.get(playerId) < cooldownMillis;
    }

    /**
     * Оставшееся время кулдауна
     */
    private int getCooldownRemaining(UUID playerId) {
        if (!travelCooldowns.containsKey(playerId)) return 0;
        long elapsed = System.currentTimeMillis() - travelCooldowns.get(playerId);
        return (int) Math.max(0, (cooldownMillis - elapsed) / 1000);
    }

    /**
     * Эффект путешествия
     */
    private void playTravelEffect(Location location, Particle particle, int count) {
        location.getWorld().spawnParticle(particle, location, count, 1, 1, 1, 0);
    }

    /**
     * Быстрое путешествие (для открытых островов)
     */
    public TravelResult fastTravel(Player player, String targetIslandId) {
        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player.getUniqueId());

        if (!playerData.isIslandUnlocked(targetIslandId)) {
            return TravelResult.failure(
                TravelResult.TravelErrorCode.ISLAND_NOT_UNLOCKED,
                "Быстрое путешествие доступно только на открытые острова!"
            );
        }

        IslandData targetIsland = plugin.getIslandService().getIslandById(targetIslandId);
        if (targetIsland == null) {
            return TravelResult.failure(TravelResult.TravelErrorCode.ISLAND_NOT_FOUND, "Остров не найден: " + targetIslandId);
        }

        Location spawnLocation = new Location(
            Bukkit.getWorld(targetIsland.getWorldName()),
            targetIsland.getSpawn().getX(),
            targetIsland.getSpawn().getY(),
            targetIsland.getSpawn().getZ()
        );

        player.teleport(spawnLocation);

        playerData.setCurrentIslandId(targetIslandId);
        plugin.getPlayerDataService().savePlayer(player);

        player.sendMessage("§6PeaceCraft §8» §aБыстрое путешествие на §e" + targetIsland.getDisplayName() + "§a!");

        return TravelResult.success();
    }
}
