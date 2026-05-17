# Оплата в магазине

## Текущий статус

- ЮMoney работает через форму Quickpay.
- Криптовалюта отображается как неактивная кнопка.

## Переменные окружения

| Переменная | Назначение |
|------------|------------|
| `YOOMONEY_WALLET` | Номер кошелька в поле `receiver` формы Quickpay |
| `YOOMONEY_NOTIFICATION_SECRET` | Секрет для проверки `sha1_hash` в webhook (опционально, но рекомендуется) |

## Эндпоинты

- `POST /api/payments/yoomoney/create` — создает заказ `mshop_player_orders` + позиции `mshop_order_items` и возвращает данные для HTML-формы Quickpay.
- `POST /api/payments/webhook/yoomoney` — webhook от ЮMoney, обновляет заказ в `PAID` по `label = order_id`.

## SQL миграция

Запустите `app/sql/migrate_mshop_orders_yoomoney.sql`.
