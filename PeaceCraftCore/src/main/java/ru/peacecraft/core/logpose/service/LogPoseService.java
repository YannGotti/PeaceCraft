package ru.peacecraft.core.logpose.service;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.islands.model.IslandData;
import ru.peacecraft.core.logpose.model.LogPoseSetResult;
import ru.peacecraft.core.logpose.model.LogPoseTarget;
import ru.peacecraft.core.logpose.model.LogPoseTarget.TargetType;
import ru.peacecraft.core.persistence.model.PlayerData;

public final class LogPoseService {

    private final PeaceCraftPlugin plugin;
    private final NamespacedKey targetKey;
    private final NamespacedKey targetTypeKey;

    public LogPoseService(PeaceCraftPlugin plugin) {
        this.plugin = plugin;
        this.targetKey = new NamespacedKey(plugin, "logpose_target");
        this.targetTypeKey = new NamespacedKey(plugin, "logpose_type");
    }

    public ItemStack createLogPoseItem() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName("§6§lLog Pose §8» §7Навигатор");
        List<String> lore = new ArrayList<>();
        lore.add("§7Ваш главный навигационный инструмент.");
        lore.add("");
        lore.add("§8▪ §7ПКМ §8— §7Открыть меню целей");
        lore.add("§8▪ §7Shift+ПКМ §8— §7Закрепить цель");
        lore.add("§8▪ §7ЛКМ §8— §7Переключить цель");
        lore.add("");
        lore.add("§eТекущая цель: §7Не выбрана");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);

        meta.getPersistentDataContainer().set(targetKey, PersistentDataType.STRING, "none");
        meta.getPersistentDataContainer().set(targetTypeKey, PersistentDataType.STRING, TargetType.ISLAND.name());

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createLogPoseWithTarget(Player player, LogPoseTarget target) {
        ItemStack item = createLogPoseItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Обновляем lore с текущей целью
        List<String> lore = meta.getLore();
        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i).contains("Текущая цель:")) {
                    lore.set(i, "§eТекущая цель: §7" + target.getDisplayName());
                    break;
                }
            }
            meta.setLore(lore);
        }

        // Сохраняем метаданные
        meta.getPersistentDataContainer().set(targetKey, PersistentDataType.STRING, target.getTargetId());
        meta.getPersistentDataContainer().set(targetTypeKey, PersistentDataType.STRING, target.getType().name());
        item.setItemMeta(meta);

        return item;
    }

    public LogPoseTarget getTargetFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        String targetId = meta.getPersistentDataContainer().get(targetKey, PersistentDataType.STRING);
        String typeName = meta.getPersistentDataContainer().get(targetTypeKey, PersistentDataType.STRING);

        if (targetId == null || typeName == null) return null;

        try {
            LogPoseTarget.TargetType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return null;
        }

        IslandData island = plugin.getIslandService().getIslandById(targetId);
        if (island == null) return null;

        return LogPoseTarget.fromIsland(island, true);
    }

    public void setTargetForPlayer(Player player, LogPoseTarget target) {
        // Сохраняем в данные игрока
        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player.getUniqueId());
        playerData.setActiveLogPoseTarget(target.getTargetId());
        plugin.getPlayerDataService().savePlayer(player);

        // Обновляем предмет в основной руке
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.COMPASS && hasLogPose(item)) {
            ItemStack newItem = createLogPoseWithTarget(player, target);
            player.getInventory().setItemInMainHand(newItem);
        }

        // Устанавливаем компас на цель (ванильная механика)
        if (target.getLocation() != null) {
            player.setCompassTarget(target.getLocation());
        }

        // Отправляем сообщение
        player.sendMessage("§6PeaceCraft §8» §aЦель установлена: §e" + target.getDisplayName());
    }

    public List<LogPoseTarget> getAvailableTargets(Player player) {
        List<LogPoseTarget> targets = new ArrayList<>();
        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player.getUniqueId());

        for (IslandData island : plugin.getIslandService().getAllIslands()) {
            boolean unlocked = playerData.isIslandUnlocked(island.getId());
            targets.add(LogPoseTarget.fromIsland(island, unlocked));
        }

        return targets;
    }

    public boolean hasLogPose(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(targetKey, PersistentDataType.STRING);
    }

    public boolean playerHasLogPose(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        if (contents == null) return false;
        for (ItemStack item : contents) {
            if (hasLogPose(item)) return true;
        }
        return false;
    }

    public void giveLogPose(Player player) {
        if (playerHasLogPose(player)) {
            player.sendMessage("§6PeaceCraft §8» §7У вас уже есть Log Pose!");
            return;
        }

        ItemStack logPose = createLogPoseItem();
        player.getInventory().addItem(logPose);
        player.sendMessage("§6PeaceCraft §8» §aВы получили Log Pose!");
    }

    public PeaceCraftPlugin getPlugin() {
        return plugin;
    }

    public LogPoseSetResult setTargetByIslandId(Player player, String islandId) {
        IslandData island = plugin.getIslandService().getIslandById(islandId);
        if (island == null) {
            return LogPoseSetResult.fail("§cОстров не найден: " + islandId);
        }

        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player);
        if (!playerData.isIslandUnlocked(islandId) && !island.getId().equals(plugin.getMainConfigManager().getStarterIslandId())) {
            return LogPoseSetResult.fail("§cЭтот остров ещё не открыт.");
        }

        LogPoseTarget target = LogPoseTarget.fromIsland(island, true);
        setTargetForPlayer(player, target);
        plugin.getPlayerDataService().markDirty(player);
        plugin.getPlayerDataService().save(player);

        return LogPoseSetResult.ok("§aЦель Log Pose установлена: §6" + island.getDisplayName());
    }

    public void resetTarget(Player player) {
        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player);
        playerData.setActiveLogPoseTarget(null);
        plugin.getPlayerDataService().save(player);
    }
}
