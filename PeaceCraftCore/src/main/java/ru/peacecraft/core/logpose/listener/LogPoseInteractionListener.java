package ru.peacecraft.core.logpose.listener;

import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.logpose.model.LogPoseTarget;
import ru.peacecraft.core.logpose.service.LogPoseService;
import ru.peacecraft.core.persistence.model.PlayerData;

public final class LogPoseInteractionListener implements Listener {

    private final PeaceCraftPlugin plugin;
    private final LogPoseService logPoseService;

    public LogPoseInteractionListener(PeaceCraftPlugin plugin, LogPoseService logPoseService) {
        this.plugin = plugin;
        this.logPoseService = logPoseService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // 🔴 INFO вместо fine для отладки
        plugin.getLogger().info(() -> "[LogPose] Interact event fired for " + player.getName());

        if (item == null) {
            plugin.getLogger().info("[LogPose] Item is null");
            return;
        }

        if (!logPoseService.hasLogPose(item)) {
            plugin.getLogger().info(() -> "[LogPose] Item is not Log Pose (type: " + item.getType() + ")");
            return;
        }

        plugin.getLogger().info(() -> "[LogPose] Valid Log Pose detected");

        Action action = event.getAction();
        plugin.getLogger().info(() -> "[LogPose] Action: " + action + ", Sneaking: " + player.isSneaking());

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            plugin.getLogger().info(() -> "[LogPose] Right click cancelled");

            if (player.isSneaking()) {
                LogPoseTarget currentTarget = logPoseService.getTargetFromItem(item);
                if (currentTarget != null) {
                    player.sendMessage("§6PeaceCraft §8» §7Цель закреплена: §e" + currentTarget.getDisplayName());
                } else {
                    player.sendMessage("§6PeaceCraft §8» §7Цель не выбрана. Нажмите ПКМ для выбора.");
                }
            } else {
                plugin.getLogger().info("[LogPose] Opening target menu");
                openTargetMenu(player);
            }
            return;
        }

        // ЛКМ — переключение цели
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            plugin.getLogger().info("[LogPose] Left click - cycling target");
            cycleTarget(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        if (newItem != null && logPoseService.hasLogPose(newItem)) {
            LogPoseTarget target = logPoseService.getTargetFromItem(newItem);
            if (target != null && target.getLocation() != null) {
                player.setCompassTarget(target.getLocation());
            }
        }
    }

    private void cycleTarget(Player player) {
        List<LogPoseTarget> targets = logPoseService.getAvailableTargets(player);
        if (targets.isEmpty()) {
            player.sendMessage("§6PeaceCraft §8» §7Нет доступных целей.");
            return;
        }

        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player.getUniqueId());
        String currentTargetId = playerData.getActiveLogPoseTarget();

        // Находим следующую разблокированную цель
        LogPoseTarget nextTarget = null;
        boolean foundCurrent = false;

        for (LogPoseTarget target : targets) {
            if (foundCurrent && target.isUnlocked()) {
                nextTarget = target;
                break;
            }
            if (target.getTargetId().equals(currentTargetId)) {
                foundCurrent = true;
            }
        }

        // Если не нашли следующую, берём первую разблокированную
        if (nextTarget == null) {
            for (LogPoseTarget target : targets) {
                if (target.isUnlocked()) {
                    nextTarget = target;
                    break;
                }
            }
        }

        if (nextTarget != null) {
            logPoseService.setTargetForPlayer(player, nextTarget);
        } else {
            player.sendMessage("§6PeaceCraft §8» §7Нет разблокированных целей.");
        }
    }

    private void openTargetMenu(Player player) {
        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player.getUniqueId());
        List<LogPoseTarget> targets = logPoseService.getAvailableTargets(player);

        plugin.getLogger().info(() -> "[LogPose] Menu: " + targets.size() + " targets available");

        player.sendMessage("");
        player.sendMessage("§6§l=== Доступные цели ===");

        int index = 0;
        for (LogPoseTarget target : targets) {
            index++;
            String status = target.isUnlocked() ? "§a[Открыт]" : "§7[Закрыт]";
            String prefix =
                playerData.getActiveLogPoseTarget() != null && playerData.getActiveLogPoseTarget().equals(target.getTargetId())
                    ? "§e► "
                    : "§8  ";

            player.sendMessage(prefix + index + ". " + target.getDisplayName() + " " + status);
        }

        player.sendMessage("§8========================");
        player.sendMessage("§7Нажмите §eЛКМ §7по Log Pose для переключения цели");
        player.sendMessage("§7Нажмите §eShift+ПКМ §7для закрепления цели");
        player.sendMessage("");
    }
}
