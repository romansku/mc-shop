CREATE TABLE IF NOT EXISTS mshop_players
(
    uuid       CHAR(36)     NOT NULL,
    username   VARCHAR(25)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (uuid),
    KEY idx_mshop_players_username (username)
);
