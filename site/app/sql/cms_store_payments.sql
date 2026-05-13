-- Платежи интернет-магазина (префикс cms_ для единообразия с остальными таблицами CMS)
-- Выполните вручную или через миграцию, если ещё не создано.

CREATE TABLE IF NOT EXISTS cms_store_payments
(
    id                   BIGINT         NOT NULL AUTO_INCREMENT,
    provider             VARCHAR(32)    NOT NULL COMMENT 'NOWPAYMENTS | STATIC_USDT_TRC20',
    status               VARCHAR(32)    NOT NULL DEFAULT 'AWAITING_PAYMENT',
    fiat_currency        VARCHAR(8)     NOT NULL DEFAULT 'USD',
    amount_fiat          DECIMAL(12, 2) NOT NULL,
    player_login         VARCHAR(32)    NOT NULL COMMENT 'ник Minecraft для выдачи',
    pay_currency         VARCHAR(32)    NULL COMMENT 'например usdttrc20',
    pay_amount_expected  DECIMAL(20, 8) NULL,
    pay_address          VARCHAR(128)   NULL,
    external_payment_id  VARCHAR(80)    NULL COMMENT 'id у NOWPayments',
    chain_tx_hash        VARCHAR(120)   NULL COMMENT 'tx hash для on-chain проверки',
    invoice_url          VARCHAR(512)   NULL,
    cart_snapshot        TEXT           NOT NULL COMMENT 'JSON: позиции корзины',
    last_ipn_payload     LONGTEXT       NULL COMMENT 'последний JSON IPN',
    chain_confirmed_at   DATETIME(3)    NULL,
    created_at           DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at           DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),
    KEY idx_cms_store_payments_external (external_payment_id),
    KEY idx_cms_store_payments_status (status),
    KEY idx_cms_store_payments_chain_tx (chain_tx_hash),
    KEY idx_cms_store_payments_player_login (player_login),
    KEY idx_cms_store_payments_created (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
