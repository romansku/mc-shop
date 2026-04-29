package org.game24.marketsync.service;

import lombok.NonNull;
import org.game24.marketsync.dao.OrderDAO;
import org.game24.marketsync.model.Order;

import java.util.List;
import java.util.Locale;

public class OrderService {

    private final OrderDAO orderDAO;

    public OrderService(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    @NonNull
    public List<@NonNull Order> findReadyOrdersForUser(@NonNull String user) {
        return orderDAO.findReadyOrdersForUser(user.toLowerCase(Locale.ROOT));
    }


    public void complete(long orderId) {
        orderDAO.complete(orderId);
    }
}
