package ru.peacecraft.core.travel.commands;

import java.util.ArrayList;
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
import ru.peacecraft.core.persistence.model.PlayerData;
import ru.peacecraft.core.travel.model.TravelResult;
import ru.peacecraft.core.travel.service.TravelService;

public final class TravelCommand extends BaseCommand implements CommandExecutor, TabCompleter {

    private final TravelService travelService;

    public TravelCommand(PeaceCraftPlugin plugin, TravelService travelService) {
        super(plugin);
        this.travelService = travelService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageService.get("general.player-only"));
            return true;
        }

        if (args.length < 1) {
            sendUsage(player);
            return true;
        }

        String targetIslandId = args[0];

        // Проверка существования острова
        IslandData targetIsland = plugin.getIslandService().getIslandById(targetIslandId);
        if (targetIsland == null) {
            player.sendMessage("§6PeaceCraft §8» §cОстров не найден: " + targetIslandId);
            return true;
        }

        // Попытка путешествия
        TravelResult result = travelService.requestTravel(player, targetIslandId);

        if (!result.isSuccess()) {
            player.sendMessage("§6PeaceCraft §8» §c" + result.getErrorMessage());
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage("§6§l=== Путешествие ===");
        player.sendMessage("§e/travel <остров> §7- Путешествие на остров");
        player.sendMessage("§e/fasttravel <остров> §7- Быстрое путешествие (открытые острова)");
        player.sendMessage("");
        player.sendMessage("§7Доступные острова:");

        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player.getUniqueId());
        for (IslandData island : plugin.getIslandService().getAllIslands()) {
            String status = playerData.isIslandUnlocked(island.getId()) ? "§a[Открыт]" : "§7[Закрыт]";
            player.sendMessage("  §e" + island.getId() + " §8- " + status);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        if (args.length == 1) {
            return getMatchingIslands(args[0]);
        }
        return null;
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
