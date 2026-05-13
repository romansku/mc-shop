<!-- BEGIN:nextjs-agent-rules -->
# This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` before writing any code. Heed deprecation notices.
<!-- END:nextjs-agent-rules -->

## Prisma и ошибка `prisma://`

Если в рантайме появляется `Error validating datasource db: the URL must start with the protocol prisma://…`, почти всегда причина в том, что клиент был сгенерирован командой `prisma generate --no-engine`.

Исправление:

1. Останови `npm run dev` (иначе на Windows часто `EPERM` при замене `query_engine*.node`).
2. Выполни `npm run prisma:generate` (это обычный `prisma generate` **без** `--no-engine`).
3. Запусти dev снова.

В репозитории не используй `--no-engine` для локальной разработки с прямым `mysql://` в `DATABASE_URL`.
