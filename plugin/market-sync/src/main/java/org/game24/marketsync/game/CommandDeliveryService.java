package org.game24.marketsync.game;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.game24.marketsync.game.hook.LuckPermsHook;
import org.game24.marketsync.model.Delivery;
import org.game24.marketsync.model.DeliveryResult;
import org.game24.marketsync.model.Item;
import org.game24.marketsync.model.ItemType;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CommandDeliveryService {

    private final Logger logger;

    private final LuckPermsHook luckPermsHook;

    public CommandDeliveryService(JavaPlugin plugin) {
        this.logger = plugin.getSLF4JLogger();
        this.luckPermsHook = new LuckPermsHook(plugin);
    }

    public CompletableFuture<DeliveryResult> completeCommandDelivery(@NonNull Delivery delivery, @NonNull Item item) {
        if (item.getType() != ItemType.COMMAND) {
            return CompletableFuture.completedFuture(DeliveryResult.FAILED);
        }

        Long deliveryId = delivery.getId();
        long orderId = delivery.getOrderId();
        long itemId = delivery.getItemId();

        String username = delivery.getUsername();
        OfflinePlayer player = Bukkit.getOfflinePlayer(username);
        if (!player.hasPlayedBefore()) {
            return CompletableFuture.completedFuture(DeliveryResult.INCOMPLETED);
        }

        // lp user {player} permission settemp mshop.nickname.prefix.text true 365d
        // prefix;;365;;DAYS
        String rawCMD = item.getData();


        String[] cmdArray = rawCMD.split(";;");
        String apiType = cmdArray[0];
        CompletableFuture<DeliveryResult> future = switch (apiType) {
            case "LP" -> switch (cmdArray[1]) {
                case "perm" -> {
                    String permission = cmdArray[2];
                    yield luckPermsHook.addPermission(username, permission, 180);
                }
                case "group" -> {
                    String group = cmdArray[2];
                    String daysStr = cmdArray[3];
                    long days = Long.parseLong(daysStr);
                    yield luckPermsHook.addGroup(username, group, days);
                }
                default -> {
                    logger.error(
                            "ItemDelivery LP command; cmd: {}; order: {}, item: {}, user: {}, delivery: {}",
                            rawCMD, orderId, itemId, username, deliveryId);
                    yield CompletableFuture.completedFuture(DeliveryResult.FAILED);
                }
            };
            case "PP" -> {

                //todo: not implemented
                yield CompletableFuture.completedFuture(DeliveryResult.FAILED);
            }
            default -> {
                logger.error(
                        "ItemDelivery unknown API type; cmd: {}; order: {}, item: {}, user: {}, delivery: {}",
                        rawCMD, orderId, itemId, username, deliveryId);
                yield CompletableFuture.completedFuture(DeliveryResult.FAILED);
            }
        };

        return future
                .orTimeout(10, TimeUnit.SECONDS)
                .exceptionally(e -> {
                    logger.error(
                            "ItemDelivery error; order: {}, item: {}, user: {}, delivery: {}",
                            orderId, itemId, username, deliveryId, e);
                    return DeliveryResult.FAILED;
                })
                .thenApply((r) -> {
                    logger.info(
                            "ItemDelivery result: {}; order: {}, item: {}, user: {}, delivery: {}",
                            r, orderId, itemId, username, deliveryId);
                    return r;
                });
    }

}
