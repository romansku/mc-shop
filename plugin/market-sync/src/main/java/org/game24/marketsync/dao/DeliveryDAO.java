package org.game24.marketsync.dao;

import lombok.NonNull;
import org.game24.marketsync.model.Delivery;

public interface DeliveryDAO {

    long get(@NonNull Delivery delivery);

    Delivery getByOrderAndItem(long orderId, long itemId);

    long save(@NonNull Delivery delivery);
}
