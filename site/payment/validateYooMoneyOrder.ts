import type { YooMoneyPaymentType } from "./types";
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
      itemIds: number[];
      totalAmountHint: number;
      playerLogin: string;
      email: string;
      paymentType: YooMoneyPaymentType;
    }
  | { ok: false; message: string } {
  if (!Array.isArray(body.items) || body.items.length === 0) {
    return { ok: false, message: "Корзина пуста" };
  }

  const itemIds: number[] = [];
  for (const row of body.items) {
    if (typeof row !== "object" || row === null) {
      return { ok: false, message: "Некорректные позиции корзины" };
    }
    const id = Number((row as Record<string, unknown>).id);
    if (!Number.isInteger(id) || id <= 0) {
      return { ok: false, message: "Некорректный id товара" };
    }
    itemIds.push(id);
  }

  if (new Set(itemIds).size !== itemIds.length) {
    return { ok: false, message: "В корзине не должно быть одинаковых товаров" };
  }

  const totalAmountHint = Number(body.totalAmount);
  if (!Number.isFinite(totalAmountHint) || totalAmountHint <= 0) {
    return { ok: false, message: "Некорректная сумма заказа" };
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
    itemIds,
    totalAmountHint,
    playerLogin: login.login,
    email: email.email,
    paymentType: paymentType.paymentType,
  };
}
