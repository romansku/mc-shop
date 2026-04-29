package org.game24.marketsync.dao;

import lombok.NonNull;
import org.game24.marketsync.model.Order;

import java.util.List;

public interface OrderDAO {

    @NonNull
    List<@NonNull Order> findPaidOrders();

    void complete(long orderId);
}
