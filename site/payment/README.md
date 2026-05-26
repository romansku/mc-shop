# Оплата в магазине

## Текущий статус

- ЮMoney работает через форму Quickpay.
- Криптовалюта отображается как неактивная кнопка.

## Переменные окружения

| Переменная | Назначение |
|------------|------------|
| `YOOMONEY_WALLET` | Номер кошелька в поле `receiver` формы Quickpay |
| `YOOMONEY_NOTIFICATION_SECRET` | Секрет для проверки подписи webhook (`sign`, HMAC-SHA256) |
| `PAYMENT_LOG_DIR` | Папка для файлов логов оплаты (абсолютный путь или путь относительно `process.cwd()`) |

`YOOMONEY_NOTIFICATION_SECRET` берется в настройках ЮMoney для HTTP-уведомлений (секретное слово / secret).  
С 18 мая 2026 для проверки используется параметр `sign` (HMAC-SHA256).  
`sha1_hash` оставлен только как fallback для старых уведомлений без `sign`.

## Эндпоинты

- `POST /api/payments/yoomoney/create` — создает заказ `mshop_player_orders` + позиции `mshop_order_items` и возвращает данные для HTML-формы Quickpay.
- `POST /api/payments/webhook/yoomoney` — webhook от ЮMoney, обновляет заказ в `PAID` по `label = order_id`.

## SQL миграция

Запустите `app/sql/migrate_mshop_orders_yoomoney.sql`.

## Логи оплаты

- Логи пишутся в директорию из `PAYMENT_LOG_DIR`.
- Если `PAYMENT_LOG_DIR` не задан, используется `./logs` (относительно рабочей директории процесса).
- Формат ротации: новый файл каждый месяц — `payments-YYYY-MM.log` (например, `payments-2026-05.log`).
