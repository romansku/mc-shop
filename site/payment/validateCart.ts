import type { CartLineSnapshot } from "./types";
import { validatePlayerLogin } from "./validatePlayerLogin";

export function validateCartPayload(items: unknown, totalUsd: unknown): {
  ok: true;
  items: CartLineSnapshot[];
  totalUsd: number;
} | { ok: false; message: string } {
  if (!Array.isArray(items) || items.length === 0) {
    return { ok: false, message: "Корзина пуста" };
  }

  const parsed: CartLineSnapshot[] = [];
  for (const row of items) {
    if (typeof row !== "object" || row === null) {
      return { ok: false, message: "Некорректные позиции корзины" };
    }
    const r = row as Record<string, unknown>;
    const id = Number(r.id);
    const name = r.name;
    const price = Number(r.price);
    if (!Number.isInteger(id) || id <= 0) {
      return { ok: false, message: "Некорректный id товара" };
    }
    if (typeof name !== "string" || name.length === 0) {
      return { ok: false, message: "Некорректное имя товара" };
    }
    if (!Number.isFinite(price) || price < 0) {
      return { ok: false, message: "Некорректная цена" };
    }
    parsed.push({ id, name, price });
  }

  const total = Number(totalUsd);
  if (!Number.isFinite(total) || total <= 0) {
    return { ok: false, message: "Некорректная сумма" };
  }

  const sum = parsed.reduce((acc, line) => acc + line.price, 0);
  const roundedSum = Math.round(sum * 100) / 100;
  const roundedTotal = Math.round(total * 100) / 100;
  if (roundedSum !== roundedTotal) {
    return { ok: false, message: "Сумма не совпадает с позициями корзины" };
  }

  return { ok: true, items: parsed, totalUsd: roundedTotal };
}

export function validateCreatePaymentBody(body: {
  items?: unknown;
  totalUsd?: unknown;
  playerLogin?: unknown;
}):
  | { ok: true; items: CartLineSnapshot[]; totalUsd: number; playerLogin: string }
  | { ok: false; message: string } {
  const cart = validateCartPayload(body.items, body.totalUsd);
  if (!cart.ok) {
    return cart;
  }
  const login = validatePlayerLogin(body.playerLogin);
  if (!login.ok) {
    return login;
  }
  return {
    ok: true,
    items: cart.items,
    totalUsd: cart.totalUsd,
    playerLogin: login.login,
  };
}