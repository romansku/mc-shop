package org.game24.marketsync.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
public class Order {

    private Long id;

    private String username;

    @Builder.Default
    private List<Item> items = new ArrayList<>();

    private OrderStatus status;

    @Builder.Default
    private Instant delivered = Instant.now();
}
