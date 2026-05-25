package org.game24.marketsync.game.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.game24.marketsync.MarketSync;

import java.util.Collections;
import java.util.List;

public class MarketSyncCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION_RELOAD = "marketsync.reload";

    private final MarketSync plugin;

    public MarketSyncCommand(MarketSync plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Использование: /" + label + " reload");
            return true;
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission(PERMISSION_RELOAD)) {
                sender.sendMessage("Недостаточно прав.");
                return true;
            }

            plugin.reloadPluginConfig();
            sender.sendMessage("Конфигурация MarketSync перезагружена.");
            return true;
        }

        sender.sendMessage("Неизвестная подкоманда. Использование: /" + label + " reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission(PERMISSION_RELOAD)) {
            return List.of("reload");
        }
        return Collections.emptyList();
    }
}
