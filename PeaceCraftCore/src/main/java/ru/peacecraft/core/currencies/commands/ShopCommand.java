package ru.peacecraft.core.currencies.commands;

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
import ru.peacecraft.core.currencies.shop.ShopGUI;

public final class ShopCommand extends BaseCommand implements CommandExecutor, TabCompleter {

    public ShopCommand(PeaceCraftPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageService.get("general.player-only"));
            return true;
        }

        if (args.length == 0) {
            // Открыть основной магазин
            openShop(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "sell" -> handleSell(player);
            case "balance" -> handleBalance(player);
            default -> {
                sendUsage(player);
                yield true;
            }
        };
    }

    private void openShop(Player player) {
        // Здесь нужно загрузить товары для текущего острова игрока
        List<ru.peacecraft.core.currencies.model.ShopItem> items = new ArrayList<>();
        // TODO: Загрузить из конфига shops.yml

        ShopGUI gui = ShopGUI.create(items);
        gui.open(player);
    }

    private boolean handleSell(Player player) {
        // Продажа предметов из инвентаря
        player.sendMessage("§6PeaceCraft §8» §7Функция продажи в разработке...");
        return true;
    }

    private boolean handleBalance(Player player) {
        // Показать баланс игрока
        player.sendMessage("§6PeaceCraft §8» §7Ваш баланс:");
        // TODO: Показать баланс по каждой валюте
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage("§6§l=== Магазин ===");
        player.sendMessage("§e/shop §7- Открыть магазин");
        player.sendMessage("§e/shop sell §7- Продать предметы");
        player.sendMessage("§e/shop balance §7- Показать баланс");
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        if (args.length == 1) {
            return getMatchingSubCommands(Arrays.asList("sell", "balance"), args[0]);
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
}
