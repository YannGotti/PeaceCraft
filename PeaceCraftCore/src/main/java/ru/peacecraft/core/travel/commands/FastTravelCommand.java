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

public final class FastTravelCommand extends BaseCommand implements CommandExecutor, TabCompleter {

    private final TravelService travelService;

    public FastTravelCommand(PeaceCraftPlugin plugin, TravelService travelService) {
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
            player.sendMessage("§6PeaceCraft §8» §cИспользование: /fasttravel <остров>");
            return true;
        }

        String targetIslandId = args[0];
        TravelResult result = travelService.fastTravel(player, targetIslandId);

        if (!result.isSuccess()) {
            player.sendMessage("§6PeaceCraft §8» §c" + result.getErrorMessage());
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        if (args.length == 1) {
            return getMatchingUnlockedIslands(sender, args[0]);
        }
        return null;
    }

    private List<String> getMatchingUnlockedIslands(CommandSender sender, String arg) {
        List<String> result = new ArrayList<>();

        if (!(sender instanceof Player player)) {
            return result;
        }

        PlayerData playerData = plugin.getPlayerDataService().getPlayerData(player.getUniqueId());

        for (IslandData island : plugin.getIslandService().getAllIslands()) {
            if (playerData.isIslandUnlocked(island.getId()) && island.getId().toLowerCase().startsWith(arg.toLowerCase())) {
                result.add(island.getId());
            }
        }
        return result;
    }
}
