package ru.peacecraft.core.currencies.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.currencies.model.Currency;
import ru.peacecraft.core.currencies.service.PlayerCurrencyService;
import ru.peacecraft.core.islands.model.IslandData;

public final class CurrencyDropListener implements Listener {

    private final PeaceCraftPlugin plugin;
    private final PlayerCurrencyService currencyService;
    private final Random random = new Random();

    // Drop rates per mob type per island currency
    private final Map<EntityType, Map<String, DropConfig>> dropConfigs = new HashMap<>();

    private static class DropConfig {

        final double minDrop;
        final double maxDrop;
        final double chance;

        DropConfig(double minDrop, double maxDrop, double chance) {
            this.minDrop = minDrop;
            this.maxDrop = maxDrop;
            this.chance = chance;
        }
    }

    public CurrencyDropListener(PeaceCraftPlugin plugin, PlayerCurrencyService currencyService) {
        this.plugin = plugin;
        this.currencyService = currencyService;
        initializeDropConfigs();
    }

    private void initializeDropConfigs() {
        // Starter Island (Scrap)
        Map<String, DropConfig> starterDrops = new HashMap<>();
        starterDrops.put("scrap", new DropConfig(1, 3, 1.0)); // 100% chance
        dropConfigs.put(EntityType.ZOMBIE, starterDrops);
        dropConfigs.put(EntityType.SKELETON, starterDrops);
        dropConfigs.put(EntityType.SPIDER, starterDrops);

        // Orange Island (Clown Nose)
        Map<String, DropConfig> orangeDrops = new HashMap<>();
        orangeDrops.put("clown_nose", new DropConfig(1, 5, 0.5)); // 50% chance
        dropConfigs.put(EntityType.VINDICATOR, orangeDrops); // Кастомный моб или заменить на VINDICATOR
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) {
            return;
        }

        // Определить остров по локации
        IslandData currentIsland = getIslandByLocation(entity.getLocation());
        if (currentIsland == null) {
            return;
        }

        String currencyId = currentIsland.getCurrencyId();
        EntityType entityType = entity.getType();

        Map<String, DropConfig> drops = dropConfigs.get(entityType);
        if (drops == null || !drops.containsKey(currencyId)) {
            return;
        }

        DropConfig config = drops.get(currencyId);

        if (random.nextDouble() > config.chance) {
            return;
        }

        double amount = config.minDrop + (random.nextDouble() * (config.maxDrop - config.minDrop));
        amount = Math.round(amount); // Округляем до целого

        currencyService.addBalance(killer.getUniqueId(), currencyId, amount);

        Currency currency = plugin.getCurrencyService().getCurrency(currencyId);
        if (currency == null) {
            plugin.getLogger().warning(() -> "Currency not found for island drop: " + currencyId);
            return;
        }

        currencyService.addBalance(killer.getUniqueId(), currencyId, amount);

        killer.sendMessage("§6PeaceCraft §8» §aПолучено §e" + (int) amount + " " + currency.getSymbol());
    }

    private IslandData getIslandByLocation(org.bukkit.Location location) {
        for (IslandData island : plugin.getIslandService().getAllIslands()) {
            double dx = location.getX() - island.getCenter().getX();
            double dz = location.getZ() - island.getCenter().getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            // Радиус острова 200 блоков
            if (distance <= 200.0) {
                return island;
            }
        }
        return null;
    }
}
