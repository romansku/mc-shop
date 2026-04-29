package org.game24.marketsync.service;

import lombok.NonNull;
import org.game24.marketsync.dao.OrderDAO;
import org.game24.marketsync.model.Order;

import java.util.List;

public class OrderService {

    private final OrderDAO orderDAO;

    public OrderService(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    @NonNull
    public List<@NonNull Order> findPaidOrders() {
        return orderDAO.findPaidOrders();
    }


    public void complete(long orderId) {
        orderDAO.complete(orderId);
    }
}
