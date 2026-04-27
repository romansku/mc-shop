package org.game24.marketsync.game;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.game24.marketsync.model.Delivery;
import org.game24.marketsync.model.DeliveryResult;
import org.game24.marketsync.model.Item;
import org.game24.marketsync.model.ItemType;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CommandDeliveryService {

    private static final Map<String, CmdType> CMD_PREFIX_TO_TYPE = Map.of(
            "lp ", CmdType.BUKKIT,
            "points ", CmdType.BUKKIT
    );

    private final JavaPlugin plugin;

    public CommandDeliveryService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<DeliveryResult> completeCommandDelivery(@NonNull Delivery delivery, @NonNull Item item) {
        if (item.getType() != ItemType.COMMAND) {
            return CompletableFuture.completedFuture(DeliveryResult.FAILED);
        }

        String username = delivery.getUsername();
        OfflinePlayer player = Bukkit.getOfflinePlayer(username);
        if (!player.hasPlayedBefore()) {
            return CompletableFuture.completedFuture(DeliveryResult.INCOMPLETED);
        }

        String rawCMD = item.getData();
        String[] cmdArray = rawCMD.split(" ");
        String start = cmdArray[0];
        CmdType cmdType = CMD_PREFIX_TO_TYPE.get(start);
        DeliveryResult result = switch (cmdType) {
            case BUKKIT -> bukkitCommand(rawCMD, delivery);
        };

        return CompletableFuture.completedFuture(result)
                .exceptionally(e -> {
                    long orderId = delivery.getOrderId();
                    Long itemId = delivery.getId();
                    plugin.getSLF4JLogger().error(
                            "ItemDelivery error; order: {}, item: {}, user: {}", orderId, itemId, username, e);
                    return DeliveryResult.FAILED;
                });
    }

    private DeliveryResult bukkitCommand(String command, Delivery delivery) {
        String preparedCommand = command.replace("{username}", delivery.getUsername());
        try {

            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), preparedCommand);
            if (success) {
                return DeliveryResult.COMPLETED;
            }

            return DeliveryResult.INCOMPLETED;
        } catch (Exception e) {
            plugin.getSLF4JLogger().error("Error on dispatch bukkit command: {}", preparedCommand, e);
            return DeliveryResult.FAILED;
        }
    }

    private enum CmdType {
        BUKKIT
    }

}
