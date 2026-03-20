package ru.peacecraft.core.commands.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
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
import ru.peacecraft.core.persistence.model.PlayerData;

public final class IslandAdminCommand extends BaseCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION_PREFIX = "peacecraft.admin.island.";

    public IslandAdminCommand(PeaceCraftPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!hasPermission(sender, PERMISSION_PREFIX + "manage")) {
            sendNoPermission(sender);
            return true;
        }

        if (args.length < 1) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "unlock" -> handleUnlock(sender, args);
            case "discover" -> handleDiscover(sender, args);
            case "setcurrent" -> handleSetCurrent(sender, args);
            case "info" -> handleInfo(sender, args);
            case "reload" -> handleReload(sender);
            default -> {
                sendUsage(sender);
                yield true;
            }
        };
    }

    private boolean handleUnlock(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /pc island unlock <игрок> <остров>");
            return true;
        }

        String playerName = args[1];
        String islandId = args[2];

        Player targetPlayer = Bukkit.getPlayer(playerName);
        UUID targetUuid;

        if (targetPlayer != null) {
            targetUuid = targetPlayer.getUniqueId();
        } else {
            try {
                targetUuid = UUID.fromString(playerName);
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cИгрок не найден: " + playerName);
                return true;
            }
        }

        if (!plugin.getIslandService().islandExists(islandId)) {
            sender.sendMessage("§cОстров не найден: " + islandId);
            return true;
        }

        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(targetUuid);
        playerData.addUnlockedIsland(islandId);
        plugin.getPlayerDataService().savePlayer(targetUuid);

        sender.sendMessage("§aОстров §6" + islandId + "§a разблокирован для §6" + playerName);

        if (targetPlayer != null) {
            targetPlayer.sendMessage(messageService.get("general.data-saved"));
        }

        return true;
    }

    private boolean handleDiscover(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /pc island discover <игрок> <остров>");
            return true;
        }

        String playerName = args[1];
        String islandId = args[2];

        Player targetPlayer = Bukkit.getPlayer(playerName);
        UUID targetUuid;

        if (targetPlayer != null) {
            targetUuid = targetPlayer.getUniqueId();
        } else {
            try {
                targetUuid = UUID.fromString(playerName);
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cИгрок не найден: " + playerName);
                return true;
            }
        }

        if (!plugin.getIslandService().islandExists(islandId)) {
            sender.sendMessage("§cОстров не найден: " + islandId);
            return true;
        }

        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(targetUuid);
        playerData.addDiscoveredIsland(islandId);
        plugin.getPlayerDataService().savePlayer(targetUuid);

        sender.sendMessage("§aОстров §6" + islandId + "§a отмечен как обнаруженный для §6" + playerName);

        return true;
    }

    private boolean handleSetCurrent(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /pc island setcurrent <игрок> <остров>");
            return true;
        }

        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }

        String islandId = args[2];

        if (!plugin.getIslandService().islandExists(islandId)) {
            sender.sendMessage("§cОстров не найден: " + islandId);
            return true;
        }

        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player.getUniqueId());
        playerData.setCurrentIslandId(islandId);
        plugin.getPlayerDataService().savePlayer(player);

        sender.sendMessage("§aТекущий остров установлен: §6" + islandId);

        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /pc island info <остров>");
            return true;
        }

        String islandId = args[1];
        IslandData islandData = plugin.getIslandService().getIslandById(islandId);

        if (islandData == null) {
            sender.sendMessage("§cОстров не найден: " + islandId);
            return true;
        }

        sender.sendMessage("§6=== Информация об острове ===");
        sender.sendMessage("§eID: §f" + islandData.getId());
        sender.sendMessage("§eНазвание: §f" + islandData.getDisplayName());
        sender.sendMessage("§eМир: §f" + islandData.getWorldName());
        sender.sendMessage("§eРегион: §f" + islandData.getRegionId());
        sender.sendMessage("§eВалюта: §f" + islandData.getCurrencyId());
        sender.sendMessage("§eТребование: §f" + islandData.getUnlockRequirement());
        sender.sendMessage(
            "§eЦентр: §f" + islandData.getCenter().getX() + ", " + islandData.getCenter().getY() + ", " + islandData.getCenter().getZ()
        );

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        plugin.getIslandsConfig().reload();
        sender.sendMessage("§aКонфигурация островов перезагружена.");
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§6=== PeaceCraft Island Admin ===");
        sender.sendMessage("§e/pc island unlock <игрок> <остров> §7- Разблокировать остров");
        sender.sendMessage("§e/pc island discover <игрок> <остров> §7- Отметить как обнаруженный");
        sender.sendMessage("§e/pc island setcurrent <игрок> <остров> §7- Установить текущий остров");
        sender.sendMessage("§e/pc island info <остров> §7- Информация об острове");
        sender.sendMessage("§e/pc island reload §7- Перезагрузить конфиг");
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        if (args.length == 1) {
            return getMatchingSubCommands(Arrays.asList("unlock", "discover", "setcurrent", "info", "reload"), args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("info")) {
                return getMatchingIslands(args[1]);
            }
            return null;
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("unlock") || args[0].equalsIgnoreCase("discover") || args[0].equalsIgnoreCase("setcurrent")) {
                return getMatchingIslands(args[2]);
            }
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
