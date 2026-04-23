package org.game24.marketsync.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Builder
public class Delivery {

    private Long id;

    private long orderId;

    private long itemId;

    private Long packId;

    private String username;

    private DeliveryResult status;

    private Instant attemptTime;

    private int attempts;

}
