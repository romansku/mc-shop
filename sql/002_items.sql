INSERT INTO mshop_items(item_type, data, amount)
VALUES ('COMMAND', 'points give {username} 100', 1),                        -- 1$
       ('COMMAND', 'points give {username} 250', 1),                        -- 2$
       ('COMMAND', 'points give {username} 700', 1),                        -- 5$
       ('COMMAND', 'points give {username} 1350', 1),                       -- 10$
       ('COMMAND', 'lp user {username} parent addtemp 7d accumulate', 1),   -- 2$
       ('COMMAND', 'lp user {username} parent addtemp 30d accumulate', 1),  -- 5$
       ('COMMAND', 'lp user {username} parent addtemp 90d accumulate', 1),  -- 12$
       ('COMMAND', 'lp user {username} parent addtemp 365d accumulate', 1), -- 40$
       ('PACK', 'no data', 1); -- 40$


CREATE TABLE IF NOT EXISTS mshop_item_packs
(
    parent_item_id BIGINT NOT NULL,
    child_item_id  BIGINT NOT NULL,

    PRIMARY KEY (parent_item_id, child_item_id),
    CONSTRAINT fk__item_pack__parent_item_id__item__id FOREIGN KEY (parent_item_id) REFERENCES mshop_items (id),
    CONSTRAINT fk__item_pack__child_item_id__item__id FOREIGN KEY (child_item_id) REFERENCES mshop_items (id)
);


INSERT INTO mshop_item_packs(parent_item_id, child_item_id)
VALUES (1, 5), -- 2.5$
       (2, 6), -- 6$
       (3, 7), -- 15$
       (4, 8) --  45$