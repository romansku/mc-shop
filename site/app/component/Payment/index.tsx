"use client";

import { useMemo, useState } from "react";
import type { ItemCard } from "@/app/dao/itemsCatalogDao";
import type { CreateYooMoneyOrderResponseBody, YooMoneyPaymentType } from "@/payment/types";
import { validatePlayerLogin } from "@/payment/validatePlayerLogin";
import styles from "./Payment.module.css";

type PaymentProps = {
  open: boolean;
  onClose: () => void;
  items: ItemCard[];
  total: number;
  onPaymentCreated: () => void;
};

type Step = "choose" | "yoomoney";
type ApiError = { ok: false; message: string };
type YooMoneyResponse = CreateYooMoneyOrderResponseBody | ApiError;

const legalLinks = {
  userAgreement: "https://mc-s3.game-24.org/rift-mc/docs/user_agreement.pdf",
  privacy: "https://mc-s3.game-24.org/rift-mc/docs/privacy.pdf",
};

export default function Payment({
  open,
  onClose,
  items,
  total,
  onPaymentCreated,
}: PaymentProps) {
  const [step, setStep] = useState<Step>("choose");
  const [playerLogin, setPlayerLogin] = useState("");
  const [email, setEmail] = useState("");
  const [paymentType, setPaymentType] = useState<YooMoneyPaymentType>("YOOMONEY_WALLET");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loginCheck = useMemo(
    () => validatePlayerLogin(playerLogin),
    [playerLogin],
  );
  const emailValid = useMemo(() => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim()), [email]);
  const canContinue = loginCheck.ok && emailValid && items.length > 0 && total > 0;

  function submitYooMoneyForm(order: CreateYooMoneyOrderResponseBody) {
    const form = document.createElement("form");
    form.method = "POST";
    form.action = order.actionUrl;
    form.style.display = "none";

    const yoomoneyPaymentType = order.paymentType === "CARD" ? "AC" : "PC";

    const fields: Record<string, string> = {
      receiver: order.receiver,
      label: order.label,
      "quickpay-form": "button",
      sum: order.sum,
      paymentType: yoomoneyPaymentType,
    };

    for (const [name, value] of Object.entries(fields)) {
      const input = document.createElement("input");
      input.type = "hidden";
      input.name = name;
      input.value = value;
      form.appendChild(input);
    }

    document.body.appendChild(form);
    form.submit();
  }

  async function createYooMoneyInvoice() {
    if (!canContinue || !loginCheck.ok) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await fetch("/api/payments/yoomoney/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          items: items.map((item) => ({ id: item.id })),
          totalAmount: total,
          playerLogin: loginCheck.login,
          email: email.trim(),
          paymentType,
        }),
      });
      const json = (await response.json()) as YooMoneyResponse;
      if (!response.ok || !("ok" in json) || json.ok !== true) {
        const message = "message" in json ? json.message : "Не удалось создать заказ";
        throw new Error(message);
      }
      onPaymentCreated();
      submitYooMoneyForm(json);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Ошибка сети");
    } finally {
      setLoading(false);
    }
  }

  if (!open) {
    return null;
  }

  return (
    <div
      className={styles.overlay}
      role="dialog"
      aria-modal="true"
      aria-labelledby="payment-title"
    >
      <div className={styles.panel}>
        <div className={styles.head}>
          <h2 id="payment-title" className={styles.title}>
            Оплата заказа
          </h2>
          <button
            type="button"
            className={styles.close}
            onClick={onClose}
            aria-label="Закрыть"
          >
            ×
          </button>
        </div>

        {step === "choose" ? (
          <>
            <div className={styles.loginBlock}>
              <label className={styles.loginLabel} htmlFor="player-login">
                Ник в игре (обязательно)
              </label>
              <input
                id="player-login"
                className={styles.loginInput}
                type="text"
                autoComplete="username"
                spellCheck={false}
                maxLength={32}
                value={playerLogin}
                onChange={(event) => setPlayerLogin(event.target.value)}
                placeholder="Например: Steve_123"
              />
              <p className={styles.loginHint}>
                Латиница, цифры и «_», от 3 до 32 символов — для выдачи покупки
                на сервере.
              </p>
              {!loginCheck.ok && playerLogin.trim().length > 0 ? (
                <p className={styles.loginError}>{loginCheck.message}</p>
              ) : null}
            </div>

            <div className={styles.loginBlock}>
              <label className={styles.loginLabel} htmlFor="player-email">
                Email (обязательно)
              </label>
              <input
                id="player-email"
                className={styles.loginInput}
                type="email"
                autoComplete="email"
                maxLength={255}
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="you@example.com"
              />
              {!emailValid && email.trim().length > 0 ? (
                <p className={styles.loginError}>Некорректный email</p>
              ) : null}
            </div>

            <div className={styles.chooseGrid}>
              <button
                type="button"
                className={`${styles.methodButton} ${styles.methodButtonDisabled}`}
                disabled
              >
                <p className={styles.methodTitle}>Криптовалюта</p>
                <p className={styles.methodDesc}>
                  Пока недоступно.
                </p>
              </button>
              <button
                type="button"
                className={`${styles.methodButton} ${!canContinue ? styles.methodButtonDisabled : ""}`}
                disabled={!canContinue}
                onClick={() => {
                  if (!canContinue) return;
                  setStep("yoomoney");
                }}
              >
                <p className={styles.methodTitle}>ЮMoney</p>
                <p className={styles.methodDesc}>
                  Создание заказа и переход в форму оплаты.
                </p>
              </button>
            </div>
          </>
        ) : null}

        {step === "yoomoney" ? (
          <div>
            <button
              type="button"
              className={styles.back}
              onClick={() => {
                setStep("choose");
                setError(null);
              }}
            >
              ← Назад к выбору
            </button>

            <p className={styles.methodDesc}>
              Игрок: <strong>{loginCheck.ok ? loginCheck.login : "-"}</strong>
              <br />
              Сумма заказа: <strong>{total.toFixed(2)} ₽</strong>
            </p>

            <div className={styles.chooseGrid}>
              <label className={styles.methodButton}>
                <input
                  type="radio"
                  name="paymentType"
                  value="YOOMONEY_WALLET"
                  checked={paymentType === "YOOMONEY_WALLET"}
                  onChange={() => setPaymentType("YOOMONEY_WALLET")}
                />{" "}
                ЮMoney кошелек
              </label>
              <label className={styles.methodButton}>
                <input
                  type="radio"
                  name="paymentType"
                  value="CARD"
                  checked={paymentType === "CARD"}
                  onChange={() => setPaymentType("CARD")}
                />{" "}
                Банковская карта
              </label>
            </div>

            <p className={styles.legalNotice}>
              Нажимая кнопку «Оплатить», вы принимаете условия{" "}
              <a
                href={legalLinks.userAgreement}
                target="_blank"
                rel="noreferrer"
                className={styles.legalLink}
              >
                Пользовательского соглашения
              </a>{" "}
              и даете согласие на обработку персональных данных в соответствии с{" "}
              <a
                href={legalLinks.privacy}
                target="_blank"
                rel="noreferrer"
                className={styles.legalLink}
              >
                Политикой конфиденциальности
              </a>
              .
            </p>

            <button
              type="button"
              className={styles.methodButton}
              onClick={() => void createYooMoneyInvoice()}
              disabled={loading}
            >
              {loading ? "Создаем заказ..." : "Оплатить"}
            </button>

            {error ? <p className={styles.loginError}>{error}</p> : null}
          </div>
        ) : null}

      </div>
    </div>
  );
}
