"use client";

import Image from "next/image";
import { useCallback, useEffect, useMemo, useState } from "react";
import type { GoodsCard } from "@/app/dao/goodsDao";
import type {
  CreatePaymentResponseBody,
  PaymentStatusResponseBody,
} from "@/payment/types";
import styles from "./Payment.module.css";
import cStyles from "./CryptoPayment.module.css";

type CryptoPaymentProps = {
  onBack: () => void;
  items: GoodsCard[];
  totalUsd: number;
  playerLogin: string;
};

type ApiError = { ok: false; message: string };
type StatusView = {
  label: string;
  isFinal: boolean;
};

function resolveStatus(status?: string): StatusView {
  switch (status) {
    case "CONFIRMED":
    case "PAID":
      return { label: "Платеж подтвержден в блокчейне", isFinal: true };
    case "FAILED":
    case "EXPIRED":
      return { label: "Платеж не прошел или истек", isFinal: true };
    case "PROCESSING":
      return { label: "Ожидаем подтверждение сети", isFinal: false };
    default:
      return { label: "Ожидаем поступление транзакции", isFinal: false };
  }
}

export default function CryptoPayment({
  onBack,
  items,
  totalUsd,
  playerLogin,
}: CryptoPaymentProps) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [data, setData] = useState<CreatePaymentResponseBody | null>(null);
  const [paymentStatus, setPaymentStatus] = useState<string | null>(null);
  const [explorerUrl, setExplorerUrl] = useState<string | null>(null);

  const requestBody = useMemo(
    () => ({
      items: items.map((item) => ({
        id: item.id,
        name: item.name,
        price: item.price,
      })),
      totalUsd,
      playerLogin,
    }),
    [items, totalUsd, playerLogin],
  );

  useEffect(() => {
    if (!playerLogin) {
      setLoading(false);
      setError("Не указан ник игрока");
      return;
    }
    let cancelled = false;

    async function run() {
      setLoading(true);
      setError(null);
      try {
        const response = await fetch("/api/payments/create", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(requestBody),
        });
        const json = (await response.json()) as
          | CreatePaymentResponseBody
          | ApiError;

        if (!response.ok || !("ok" in json) || json.ok !== true) {
          const message =
            typeof json === "object" &&
            json !== null &&
            "message" in json &&
            typeof (json as ApiError).message === "string"
              ? (json as ApiError).message
              : "Не удалось создать платёж";
          throw new Error(message);
        }

        if (!cancelled) {
          setData(json);
        }
      } catch (e) {
        if (!cancelled) {
          setError(e instanceof Error ? e.message : "Ошибка сети");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void run();
    return () => {
      cancelled = true;
    };
  }, [requestBody, playerLogin]);

  useEffect(() => {
    if (!data) return;
    if (data.provider !== "STATIC_USDT_TRC20" && data.provider !== "STATIC_SOL") {
      return;
    }
    const paymentId = data.paymentId;

    let cancelled = false;
    let timer: ReturnType<typeof setTimeout> | null = null;

    async function poll() {
      try {
        const response = await fetch(`/api/payments/status/${paymentId}`, {
          method: "GET",
          cache: "no-store",
        });
        const json = (await response.json()) as
          | PaymentStatusResponseBody
          | ApiError;

        if (!response.ok || !("ok" in json) || json.ok !== true) {
          const message =
            typeof json === "object" &&
            json !== null &&
            "message" in json &&
            typeof (json as ApiError).message === "string"
              ? (json as ApiError).message
              : "Не удалось проверить статус";
          throw new Error(message);
        }

        if (cancelled) return;

        setPaymentStatus(json.status);
        setExplorerUrl(json.explorerUrl);
        const resolved = resolveStatus(json.status);
        if (!resolved.isFinal) {
          timer = setTimeout(() => {
            void poll();
          }, 15000);
        }
      } catch {
        if (cancelled) return;
        timer = setTimeout(() => {
          void poll();
        }, 20000);
      }
    }

    void poll();

    return () => {
      cancelled = true;
      if (timer) {
        clearTimeout(timer);
      }
    };
  }, [data]);

  const copy = useCallback(async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
    } catch {
      /* ignore */
    }
  }, []);

  const qrSrc =
    data?.payAddress && data.payAmount
      ? `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(
          data.provider === "STATIC_SOL"
            ? `Solana SOL\n${data.payAddress}\n${data.payAmount}`
            : `USDT TRC20\n${data.payAddress}\n${data.payAmount}`,
        )}`
      : null;
  const resolvedStatus = resolveStatus(paymentStatus ?? undefined);

  return (
    <div>
      <button type="button" className={styles.back} onClick={onBack}>
        ← Назад к выбору
      </button>

      {loading ? <p className={cStyles.loading}>Готовим реквизиты…</p> : null}
      {error ? <p className={cStyles.error}>{error}</p> : null}

      {data ? (
        <>
          <p className={cStyles.summary}>
            Игрок: <strong>{playerLogin}</strong>
            <br />
            Заказ №{data.paymentId} · к оплате ${data.fiatAmountUsd} →{" "}
            <strong>{data.payAmount}</strong> {data.payCurrency}
          </p>
          <p className={cStyles.status}>{resolvedStatus.label}</p>

          {data.invoiceUrl ? (
            <a
              className={cStyles.invoice}
              href={data.invoiceUrl}
              target="_blank"
              rel="noreferrer"
            >
              Открыть страницу оплаты NOWPayments
            </a>
          ) : null}

          <div className={cStyles.row}>
            <span className={cStyles.label}>
              {data.provider === "STATIC_SOL" ? "Адрес (Solana)" : "Адрес (TRC20)"}
            </span>
            <span className={cStyles.value}>
              {data.payAddress ?? "— (см. ссылку NOWPayments)"}
            </span>
            {data.payAddress ? (
              <button
                type="button"
                className={cStyles.copy}
                onClick={() => copy(data.payAddress ?? "")}
              >
                Скопировать адрес
              </button>
            ) : null}
          </div>

          <div className={cStyles.row}>
            <span className={cStyles.label}>
              {data.provider === "STATIC_SOL" ? "Сумма SOL" : "Сумма USDT"}
            </span>
            <span className={cStyles.value}>{data.payAmount}</span>
            <button
              type="button"
              className={cStyles.copy}
              onClick={() => copy(data.payAmount)}
            >
              Скопировать сумму
            </button>
          </div>

          {qrSrc ? (
            <Image
              className={cStyles.qr}
              src={qrSrc}
              alt="QR для оплаты"
              width={180}
              height={180}
              unoptimized
            />
          ) : null}

          {data.hint ? <p className={cStyles.hint}>{data.hint}</p> : null}
          {explorerUrl ? (
            <a className={cStyles.explorer} href={explorerUrl} target="_blank" rel="noreferrer">
              {data.provider === "STATIC_SOL"
                ? "Открыть транзакцию в Solscan"
                : "Открыть транзакцию в Tronscan"}
            </a>
          ) : null}
        </>
      ) : null}
    </div>
  );
}
