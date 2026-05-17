import type { CartLineSnapshot, YooMoneyPaymentType } from "./types";
import { validatePlayerLogin } from "./validatePlayerLogin";

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function validateEmail(value: unknown): { ok: true; email: string } | { ok: false; message: string } {
  if (typeof value !== "string") {
    return { ok: false, message: "Укажите email" };
  }
  const email = value.trim();
  if (!EMAIL_RE.test(email)) {
    return { ok: false, message: "Некорректный email" };
  }
  return { ok: true, email };
}

function validatePaymentType(value: unknown): { ok: true; paymentType: YooMoneyPaymentType } | { ok: false; message: string } {
  if (value === "YOOMONEY_WALLET" || value === "CARD") {
    return { ok: true, paymentType: value };
  }
  return { ok: false, message: "Некорректный тип оплаты ЮMoney" };
}

export function validateCreateYooMoneyOrderBody(body: {
  items?: unknown;
  totalAmount?: unknown;
  playerLogin?: unknown;
  email?: unknown;
  paymentType?: unknown;
}):
  | {
      ok: true;
      items: CartLineSnapshot[];
      totalAmount: number;
      playerLogin: string;
      email: string;
      paymentType: YooMoneyPaymentType;
    }
  | { ok: false; message: string } {
  if (!Array.isArray(body.items) || body.items.length === 0) {
    return { ok: false, message: "Корзина пуста" };
  }

  const parsedItems: CartLineSnapshot[] = [];
  for (const row of body.items) {
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
    if (typeof name !== "string" || name.trim().length === 0) {
      return { ok: false, message: "Некорректное имя товара" };
    }
    if (!Number.isFinite(price) || price <= 0) {
      return { ok: false, message: "Некорректная цена товара" };
    }
    parsedItems.push({ id, name: name.trim(), price });
  }

  const totalAmount = Number(body.totalAmount);
  if (!Number.isFinite(totalAmount) || totalAmount <= 0) {
    return { ok: false, message: "Некорректная сумма заказа" };
  }

  const cartTotal = Math.round(parsedItems.reduce((acc, item) => acc + item.price, 0) * 100) / 100;
  const roundedTotal = Math.round(totalAmount * 100) / 100;
  if (cartTotal !== roundedTotal) {
    return { ok: false, message: "Сумма заказа не совпадает с корзиной" };
  }

  const login = validatePlayerLogin(body.playerLogin);
  if (!login.ok) {
    return login;
  }

  const email = validateEmail(body.email);
  if (!email.ok) {
    return email;
  }

  const paymentType = validatePaymentType(body.paymentType);
  if (!paymentType.ok) {
    return paymentType;
  }

  return {
    ok: true,
    items: parsedItems,
    totalAmount: roundedTotal,
    playerLogin: login.login,
    email: email.email,
    paymentType: paymentType.paymentType,
  };
}
