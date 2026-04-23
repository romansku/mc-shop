package org.game24.marketsync.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class Item {

    private long id;

    private ItemType type;

    private String data;

    @Builder.Default
    private int count = 1;

}
