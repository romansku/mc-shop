INSERT INTO mshop_items(item_type, data, amount)
VALUES ('COMMAND', 'PP;;give;;100', 1),                                    -- 1$
       ('COMMAND', 'PP;;give;;220', 1),                                    -- 2$
       ('COMMAND', 'PP;;give;;580', 1),                                    -- 5$
       ('COMMAND', 'PP;;give;;1250', 1),                                   -- 10$
       ('COMMAND', 'LP;;perm;;mshop.nickname.prefix.text;;6;;MONTHS', 1),  -- 1$
       ('COMMAND', 'LP;;perm;;mshop.nickname.prefix.color;;6;;MONTHS', 1), -- 1$
       ('COMMAND', 'LP;;perm;;mshop.nickname.suffix.text;;6;;MONTHS', 1),  -- 1$
       ('COMMAND', 'LP;;perm;;mshop.nickname.suffix.color;;6;;MONTHS', 1), -- 1$
       ('COMMAND', 'LP;;perm;;mshop.nickname.chat.color;;6;;MONTHS', 1),   -- 1$
       ('COMMAND', 'LP;;perm;;mshop.nickname.name.color;;6;;MONTHS', 1),   -- 1$
       ('COMMAND', 'LP;;group;;vip;;7;;DAYS', 1),                          -- 1$
       ('COMMAND', 'LP;;group;;vip;;1;;MONTHS', 1),                        -- 3$
       ('COMMAND', 'LP;;group;;vip;;3;;MONTHS', 1),                        -- 8$
       ('COMMAND', 'LP;;group;;vip;;6;;MONTHS', 1),                        -- 15$
       ('COMMAND', 'LP;;group;;vip;;12;;MONTHS', 1),                       -- 25$
       ('PACK', 'no data', 1) -- 40$
;

CREATE TABLE IF NOT EXISTS mshop_item_packs
(
    parent_item_id BIGINT NOT NULL,
    child_item_id  BIGINT NOT NULL,

    PRIMARY KEY (parent_item_id, child_item_id),
    CONSTRAINT fk__item_pack__parent_item_id__item__id FOREIGN KEY (parent_item_id) REFERENCES mshop_items (id),
    CONSTRAINT fk__item_pack__child_item_id__item__id FOREIGN KEY (child_item_id) REFERENCES mshop_items (id)
);
