INSERT INTO mshop_items(id, item_type, data, amount, name, description, prioritization, price)
VALUES (1, 'COMMAND', 'PP;;give;;100', 1, '100 Points', 'Малый пакет премиум-монет', 2, 100.00),          -- 1$
       (2, 'COMMAND', 'PP;;give;;220', 1, '220 Points', 'Небольшой пакет премиум-монет', 2, 200.00),              -- 2$
       (3, 'COMMAND', 'PP;;give;;580', 1, '580 Points', 'Средний пакет премиум-монет', 2, 500.00),            -- 5$
       (4, 'COMMAND', 'PP;;give;;1250', 1, '1250 Points', 'Крупный пакет премиум-монет', 2, 1000.00),         -- 10$
       (5, 'COMMAND', 'LP;;perm;;mshop.nickname.prefix.text;;183;;DAYS', 1, 'Префикс: текст',
        'Текст префикса ника на 183 дня', 3, 100.00),                                                           -- 1$
       (6, 'COMMAND', 'LP;;perm;;mshop.nickname.prefix.color;;183;;DAYS', 1, 'Префикс: цвет',
        'Цвет префикса ника на 183 дня', 3, 100.00),                                                            -- 1$
       (7, 'COMMAND', 'LP;;perm;;mshop.nickname.suffix.text;;183;;DAYS', 1, 'Суффикс: текст',
        'Текст суффикса ника на 183 дня', 3, 100.00),                                                           -- 1$
       (8, 'COMMAND', 'LP;;perm;;mshop.nickname.suffix.color;;183;;DAYS', 1, 'Суффикс: цвет',
        'Цвет суффикса ника на 183 дня', 3, 100.00),                                                            -- 1$
       (9, 'COMMAND', 'LP;;perm;;mshop.nickname.chat.color;;183;;DAYS', 1, 'Цвет текста в чате',
        'Цвет сообщений в чате на 183 дня', 3, 100.00),                                                         -- 1$
       (10, 'COMMAND', 'LP;;perm;;mshop.nickname.name.color;;183;;DAYS', 1, 'Цвет имени',
        'Цвет отображаемого имени на 183 дня', 3, 100.00),                                                      -- 1$
       (11, 'COMMAND', 'LP;;group;;vip;;7;;DAYS', 1, 'VIP · Неделя', 'Статус VIP на 7 дней', 1, 100.00),      -- 1$
       (12, 'COMMAND', 'LP;;group;;vip;;31;;DAYS', 1, 'VIP · Месяц', 'Статус VIP на 31 день', 1, 300.00),   -- 3$
       (13, 'COMMAND', 'LP;;group;;vip;;92;;DAYS', 1, 'VIP · 3 Месяца', 'Статус VIP на 92 дня', 1, 800.00),     -- 8$
       (14, 'COMMAND', 'LP;;group;;vip;;183;;DAYS', 1, 'VIP · Полгода', 'Статус VIP на 183 дня', 1, 1500.00), -- 15$
       (15, 'COMMAND', 'LP;;group;;vip;;365;;DAYS', 1, 'VIP · Год', 'Статус VIP на 365 дней', 1, 2500.00),  -- 25$
       (16, 'PACK', 'no data', 1, 'Набор «Старт»', 'Статус VIP на Месяц\n220 премиум монет', 0, 400.00),
       (17, 'PACK', 'no data', 1, 'Набор «Супер Ник»', 'Все эффекты ника и чата\nв одном наборе', 0, 500.00)
;

UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/points-100.webp' WHERE id = 1;
UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/points-220.webp' WHERE id = 2;
UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/points-580.webp' WHERE id = 3;
UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/points-1250.webp' WHERE id = 4;

UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/prefix-text.webp' WHERE id = 5;
UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/prefix-color.webp' WHERE id = 6;
UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/suffix-text.webp' WHERE id = 7;
UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/suffix-color.webp' WHERE id = 8;
UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/chat-color.webp' WHERE  id = 9;
UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/name-color.webp' WHERE id = 10;

UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/vip-7.webp'  WHERE id = 11;
UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/vip-31.webp'  WHERE id = 12;
UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/vip-92.webp' WHERE id = 13;
UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/vip-183.webp'  WHERE id = 14;
UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/vip-365.webp'  WHERE id = 15;

UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/starter-pack.webp' WHERE id = 16;
UPDATE mshop_items SET image_link = 'https://mc-s3.game-24.org/rift-mc/images/items/super-nick.webp' WHERE id = 17;

UPDATE mshop_items SET favorite = true WHERE id in (12, 16, 17);

INSERT INTO mshop_item_packs (parent_item_id, child_item_id)
VALUES (16, 2),
       (16, 12),
       (17, 5),
       (17, 6),
       (17, 7),
       (17, 8),
       (17, 9),
       (17, 10);

