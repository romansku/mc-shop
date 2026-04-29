package org.game24.marketsync.processor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.NonNull;
import org.game24.marketsync.model.Delivery;
import org.game24.marketsync.model.DeliveryResult;
import org.game24.marketsync.model.Order;
import org.game24.marketsync.service.OrderService;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class OrderProcessor {

    private final OrderService orderService;

    private final ExecutorService worker = Executors.newVirtualThreadPerTaskExecutor();

    private final LoadingCache<Long, Lock> orderLocks = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(CacheLoader.from(k -> new ReentrantLock(true)));

    private final DeliveryProcessor deliveryProcessor;

    private final Logger logger;

    public OrderProcessor(@NonNull OrderService orderService,
                          @NonNull DeliveryProcessor deliveryProcessor,
                          @NonNull Logger logger) {
        this.orderService = orderService;
        this.deliveryProcessor = deliveryProcessor;
        this.logger = logger;
    }


    public void processOrder(@NonNull Order order) {

        Lock lock = orderLocks.getUnchecked(order.getId());
        if (lock.tryLock()) {
            try {
                CompletableFuture<DeliveryResult>[] deliveryFutures = runDeliveries(order);
                CompletableFuture.allOf(deliveryFutures)
                        .whenComplete((ignored, ex) -> {
                            if (ex != null) {
                                logger.error("Error processing order {}", order.getId(), ex);
                                return;
                            }

                            long countCompleted = Arrays.stream(deliveryFutures)
                                    .map(CompletableFuture::join)
                                    .filter(dr -> Objects.equals(dr, DeliveryResult.COMPLETED))
                                    .count();

                            int countItems = order.getItems().size();
                            if (countCompleted == countItems) {
                                orderService.complete(order.getId());
                            }
                        });
            } finally {
                lock.unlock();
            }
        } else {
            logger.info("Order {} has been locked", order.getId());
        }


    }

    private CompletableFuture<DeliveryResult> @NonNull [] runDeliveries(@NonNull Order order) {
        return order.getItems().stream()
                .map(i -> Delivery.builder()
                        .orderId(order.getId())
                        .itemId(i.getId())
                        .username(order.getUsername())
                        .build())
                .<Supplier<DeliveryResult>>map(d -> () -> deliveryProcessor.process(d))
                .map(c -> CompletableFuture.supplyAsync(c, worker)
                        .orTimeout(2, TimeUnit.MINUTES)
                        .exceptionally(ex -> {
                            logger.error("Cannot on delivery item", ex);
                            return DeliveryResult.FAILED;
                        }))
                .<CompletableFuture<DeliveryResult>>toArray(CompletableFuture[]::new);
    }


}
