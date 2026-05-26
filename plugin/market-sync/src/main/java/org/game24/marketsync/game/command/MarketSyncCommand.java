package org.game24.marketsync.game.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.game24.marketsync.MarketSync;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MarketSyncCommand implements BasicCommand {

    private static final String PERMISSION_RELOAD = "marketsync.reload";

    private final MarketSync plugin;

    public MarketSyncCommand(MarketSync plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        var sender = source.getSender();

        if (args.length == 0) {
            sender.sendMessage("Использование: /marketsync reload");
            return;
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission(PERMISSION_RELOAD)) {
                sender.sendMessage("Недостаточно прав.");
                return;
            }

            try {
                plugin.reloadPluginConfig();
            } catch (RuntimeException e) {
                sender.sendMessage("Ошибка перезагрузки конфигурации: " + e.getMessage());
                return;
            }
            sender.sendMessage("Конфигурация MarketSync перезагружена.");
            return;
        }

        sender.sendMessage("Неизвестная подкоманда. Использование: /marketsync reload");
    }


    @Override
    public @NonNull Collection<String> suggest(@NonNull CommandSourceStack source, String[] args) {
        if (args.length == 1 && source.getSender().hasPermission(PERMISSION_RELOAD)) {
            return List.of("reload");
        }
        return Collections.emptyList();
    }
}
