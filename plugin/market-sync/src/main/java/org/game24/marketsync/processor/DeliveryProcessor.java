package org.game24.marketsync.processor;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.game24.marketsync.game.CommandDeliveryService;
import org.game24.marketsync.game.ItemDeliveryService;
import org.game24.marketsync.model.Delivery;
import org.game24.marketsync.model.DeliveryResult;
import org.game24.marketsync.model.Item;
import org.game24.marketsync.model.ItemType;
import org.game24.marketsync.service.DeliveryService;
import org.game24.marketsync.service.ItemService;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class DeliveryProcessor {

    private final JavaPlugin plugin;

    private final DeliveryService deliveryService;
    private final ItemDeliveryService itemDeliveryService;
    private final CommandDeliveryService commandDeliveryService;
    private final ItemService itemService;
    private final ExecutorService internalWorker = Executors.newVirtualThreadPerTaskExecutor();

    public DeliveryProcessor(JavaPlugin plugin,
                             DeliveryService deliveryService,
                             ItemDeliveryService itemDeliveryService,
                             CommandDeliveryService commandDeliveryService,
                             ItemService itemService) {
        this.plugin = plugin;
        this.deliveryService = deliveryService;
        this.itemDeliveryService = itemDeliveryService;
        this.commandDeliveryService = commandDeliveryService;
        this.itemService = itemService;
    }


    public @NonNull DeliveryResult process(@NonNull Delivery delivery) {
        delivery = deliveryService.prepareAttempt(delivery);
        DeliveryResult status = delivery.getStatus();
        if (status != DeliveryResult.INCOMPLETED) {
            return status;
        }

        long itemId = delivery.getItemId();
        long orderId = delivery.getOrderId();

        Logger logger = plugin.getSLF4JLogger();
        Optional<Item> itemOpt = itemService.find(itemId);
        if (itemOpt.isEmpty()) {
            logger.error("Cannot find item with id {} in order {}", itemId, orderId);
            return DeliveryResult.FAILED;
        }

        String username = delivery.getUsername();
        Player onlinePlayer = Bukkit.getPlayerExact(username);
        if (onlinePlayer == null) {
            logger.debug("Player {} for order {} is offline", username, orderId);
            return DeliveryResult.INCOMPLETED;
        }

        Item item = itemOpt.get();
        ItemType type = item.getType();
        CompletableFuture<DeliveryResult> resultFuture = switch (type) {
            case ITEM -> itemDeliveryService.completeItemDelivery(delivery, item);
            case COMMAND -> commandDeliveryService.completeCommandDelivery(delivery, item);
            case PACK -> processPack(delivery, item);
        };

        DeliveryResult postStatus = resultFuture.join();
        Delivery saved = deliveryService.save(Delivery.builder()
                .id(delivery.getId())
                .orderId(orderId)
                .itemId(itemId)
                .packId(delivery.getPackId())
                .username(username)
                .status(postStatus)
                .attempts(delivery.getAttempts())
                .attemptTime(delivery.getAttemptTime())
                .build());
        return saved.getStatus();
    }

    private @NonNull CompletableFuture<DeliveryResult> processPack(@NonNull Delivery delivery,
                                                                   @NonNull Item pack) {
        if (pack.getType() != ItemType.PACK) {
            return CompletableFuture.completedFuture(DeliveryResult.FAILED);
        }

        long packId = pack.getId();
        long orderId = delivery.getOrderId();
        List<Item> packItems = itemService.findChildren(packId);
        if (packItems.isEmpty()) {
            return CompletableFuture.completedFuture(DeliveryResult.COMPLETED);
        }

        CompletableFuture<DeliveryResult>[] packFuture = packItems.stream()
                .map(i -> Delivery.builder()
                        .username(delivery.getUsername())
                        .orderId(orderId)
                        .itemId(i.getId())
                        .packId(packId)
                        .status(DeliveryResult.INCOMPLETED)
                        .build())
                .<Supplier<DeliveryResult>>map(d -> () -> this.process(d))
                .map(sup -> CompletableFuture.supplyAsync(sup, internalWorker)
                        .orTimeout(1, TimeUnit.MINUTES)
                        .exceptionally(ex -> {
                            plugin.getSLF4JLogger().error("Cannot on delivery item", ex);
                            return DeliveryResult.FAILED;
                        }))
                .<CompletableFuture<DeliveryResult>>toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(packFuture)
                .thenApply(ignored -> {
                    long countCompleted = Arrays.stream(packFuture)
                            .map(CompletableFuture::join)
                            .filter(dr -> Objects.equals(dr, DeliveryResult.COMPLETED))
                            .count();

                    int countItems = packItems.size();
                    if (countCompleted == countItems) {
                        return DeliveryResult.COMPLETED;
                    }

                    return DeliveryResult.INCOMPLETED;
                })
                .exceptionally(ex -> {
                    plugin.getSLF4JLogger().error("Error processing pack {} in order {}", packId, orderId, ex);
                    return DeliveryResult.FAILED;
                });
    }

}
