# Оплата в магазине

## Выбор: USDT (TRC20)

Для магазина выбран **USDT в сети TRC20** (Tron): низкие комиссии, один стабильный актив, удобно для покупателей из РФ и не только.

Альтернативы **BTC/ETH** потребовали бы либо отдельные шлюзы под каждую сеть, либо более тяжёлый ончейн-мониторинг. Через NOWPayments при желании можно включить и другие монеты в личном кабинете провайдера, не меняя код витрины.

## Режим 1 — NOWPayments (рекомендуется)

Провайдер сам выдаёт адрес и сумму, присылает **IPN webhook** при смене статуса — статус платежа обновляется в таблице `cms_store_payments`. В API NOWPayments сумма заказа передаётся в **USD** (`price_currency: usd`), в соответствии с ценами в магазине.

### Что добавить в `.env`

| Переменная | Что это | Где взять |
|------------|---------|-----------|
| `NOWPAYMENTS_API_KEY` | API-ключ | [nowpayments.io](https://nowpayments.io) → регистрация → **Dashboard → API keys** → создать ключ. Это не «пароль» от аккаунта, а отдельный секрет для API. |
| `NOWPAYMENTS_IPN_SECRET` | Секрет подписи IPN | Там же в кабинете NOWPayments, раздел **IPN / Instant Payment Notifications** (секрет для проверки `x-nowpayments-sig`). |
| `NEXT_PUBLIC_APP_URL` | Публичный URL сайта | Например `https://shop.example.com` (без слэша в конце). Нужен для `ipn_callback_url`: `…/api/payments/webhook/nowpayments`. На локалке можно `http://localhost:3000`, но webhook с интернета до localhost не дойдёт — для теста IPN используйте ngrok или деплой. |

**Кошелёк** в NOWPayments настраивается в их кабинете (куда выводить крипту) — в `.env` адрес кошелька не обязателен.

## Режим 2 — Статический кошелёк (fallback)

Если API-ключа NOWPayments нет, приложение создаёт платёж в БД и показывает **ваш** адрес USDT TRC20. Сумма к оплате в USDT считается из **суммы заказа в USD** (цены в магазине в долларах) плюс небольшой уникальный «хвост» для идентификации.

### Что добавить в `.env`

| Переменная | Что это | Где взять |
|------------|---------|-----------|
| `STATIC_USDT_TRC20_ADDRESS` | Ваш адрес приёма USDT (TRC20) | Кошелёк TronLink / Trust Wallet / аппаратный кошелёк → «Получить» → скопировать адрес (начинается с `T…`). |
| `TRONGRID_API_KEY` *(опционально)* | Ключ TronGrid для стабильного RPC | [trongrid.io](https://www.trongrid.io) → создать API key. Без ключа тоже работает, но выше риск rate limit. |
| `TRON_USDT_CONTRACT_ADDRESS` *(опционально)* | Адрес контракта USDT в сети Tron | Обычно не менять. По умолчанию `TXLAQ63Xg1NAzckPwKHvzw7CSEmLMEqcdj`. |

Если задан `NOWPAYMENTS_API_KEY`, статический режим **не используется** для новых платежей.

## База данных

Выполните SQL из `app/sql/cms_store_payments.sql`, затем:

Если таблица уже существовала как `store_payments`, сначала см. `app/sql/migrate_rename_store_payments_to_cms.sql`. Если в `cms_store_payments` не хватает колонки `player_login`, см. `app/sql/cms_store_payments_add_player_login.sql`.

```bash
npx prisma generate
```

Если Prisma пишет, что URL должен начинаться с `prisma://`, значит клиент когда-то собрали с `prisma generate --no-engine`. Остановите dev-сервер и выполните `npm run prisma:generate` (см. также `AGENTS.md`).

## Эндпоинты

- `POST /api/payments/create` — создать крипто-платёж (тело: `{ items, totalUsd, playerLogin }`).
- `POST /api/payments/webhook/nowpayments` — IPN NOWPayments (не вызывать вручную).
- `GET /api/payments/status/:id` — проверка статуса платежа. Для `STATIC_USDT_TRC20` запускает on-chain сканирование входящих USDT TRC20.

## ЮKassa

Кнопка на витрине заглушена; отдельные ключи ЮKassa (`shopId`, секрет) понадобятся позже, когда подключим создание платежа и webhook.
