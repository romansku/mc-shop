-- Idempotent migration: only add the column if it's missing.
SET @net_amount_exists :=
    (
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'mshop_player_orders'
          AND column_name = 'net_amount'
    );

SET @net_amount_ddl :=
    IF(
        @net_amount_exists = 0,
        'ALTER TABLE mshop_player_orders ADD COLUMN net_amount DECIMAL(12,2) NULL',
        'SELECT 1'
    );

PREPARE stmt FROM @net_amount_ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
