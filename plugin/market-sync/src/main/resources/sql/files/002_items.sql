INSERT INTO mshop_items(id, item_type, data, amount, name, description, prioritization, price)
VALUES (1, 'COMMAND', 'PP;;give;;100', 1, '100 Points', 'Малый пакет премиум-монет', 2, 100.00),          -- 1$
       (2, 'COMMAND', 'PP;;give;;220', 1, '220 Points', 'Небольшой пакет премиум-монет', 2, 200.00),              -- 2$
       (3, 'COMMAND', 'PP;;give;;580', 1, '580 Points', 'Средний пакет премиум-монет', 2, 500.00),            -- 5$
       (4, 'COMMAND', 'PP;;give;;1250', 1, '1250 Points', 'Крупный пакет премиум-монет', 2, 1000.00),         -- 10$
       (5, 'COMMAND', 'LP;;perm;;mshop.nickname.prefix.text;;183;;DAYS', 1, 'Префикс: текст',
        'Текст префикса ника на 183 дня.', 3, 100.00),                                                           -- 1$
       (6, 'COMMAND', 'LP;;perm;;mshop.nickname.prefix.color;;183;;DAYS', 1, 'Префикс: цвет',
        'Цвет префикса ника на 183 дня.', 3, 100.00),                                                            -- 1$
       (7, 'COMMAND', 'LP;;perm;;mshop.nickname.suffix.text;;183;;DAYS', 1, 'Суффикс: текст',
        'Текст суффикса ника на 183 дня.', 3, 100.00),                                                           -- 1$
       (8, 'COMMAND', 'LP;;perm;;mshop.nickname.suffix.color;;183;;DAYS', 1, 'Суффикс: цвет',
        'Цвет суффикса ника на 183 дня.', 3, 100.00),                                                            -- 1$
       (9, 'COMMAND', 'LP;;perm;;mshop.nickname.chat.color;;183;;DAYS', 1, 'Цвет текста в чате',
        'Цвет сообщений в чате на 183 дня.', 3, 100.00),                                                         -- 1$
       (10, 'COMMAND', 'LP;;perm;;mshop.nickname.name.color;;183;;DAYS', 1, 'Цвет имени',
        'Цвет отображаемого имени на 183 дня.', 3, 100.00),                                                      -- 1$
       (11, 'COMMAND', 'LP;;group;;vip;;7;;DAYS', 1, 'VIP · 7 дней', 'Статус VIP на 7 дней.', 1, 100.00),      -- 1$
       (12, 'COMMAND', 'LP;;group;;vip;;31;;DAYS', 1, 'VIP · 31 день', 'Статус VIP на 31 день.', 1, 300.00),   -- 3$
       (13, 'COMMAND', 'LP;;group;;vip;;92;;DAYS', 1, 'VIP · 92 дня', 'Статус VIP на 92 дня.', 1, 800.00),     -- 8$
       (14, 'COMMAND', 'LP;;group;;vip;;183;;DAYS', 1, 'VIP · 183 дня', 'Статус VIP на полгода.', 1, 1500.00), -- 15$
       (15, 'COMMAND', 'LP;;group;;vip;;365;;DAYS', 1, 'VIP · 1 год', 'Статус VIP на 365 дней.', 1, 2500.00),  -- 25$
       (16, 'PACK', 'no data', 1, 'Набор «Старт»', 'Статус VIP на 31 день\n220 премиум монет', 0, 400.00)
;

INSERT INTO mshop_item_packs (parent_item_id, child_item_id)
VALUES (16, 2),
       (16, 12);

