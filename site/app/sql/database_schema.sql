-- for site
CREATE TABLE cms_goods
(
    id             BIGINT               NOT NULL AUTO_INCREMENT,
    name           VARCHAR(25)          NOT NULL UNIQUE,
    description    VARCHAR(500),
    favorite       TINYINT(1) DEFAULT 0 NOT NULL,
    prioritization INT                  NOT NULL,
    price          DECIMAL(10, 2)       NOT NULL,
    image_link     VARCHAR(512),

    PRIMARY KEY (id)
);

-- for game
CREATE TABLE cms_item
(
    id        BIGINT                           NOT NULL AUTO_INCREMENT,
    -- for process
    item_type ENUM ('COMMAND', 'ITEM', 'PACK') NOT NULL,
    -- technical name in minecraft server or raw command
    data      VARCHAR(150)                     NOT NULL,
    -- ignored if type is not ITEM
    amount    INT                              NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

-- pack of items
CREATE TABLE cms_item_pack
(
    goods_id         BIGINT NOT NULL,
    included_item_id BIGINT NOT NULL,

    PRIMARY KEY (goods_id, included_item_id),
    CONSTRAINT fk_cms_goods
        FOREIGN KEY (goods_id)
            REFERENCES cms_goods (id),
    CONSTRAINT fk_cms_items
        FOREIGN KEY (included_item_id)
            REFERENCES cms_item (id)
);

-- order
CREATE TABLE cms_player_order
(
    id             BIGINT                    NOT NULL AUTO_INCREMENT,
    user_name      VARCHAR(255)              NOT NULL,
    email          VARCHAR(255)              NOT NULL,
    payment_method ENUM ('CRYPTO', 'PAYPAL') NOT NULL,
    created_date   TIMESTAMP,
    delivered_date TIMESTAMP,
    payment_id     BIGINT, -- no SQL relation for few payments systems
    status         ENUM (
        'DRAFT',           -- user created order
        'CREATED',         -- the order is validated and invoice is prepared
        'PAID',            -- the user have paid the order
        'COMPLETED',       -- the order is delivered to the user
        'CANCELLED'        -- the order is canceled
        )                                    NOT NULL,
    PRIMARY KEY (id)
);

-- items for order
CREATE TABLE cms_order_item
(
    order_id BIGINT REFERENCES cms_player_order (id),
    item_id  BIGINT REFERENCES cms_item (id),

    PRIMARY KEY (order_id, item_id)
);

-- paymen of
CREATE TABLE cms_crypto_payments
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
    updated   TIMESTAMP    NOT NULL
);

-- Платежи витрины магазина (Next.js): см. app/sql/cms_store_payments.sql
