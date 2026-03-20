package ru.peacecraft.core.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.message.MessageService;

public abstract class BaseCommand {

    protected final PeaceCraftPlugin plugin;
    protected final MessageService messageService;

    protected BaseCommand(PeaceCraftPlugin plugin) {
        this.plugin = plugin;
        this.messageService = plugin.getMessageService();
    }

    protected boolean hasPermission(CommandSender sender, String permission) {
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        return sender.hasPermission(permission);
    }

    protected boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    protected Player requirePlayer(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageService.get("general.player-only"));
            return null;
        }
        return player;
    }

    protected void sendNoPermission(CommandSender sender) {
        if (messageService != null) {
            sender.sendMessage(messageService.get("general.no-permission"));
        } else {
            sender.sendMessage("§6PeaceCraft §8» §cУ тебя нет прав.");
        }
    }
}
