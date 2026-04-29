package org.game24.marketsync.service;

import org.game24.marketsync.dao.DeliveryDAO;
import org.game24.marketsync.model.Delivery;
import org.game24.marketsync.model.DeliveryResult;

import java.time.Instant;

public class DeliveryService {

    private final DeliveryDAO deliveryDAO;

    public DeliveryService(DeliveryDAO deliveryDAO) {
        this.deliveryDAO = deliveryDAO;
    }


    public Delivery prepareAttempt(Delivery delivery) {

        Delivery processable = deliveryDAO.getByOrderAndItem(
                delivery.getOrderId(),
                delivery.getItemId(),
                delivery.getPackId());

        if (processable != null) {
            DeliveryResult status = processable.getStatus();
            if (status == DeliveryResult.COMPLETED) {
                return processable;
            }
        } else {
            processable = delivery;
        }

        processable.setAttemptTime(Instant.now());
        processable.setAttempts(processable.getAttempts() + 1);
        long id = deliveryDAO.save(processable);
        processable.setId(id);
        return processable;
    }

    public Delivery save(Delivery delivery) {
        long id = deliveryDAO.save(delivery);
        delivery.setId(id);
        return delivery;
    }
}
