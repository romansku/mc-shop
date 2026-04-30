CREATE TABLE IF NOT EXISTS mshop_migration
(
    id       BIGINT                  NOT NULL AUTO_INCREMENT,
    name     VARCHAR(512)            NOT NULL UNIQUE,
    executed TIMESTAMP DEFAULT NOW() NOT NULL,
    PRIMARY KEY (id)
);
