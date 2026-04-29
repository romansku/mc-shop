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
import org.game24.marketsync.service.DeliveryService;
import org.game24.marketsync.service.ItemService;
import org.game24.marketsync.service.OrderService;
import org.slf4j.Logger;

public class MarketSync extends JavaPlugin {

    private Logger logger;

    private MarketSyncConfig config;

    private DeliveryService deliveryService;

    private OrderService orderService;

    private ItemService itemService;

    @Override
    public void onEnable() {
        this.logger = this.getSLF4JLogger();
        initConfig();
        initServices();
    }

    private void initConfig() {
        this.config = new MarketSyncConfig(this);
    }

    private void initServices() {
        Database database = new Database(this.config);

        DeliveryDAO deliveryDAO = new DeliverySimpleDAO(database, logger);
        deliveryService = new DeliveryService(deliveryDAO);

        OrderDAO orderDAO = new OrderSimpleDAO(database, logger);
        orderService = new OrderService(orderDAO);

        ItemDAO itemDAO = new ItemSimpleDAO(database, logger);
        itemService = new ItemService(itemDAO);
    }
}
