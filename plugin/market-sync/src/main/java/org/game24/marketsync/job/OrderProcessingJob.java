package org.game24.marketsync.job;

import lombok.NonNull;
import org.game24.marketsync.model.Order;
import org.game24.marketsync.processor.OrderProcessor;
import org.game24.marketsync.service.OrderService;
import org.slf4j.Logger;

import java.util.List;

public class OrderProcessingJob implements Runnable {

    private final OrderService orderService;

    private final OrderProcessor orderProcessor;

    private final Logger logger;

    public OrderProcessingJob(OrderService orderService, OrderProcessor orderProcessor, Logger logger) {
        this.orderService = orderService;
        this.orderProcessor = orderProcessor;
        this.logger = logger;
    }


    @Override
    public void run() {
        logger.info("Staring order processing");
        List<@NonNull Order> orders = orderService.findPaidOrders();
        logger.info("Found {} orders to process", orders.size());
        for (Order order : orders) {
            if (Thread.currentThread().isInterrupted()) {
                logger.info("Interrupted order processing");
                break;
            }

            orderProcessor.processOrder(order);
        }
        logger.info("Finish processing orders");
    }
}
