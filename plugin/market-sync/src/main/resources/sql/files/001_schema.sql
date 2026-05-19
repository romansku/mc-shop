-- sellable items: game fields + site / catalog
CREATE TABLE IF NOT EXISTS mshop_items
(
    id        BIGINT                                     NOT NULL AUTO_INCREMENT,
    -- for process (game)
    item_type ENUM ('COMMAND', 'ITEM', 'PACK') NOT NULL,
    -- technical name in minecraft server or raw command
    data      VARCHAR(150)                               NOT NULL,
    -- ignored if type is not ITEM
    amount    INT                                        NOT NULL DEFAULT 1,
    -- for site / catalog
    name           VARCHAR(25)              NOT NULL,
    description    VARCHAR(500)             NOT NULL,
    favorite       TINYINT(1) DEFAULT 0     NOT NULL,
    prioritization INT                      NOT NULL,
    price          DECIMAL(10, 2)           NOT NULL,
    image_link     VARCHAR(512),
    active         BOOLEAN    DEFAULT FALSE NOT NULL, -- hide test item from users

    PRIMARY KEY (id),
    UNIQUE KEY uq__mshop_items__name (name)
);

-- pack of items
CREATE TABLE IF NOT EXISTS mshop_item_packs
(
    parent_item_id BIGINT NOT NULL,
    child_item_id  BIGINT NOT NULL,

    PRIMARY KEY (parent_item_id, child_item_id),
    CONSTRAINT fk__item_pack__parent_item_id__item__id FOREIGN KEY (parent_item_id) REFERENCES mshop_items (id),
    CONSTRAINT fk__item_pack__child_item_id__item__id FOREIGN KEY (child_item_id) REFERENCES mshop_items (id)
);


-- order
CREATE TABLE IF NOT EXISTS mshop_player_orders
(
    id                    BIGINT                                         NOT NULL AUTO_INCREMENT,
    user_name             VARCHAR(25)                                    NOT NULL,
    email                 VARCHAR(255)                                   NOT NULL,
    payment_method        ENUM ('CRYPTO', 'PAYPAL', 'YOOMONEY')         NOT NULL,
    payment_type          VARCHAR(8)                                     NULL COMMENT 'PC | AC',
    amount                DECIMAL(12, 2)                                NOT NULL COMMENT 'Сумма инвойса',
    amount_currency       VARCHAR(8)                                     NOT NULL DEFAULT 'RUB',
    created_date          TIMESTAMP                                      NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_date        TIMESTAMP                                      NULL,
    payment_id            BIGINT                                         NULL,
    yoomoney_operation_id VARCHAR(64)                                    NULL,
    yoomoney_sender       VARCHAR(32)                                    NULL,
    yoomoney_payload      LONGTEXT                                       NULL,
    status                ENUM (
        'DRAFT',
        'CREATED',
        'PAID',
        'COMPLETED',
        'CANCELLED'
        )                                                                NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_mshop_player_orders_yoomoney_operation_id (yoomoney_operation_id),
    KEY idx_mshop_player_orders_status (status),
    KEY idx_mshop_player_orders_payment_method (payment_method)
);

-- items for order
CREATE TABLE IF NOT EXISTS mshop_order_items
(
    order_id BIGINT NOT NULL,
    item_id  BIGINT NOT NULL,

    PRIMARY KEY (order_id, item_id),
    CONSTRAINT fk__order_item__order_id__player_order__id FOREIGN KEY (order_id) REFERENCES mshop_player_orders (id),
    CONSTRAINT fk__order_item__item_id__item__id FOREIGN KEY (item_id) REFERENCES mshop_items (id)
);

-- paymen of
CREATE TABLE IF NOT EXISTS mshop_crypto_payments
(
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    native_id VARCHAR(255), -- fill after created in payment system
    user_name VARCHAR(255) NOT NULL,
    -- each payment system has own statuses
    status    ENUM (
        'CREATED',          -- invoice is prepared
        'PAID',             -- invoice is paid
        'CANCELLED'         -- invoice is canceled
        )                  NOT NULL,
    updated   TIMESTAMP    NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx__player_order__username ON mshop_player_orders (user_name);

ALTER DATABASE market_sync_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

ALTER TABLE mshop_items
    CONVERT TO CHARACTER SET utf8mb4
        COLLATE utf8mb4_unicode_ci;