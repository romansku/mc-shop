"use client";

import { useMemo, useState } from "react";
import type { GoodsCard } from "@/app/dao/goodsDao";
import { validatePlayerLogin } from "@/payment/validatePlayerLogin";
import CryptoPayment from "./CryptoPayment";
import styles from "./Payment.module.css";
import YooKassaPayment from "./YooKassaPayment";

type PaymentProps = {
  open: boolean;
  onClose: () => void;
  items: GoodsCard[];
  totalUsd: number;
};

type Step = "choose" | "crypto" | "yookassa";

export default function Payment({
  open,
  onClose,
  items,
  totalUsd,
}: PaymentProps) {
  const [step, setStep] = useState<Step>("choose");
  const [playerLogin, setPlayerLogin] = useState("");

  const loginCheck = useMemo(
    () => validatePlayerLogin(playerLogin),
    [playerLogin],
  );
  const canContinue = loginCheck.ok;

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
              {!canContinue && playerLogin.trim().length > 0 ? (
                <p className={styles.loginError}>{loginCheck.message}</p>
              ) : null}
            </div>

            <div className={styles.chooseGrid}>
              <button
                type="button"
                className={`${styles.methodButton} ${!canContinue ? styles.methodButtonDisabled : ""}`}
                disabled={!canContinue}
                onClick={() => {
                  if (!canContinue) return;
                  setStep("crypto");
                }}
              >
                <p className={styles.methodTitle}>Криптовалюта</p>
                <p className={styles.methodDesc}>
                  NOWPayments (если включён), либо SOL на ваш депозитный адрес,
                  либо прямой USDT TRC20 — в зависимости от настроек сервера.
                </p>
              </button>
              <button
                type="button"
                className={`${styles.methodButton} ${!canContinue ? styles.methodButtonDisabled : ""}`}
                disabled={!canContinue}
                onClick={() => {
                  if (!canContinue) return;
                  setStep("yookassa");
                }}
              >
                <p className={styles.methodTitle}>ЮKassa</p>
                <p className={styles.methodDesc}>
                  Оплата картой — в разработке.
                </p>
              </button>
            </div>
          </>
        ) : null}

        {step === "crypto" ? (
          <CryptoPayment
            onBack={() => setStep("choose")}
            items={items}
            totalUsd={totalUsd}
            playerLogin={loginCheck.ok ? loginCheck.login : ""}
          />
        ) : null}

        {step === "yookassa" ? (
          <YooKassaPayment onBack={() => setStep("choose")} />
        ) : null}
      </div>
    </div>
  );
}
