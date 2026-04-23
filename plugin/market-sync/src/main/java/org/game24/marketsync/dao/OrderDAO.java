package org.game24.marketsync.dao;

import lombok.NonNull;
import org.game24.marketsync.model.Order;

import java.util.List;

public interface OrderDAO {

    @NonNull
    List<Order> findReadyOrdersForUser(@NonNull String username);

    void complete(long orderId);
}
