package ru.peacecraft.core.commands.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

public final class IslandAdminCommand extends BaseCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "peacecraft.admin.island.manage";

    public IslandAdminCommand(PeaceCraftPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!hasPermission(sender, PERMISSION)) {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("island")) {
            sendUsage(sender);
            return true;
        }

        if (args.length == 1) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[1].toLowerCase();

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
        if (args.length < 4) {
            sender.sendMessage("§cИспользование: /pc island unlock <игрок> <остров>");
            return true;
        }

        TargetPlayer target = resolveTarget(args[2]);
        if (target == null) {
            sender.sendMessage("§cИгрок не найден: " + args[2]);
            return true;
        }

        String islandId = args[3];
        if (!plugin.getIslandService().islandExists(islandId)) {
            sender.sendMessage("§cОстров не найден: " + islandId);
            return true;
        }

        boolean changed = plugin.getIslandService().unlockIsland(target.uuid(), islandId);
        plugin.getPlayerDataService().save(target.uuid());

        if (changed) {
            sender.sendMessage("§aОстров §6" + islandId + "§a разблокирован для §6" + target.name());
        } else {
            sender.sendMessage("§eОстров §6" + islandId + "§e уже был разблокирован для §6" + target.name());
        }

        Player onlinePlayer = target.onlinePlayer();
        if (onlinePlayer != null) {
            onlinePlayer.sendMessage("§6PeaceCraft §8» §aТебе разблокировали остров: §e" + islandId);
        }

        return true;
    }

    private boolean handleDiscover(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cИспользование: /pc island discover <игрок> <остров>");
            return true;
        }

        TargetPlayer target = resolveTarget(args[2]);
        if (target == null) {
            sender.sendMessage("§cИгрок не найден: " + args[2]);
            return true;
        }

        String islandId = args[3];
        if (!plugin.getIslandService().islandExists(islandId)) {
            sender.sendMessage("§cОстров не найден: " + islandId);
            return true;
        }

        boolean changed = plugin.getIslandService().discoverIsland(target.uuid(), islandId);
        plugin.getPlayerDataService().save(target.uuid());

        if (changed) {
            sender.sendMessage("§aОстров §6" + islandId + "§a отмечен как обнаруженный для §6" + target.name());
        } else {
            sender.sendMessage("§eОстров §6" + islandId + "§e уже был обнаружен у §6" + target.name());
        }

        return true;
    }

    private boolean handleSetCurrent(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cИспользование: /pc island setcurrent <игрок> <остров>");
            return true;
        }

        TargetPlayer target = resolveTarget(args[2]);
        if (target == null) {
            sender.sendMessage("§cИгрок не найден: " + args[2]);
            return true;
        }

        String islandId = args[3];
        if (!plugin.getIslandService().islandExists(islandId)) {
            sender.sendMessage("§cОстров не найден: " + islandId);
            return true;
        }

        boolean changed = plugin.getIslandService().setCurrentIsland(target.uuid(), islandId);
        plugin.getPlayerDataService().save(target.uuid());

        if (changed) {
            sender.sendMessage("§aТекущий остров для §6" + target.name() + "§a установлен: §6" + islandId);
        } else {
            sender.sendMessage("§eУ §6" + target.name() + "§e уже установлен текущий остров: §6" + islandId);
        }

        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /pc island info <остров>");
            return true;
        }

        String islandId = args[2];
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
            return getMatching(Arrays.asList("island"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("island")) {
            return getMatching(Arrays.asList("unlock", "discover", "setcurrent", "info", "reload"), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("island")) {
            if (args[1].equalsIgnoreCase("info")) {
                return getMatchingIslands(args[2]);
            }
            return null;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("island")) {
            if (args[1].equalsIgnoreCase("unlock") || args[1].equalsIgnoreCase("discover") || args[1].equalsIgnoreCase("setcurrent")) {
                return getMatchingIslands(args[3]);
            }
        }

        return null;
    }

    private List<String> getMatching(List<String> values, String arg) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase().startsWith(arg.toLowerCase())) {
                result.add(value);
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

    private @Nullable TargetPlayer resolveTarget(String input) {
        Player online = Bukkit.getPlayerExact(input);
        if (online != null) {
            return new TargetPlayer(online.getUniqueId(), online.getName(), online);
        }

        try {
            UUID uuid = UUID.fromString(input);
            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            String name = offline.getName() != null ? offline.getName() : uuid.toString();
            return new TargetPlayer(uuid, name, online);
        } catch (IllegalArgumentException ignored) {}

        OfflinePlayer offline = Bukkit.getOfflinePlayer(input);
        if (offline.getUniqueId() == null) {
            return null;
        }

        return new TargetPlayer(offline.getUniqueId(), offline.getName() != null ? offline.getName() : input, null);
    }

    private record TargetPlayer(UUID uuid, String name, @Nullable Player onlinePlayer) {}
}
