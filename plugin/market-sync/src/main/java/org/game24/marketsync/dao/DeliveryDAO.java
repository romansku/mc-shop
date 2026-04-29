package org.game24.marketsync.dao;

import lombok.NonNull;
import org.game24.marketsync.model.Delivery;

public interface DeliveryDAO {

    Delivery getByOrderAndItem(long orderId, long itemId, Long packId);

    long save(@NonNull Delivery delivery);
}
