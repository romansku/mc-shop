package org.game24.marketsync;


import org.bukkit.plugin.java.JavaPlugin;
import org.game24.marketsync.config.MarketSyncConfig;
import org.game24.marketsync.dao.Database;
import org.game24.marketsync.dao.DeliveryDAO;
import org.game24.marketsync.dao.ItemDAO;
import org.game24.marketsync.dao.OrderDAO;
import org.game24.marketsync.dao.impl.DeliverySimpleDAO;
import org.game24.marketsync.dao.impl.ItemSimpleDAO;
import org.game24.marketsync.dao.impl.OrderSimpleDAO;
import org.game24.marketsync.game.CommandDeliveryService;
import org.game24.marketsync.game.ItemDeliveryService;
import org.game24.marketsync.job.OrderProcessingJob;
import org.game24.marketsync.processor.DeliveryProcessor;
import org.game24.marketsync.processor.OrderProcessor;
import org.game24.marketsync.service.DeliveryService;
import org.game24.marketsync.service.ItemService;
import org.game24.marketsync.service.OrderService;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MarketSync extends JavaPlugin {

    private final ScheduledExecutorService orderProcessingExecutor = Executors.newScheduledThreadPool(1,
            Thread.ofVirtual().factory());

    private Logger logger;

    private MarketSyncConfig config;

    private Database database;

    private DeliveryService deliveryService;

    private OrderService orderService;

    private ItemService itemService;

    private ScheduledFuture<?> scheduledTask;

    @Override
    public void onEnable() {
        this.logger = this.getSLF4JLogger();
        initConfig();
        initServices();
        startOrderProcessingJob();
    }

    private void initConfig() {
        this.config = new MarketSyncConfig(this);
    }

    private void initServices() {
        database = new Database(this.config);

        DeliveryDAO deliveryDAO = new DeliverySimpleDAO(database, logger);
        deliveryService = new DeliveryService(deliveryDAO);

        OrderDAO orderDAO = new OrderSimpleDAO(database, logger);
        orderService = new OrderService(orderDAO);

        ItemDAO itemDAO = new ItemSimpleDAO(database, logger);
        itemService = new ItemService(itemDAO);
    }

    private void startOrderProcessingJob() {
        ItemDeliveryService itemDeliveryService = new ItemDeliveryService(this);
        CommandDeliveryService commandDeliveryService = new CommandDeliveryService(this);
        DeliveryProcessor deliveryProcessor = new DeliveryProcessor(this,
                deliveryService,
                itemDeliveryService,
                commandDeliveryService,
                itemService);
        OrderProcessor orderProcessor = new OrderProcessor(orderService, deliveryProcessor, logger);
        OrderProcessingJob job = new OrderProcessingJob(orderService, orderProcessor, logger);

        int jobDelay = config.getJobDelay();
        scheduledTask = orderProcessingExecutor.scheduleWithFixedDelay(
                job,
                jobDelay,
                jobDelay,
                TimeUnit.MINUTES);
    }

    @Override
    public void onDisable() {
        shutdownOrderProcessingJob();

        database.close();
    }

    private void shutdownOrderProcessingJob() {

        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(true);
        }

        if (!orderProcessingExecutor.isShutdown()) {
            orderProcessingExecutor.shutdown();
            try {
                boolean terminated = orderProcessingExecutor.awaitTermination(10, TimeUnit.SECONDS);
                if (!terminated) {
                    orderProcessingExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warn("Shutdown thread was interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }
}
