-- for process in game; not for site
CREATE TABLE IF NOT EXISTS mshop_deliveries
(
    id           BIGINT                                    NOT NULL AUTO_INCREMENT,
    order_id     BIGINT                                    NOT NULL,
    item_id      BIGINT                                    NOT NULL,
    pack_id      BIGINT                                    NULL,
    username     VARCHAR(35)                               NOT NULL,
    status       enum ('COMPLETED','INCOMPLETED','FAILED') NOT NULL,
    attempt_time TIMESTAMP                                 NOT NULL,
    attempts     INT                                       NOT NULL,
    pack_id_idx  BIGINT AS (IFNULL(pack_id, -1)) VIRTUAL,

    PRIMARY KEY (id),
    CONSTRAINT fk__delivery__item_id__player_order__item_id FOREIGN KEY (item_id) REFERENCES mshop_order_items (item_id),
    CONSTRAINT fk__delivery__order_id__player_order__order_id FOREIGN KEY (order_id) REFERENCES mshop_order_items (order_id),
    CONSTRAINT fk__delivery__pack_id__player_order__order_id FOREIGN KEY (pack_id) REFERENCES mshop_order_items (item_id)
);

CREATE UNIQUE INDEX u_idx__delivery__order_item_pack ON mshop_deliveries (order_id, item_id, pack_id_idx);