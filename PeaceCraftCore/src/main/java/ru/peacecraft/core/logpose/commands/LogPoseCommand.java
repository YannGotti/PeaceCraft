package ru.peacecraft.core.logpose.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.commands.BaseCommand;
import ru.peacecraft.core.islands.model.IslandData;
import ru.peacecraft.core.logpose.model.LogPoseTarget;
import ru.peacecraft.core.logpose.service.LogPoseService;

public final class LogPoseCommand extends BaseCommand implements CommandExecutor, TabCompleter {

    private final LogPoseService logPoseService;

    public LogPoseCommand(PeaceCraftPlugin plugin, LogPoseService logPoseService) {
        super(plugin);
        this.logPoseService = logPoseService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageService.get("general.player-only"));
            return true;
        }

        if (args.length == 0) {
            // /logpose — выдать предмет
            logPoseService.giveLogPose(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "set" -> handleSet(player, args);
            case "reset" -> handleReset(player);
            case "info" -> handleInfo(player);
            default -> {
                sendUsage(player);
                yield true;
            }
        };
    }

    private boolean handleSet(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /logpose set <остров>");
            return true;
        }

        String islandId = args[1];
        IslandData island = plugin.getIslandService().getIslandById(islandId);

        if (island == null) {
            player.sendMessage("§cОстров не найден: " + islandId);
            return true;
        }

        LogPoseTarget target = LogPoseTarget.fromIsland(island, true);
        logPoseService.setTargetForPlayer(player, target);
        player.sendMessage("§aЦель Log Pose установлена: §6" + island.getDisplayName());

        return true;
    }

    private boolean handleReset(Player player) {
        plugin.getPlayerDataService().getPlayerData(player.getUniqueId()).setActiveLogPoseTarget(null);
        player.sendMessage("§aЦель Log Pose сброшена.");
        return true;
    }

    private boolean handleInfo(Player player) {
        LogPoseTarget currentTarget = logPoseService.getTargetFromItem(player.getInventory().getItemInMainHand());

        if (currentTarget == null) {
            player.sendMessage("§7У вас нет активной цели Log Pose.");
            return true;
        }

        player.sendMessage("§6§l=== Информация Log Pose ===");
        player.sendMessage("§eЦель: §7" + currentTarget.getDisplayName());
        player.sendMessage("§eТип: §7" + currentTarget.getType());
        player.sendMessage(
            "§eКоординаты: §7" +
                currentTarget.getLocation().getBlockX() +
                ", " +
                currentTarget.getLocation().getBlockY() +
                ", " +
                currentTarget.getLocation().getBlockZ()
        );
        player.sendMessage("§eСтатус: §7" + (currentTarget.isUnlocked() ? "§aОткрыт" : "§cЗакрыт"));

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage("§6§l=== Log Pose Команды ===");
        player.sendMessage("§e/logpose §7- Получить Log Pose");
        player.sendMessage("§e/logpose set <остров> §7- Установить цель");
        player.sendMessage("§e/logpose reset §7- Сбросить цель");
        player.sendMessage("§e/logpose info §7- Информация о текущей цели");
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        if (args.length == 1) {
            return getMatchingSubCommands(Arrays.asList("set", "reset", "info"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return getMatchingIslands(args[1]);
        }

        return null;
    }

    private List<String> getMatchingSubCommands(List<String> subCommands, String arg) {
        List<String> result = new ArrayList<>();
        for (String subCommand : subCommands) {
            if (subCommand.toLowerCase().startsWith(arg.toLowerCase())) {
                result.add(subCommand);
            }
        }
        return result;
    }

    private List<String> getMatchingIslands(String arg) {
        List<String> result = new ArrayList<>();
        for (IslandData island : plugin.getIslandService().getAllIslands()) {
            if (island.getId().toLowerCase().startsWith(arg.toLowerCase())) {
                result.add(island.getId());
            }
        }
        return result;
    }
}
