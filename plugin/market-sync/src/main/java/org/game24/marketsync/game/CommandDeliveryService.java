package org.game24.marketsync.game;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.game24.marketsync.game.hook.LuckPermsHook;
import org.game24.marketsync.game.hook.PlayerPointsHook;
import org.game24.marketsync.model.Delivery;
import org.game24.marketsync.model.DeliveryResult;
import org.game24.marketsync.model.Item;
import org.game24.marketsync.model.ItemType;
import org.slf4j.Logger;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CommandDeliveryService {

    private final Logger logger;

    private final LuckPermsHook luckPermsHook;

    private final PlayerPointsHook playerPointsHook;

    public CommandDeliveryService(JavaPlugin plugin) {
        this.logger = plugin.getSLF4JLogger();
        this.luckPermsHook = new LuckPermsHook(plugin);
        this.playerPointsHook = new PlayerPointsHook(plugin);
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

        // LP;;perm;;mshop.nickname.prefix.color;;6;;MONTHS
        // LP;;group;;vip;;7;;DAYS
        // PP;;give;;500
        String rawCMD = item.getData();


        String[] cmdArray = rawCMD.split(";;");
        String apiType = cmdArray[0];
        final String cmdType = cmdArray[1];
        CompletableFuture<DeliveryResult> future = switch (apiType) {
            case "LP" -> switch (cmdType) {
                case "perm" -> {
                    String permission = cmdArray[2];
                    long duration = Long.parseLong(cmdArray[3]);
                    ChronoUnit unit = ChronoUnit.valueOf(cmdArray[4]);
                    yield luckPermsHook.addPermission(username, permission, duration, unit);
                }
                case "group" -> {
                    String group = cmdArray[2];
                    long duration = Long.parseLong(cmdArray[3]);
                    ChronoUnit unit = ChronoUnit.valueOf(cmdArray[4]);
                    yield luckPermsHook.addGroup(username, group, duration, unit);
                }
                default -> {
                    logger.error(
                            "ItemDelivery LP command unsupported; cmd: {}; order: {}, item: {}, user: {}, delivery: {}",
                            rawCMD, orderId, itemId, username, deliveryId);
                    yield CompletableFuture.completedFuture(DeliveryResult.FAILED);
                }
            };
            case "PP" -> {
                    if (cmdType.equals("give")) {
                        String amountStr = cmdArray[2];
                        int amount = Integer.parseInt(amountStr);
                        yield playerPointsHook.givePoints(username, amount);
                    }

                    logger.error(
                            "ItemDelivery PP command unsupported; cmd: {}; order: {}, item: {}, user: {}, delivery: {}",
                            rawCMD, orderId, itemId, username, deliveryId);
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
