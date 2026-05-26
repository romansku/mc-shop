package org.game24.marketsync;


import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.game24.marketsync.config.AntiBrightnessConfig;
import org.game24.marketsync.config.MarketSyncConfig;
import org.game24.marketsync.dao.Database;
import org.game24.marketsync.dao.DeliveryDAO;
import org.game24.marketsync.dao.ItemDAO;
import org.game24.marketsync.dao.OrderDAO;
import org.game24.marketsync.dao.PlayerDAO;
import org.game24.marketsync.dao.impl.DeliverySimpleDAO;
import org.game24.marketsync.dao.impl.ItemSimpleDAO;
import org.game24.marketsync.dao.impl.OrderSimpleDAO;
import org.game24.marketsync.dao.impl.PlayerSimpleDAO;
import org.game24.marketsync.game.CommandDeliveryService;
import org.game24.marketsync.game.ItemDeliveryService;
import org.game24.marketsync.game.antibrightness.AntiBrightnessAdminAlerter;
import org.game24.marketsync.game.antibrightness.AntiBrightnessDecayJob;
import org.game24.marketsync.game.antibrightness.AntiBrightnessTracker;
import org.game24.marketsync.game.command.MarketSyncCommand;
import org.game24.marketsync.game.hook.AuthMeHook;
import org.game24.marketsync.game.hook.LuckPermsHook;
import org.game24.marketsync.game.hook.PlayerPointsHook;
import org.game24.marketsync.game.listener.AntiBrightnessListener;
import org.game24.marketsync.game.listener.NicknameChangeListener;
import org.game24.marketsync.game.listener.PlayerRegistryListener;
import org.game24.marketsync.game.placeholder.NickPlaceholderData;
import org.game24.marketsync.game.placeholder.NicknamePlaceholderExpansion;
import org.game24.marketsync.job.OrderProcessingJob;
import org.game24.marketsync.processor.DeliveryProcessor;
import org.game24.marketsync.processor.OrderProcessor;
import org.game24.marketsync.service.DeliveryService;
import org.game24.marketsync.service.ItemService;
import org.game24.marketsync.service.OrderService;
import org.game24.marketsync.service.PlayerRegistryService;
import org.game24.marketsync.sync.PlayerBootstrapSync;
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

    private PlayerRegistryService playerRegistryService;

    private LuckPermsHook luckPermsHook;

    private PlayerPointsHook playerPointsHook;

    private ScheduledFuture<?> scheduledTask;

    private NicknameChangeListener nicknameChangeListener;

    private NicknamePlaceholderExpansion placeholderExpansion;

    private AntiBrightnessTracker antiBrightnessTracker;

    private AntiBrightnessListener antiBrightnessListener;

    private AntiBrightnessAdminAlerter antiBrightnessAdminAlerter;

    private AntiBrightnessDecayJob antiBrightnessDecayJob;

    private Thread antiBrightnessDecayThread;

    @Override
    public void onEnable() {
        this.logger = this.getSLF4JLogger();
        initConfig();
        initServices();
        if (!isEnabled()) {
            return;
        }
        initPlayerRegistry();
        initHooks();
        initAntiBrightness();
        initCommands();
        initPlaceholders();
        startOrderProcessingJob();
    }

    public void reloadPluginConfig() {
        config.reload(this);
        if (database != null) {
            try {
                database.reloadPool(config);
            } catch (RuntimeException e) {
                logger.error("Failed to reload database pool", e);
                throw e;
            }
        }
        reloadAntiBrightnessConfig();
    }

    private void initConfig() {
        this.config = new MarketSyncConfig(this);
    }

    private void initServices() {
        database = new Database(this.config);
        try {
            database.init(this);
        } catch (Exception e) {
            logger.error("Error on migration database; disable plugin", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        DeliveryDAO deliveryDAO = new DeliverySimpleDAO(database, logger);
        deliveryService = new DeliveryService(deliveryDAO);

        OrderDAO orderDAO = new OrderSimpleDAO(database, logger);
        orderService = new OrderService(orderDAO);

        ItemDAO itemDAO = new ItemSimpleDAO(database, logger);
        itemService = new ItemService(itemDAO);

        PlayerDAO playerDAO = new PlayerSimpleDAO(database, logger);
        playerRegistryService = new PlayerRegistryService(playerDAO);
    }

    private void initPlayerRegistry() {
        AuthMeHook authMeHook = new AuthMeHook(this);
        Bukkit.getPluginManager().registerEvents(new PlayerRegistryListener(playerRegistryService), this);
        PlayerBootstrapSync bootstrapTask = new PlayerBootstrapSync(playerRegistryService, authMeHook, logger);
        Thread.ofVirtual().name("market-sync-player-bootstrap").start(bootstrapTask);
    }

    private void initHooks() {
        luckPermsHook = new LuckPermsHook(this);
        playerPointsHook = new PlayerPointsHook(this);
    }

    private void initAntiBrightness() {
        AntiBrightnessConfig antiBrightnessConfig = config.getAntiBrightnessConfig();
        antiBrightnessTracker = new AntiBrightnessTracker(antiBrightnessConfig);
        antiBrightnessAdminAlerter = new AntiBrightnessAdminAlerter(logger, antiBrightnessConfig);
        antiBrightnessListener = new AntiBrightnessListener(
                antiBrightnessConfig,
                antiBrightnessTracker,
                antiBrightnessAdminAlerter
        );
        Bukkit.getPluginManager().registerEvents(antiBrightnessListener, this);

        if (!antiBrightnessConfig.enabled()) {
            return;
        }

        antiBrightnessDecayJob = new AntiBrightnessDecayJob(antiBrightnessTracker, antiBrightnessConfig, logger);
        antiBrightnessDecayThread = Thread.ofVirtual()
                .name("market-sync-anti-brightness-decay")
                .start(antiBrightnessDecayJob);
    }

    private void reloadAntiBrightnessConfig() {
        AntiBrightnessConfig antiBrightnessConfig = config.getAntiBrightnessConfig();
        if (antiBrightnessTracker != null) {
            antiBrightnessTracker.updateConfig(antiBrightnessConfig);
        }
        if (antiBrightnessAdminAlerter != null) {
            antiBrightnessAdminAlerter.updateConfig(antiBrightnessConfig);
        }
        if (antiBrightnessListener != null) {
            antiBrightnessListener.updateConfig(antiBrightnessConfig);
        }
        if (antiBrightnessDecayJob != null) {
            antiBrightnessDecayJob.updateConfig(antiBrightnessConfig);
        }
    }

    private void initCommands() {
        MarketSyncCommand command = new MarketSyncCommand(this);
        registerCommand("marketsync", "MarketSync admin commands", command);
    }

    private void startOrderProcessingJob() {
        ItemDeliveryService itemDeliveryService = new ItemDeliveryService(this);
        CommandDeliveryService commandDeliveryService = new CommandDeliveryService(this,
                luckPermsHook,
                playerPointsHook);
        DeliveryProcessor deliveryProcessor = new DeliveryProcessor(this,
                deliveryService,
                itemDeliveryService,
                commandDeliveryService,
                itemService);
        OrderProcessor orderProcessor = new OrderProcessor(orderService, deliveryProcessor, this, logger);
        OrderProcessingJob job = new OrderProcessingJob(orderService, orderProcessor, logger);

        int jobDelay = config.getJobDelay();
        scheduledTask = orderProcessingExecutor.scheduleWithFixedDelay(
                job,
                0,
                jobDelay,
                TimeUnit.MINUTES);
    }

    private void initPlaceholders() {
        NickPlaceholderData placeholderData = new NickPlaceholderData();

        nicknameChangeListener = new NicknameChangeListener(this, placeholderData);
        nicknameChangeListener.init();
        Bukkit.getPluginManager().registerEvents(nicknameChangeListener, this);

        placeholderExpansion = new NicknamePlaceholderExpansion(placeholderData);
        placeholderExpansion.register();
    }

    @Override
    public void onDisable() {
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }

        if (nicknameChangeListener != null) {
            nicknameChangeListener.destroy();
        }

        shutdownOrderProcessingJob();
        shutdownAntiBrightness();

        if (database != null) {
            database.close();
        }
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

    private void shutdownAntiBrightness() {
        if (antiBrightnessDecayJob != null) {
            antiBrightnessDecayJob.stop();
        }

        if (antiBrightnessDecayThread != null) {
            antiBrightnessDecayThread.interrupt();
        }
    }
}
