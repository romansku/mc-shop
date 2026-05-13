import "server-only";

import { prisma } from "@/app/dao/prisma";
import { createNowpaymentsPayment } from "./nowpaymentsClient";
import { computeStaticSolLamports, fetchSolUsdRate } from "./staticSol";
import { computeStaticUsdtAmount } from "./staticUsdt";
import type { CartLineSnapshot, CreatePaymentResponseBody } from "./types";

const PAY_CURRENCY = "usdttrc20";

function getPublicBaseUrl(): string {
  const base =
    process.env.NEXT_PUBLIC_APP_URL?.replace(/\/$/, "") ??
    "http://localhost:3000";
  return base;
}

export async function createCryptoPaymentRecord(params: {
  items: CartLineSnapshot[];
  totalUsd: number;
  playerLogin: string;
}): Promise<CreatePaymentResponseBody> {
  const snapshot = JSON.stringify({
    items: params.items,
    totalUsd: params.totalUsd,
    playerLogin: params.playerLogin,
    createdAt: new Date().toISOString(),
  });

  const nowKey = process.env.NOWPAYMENTS_API_KEY?.trim();
  const staticWallet = process.env.STATIC_USDT_TRC20_ADDRESS?.trim();

  if (nowKey) {
    const draft = await prisma.store_payment.create({
      data: {
        provider: "NOWPAYMENTS",
        status: "CREATING",
        fiat_currency: "USD",
        amount_fiat: params.totalUsd,
        player_login: params.playerLogin,
        cart_snapshot: snapshot,
        pay_currency: PAY_CURRENCY,
      },
    });

    const orderId = String(draft.id);
    const ipnUrl = `${getPublicBaseUrl()}/api/payments/webhook/nowpayments`;

    try {
      const np = await createNowpaymentsPayment({
        apiKey: nowKey,
        priceAmount: params.totalUsd,
        priceCurrency: "usd",
        payCurrency: PAY_CURRENCY,
        orderId,
        ipnCallbackUrl: ipnUrl,
        orderDescription: `Minecraft store #${orderId} player=${params.playerLogin}`.slice(
          0,
          180,
        ),
      });

      const paymentId =
        np.payment_id !== undefined ? String(np.payment_id) : null;
      const payAddress = np.pay_address ?? null;
      const payAmount =
        np.pay_amount !== undefined ? String(np.pay_amount) : "0";
      const invoiceUrl = np.invoice_url ?? null;

      await prisma.store_payment.update({
        where: { id: draft.id },
        data: {
          status: "AWAITING_PAYMENT",
          external_payment_id: paymentId,
          pay_address: payAddress,
          pay_amount_expected: payAmount,
          invoice_url: invoiceUrl,
        },
      });

      return {
        ok: true,
        paymentId: orderId,
        provider: "NOWPAYMENTS",
        payCurrency: np.pay_currency ?? PAY_CURRENCY,
        payAddress,
        payAmount,
        invoiceUrl,
        fiatAmountUsd: params.totalUsd.toFixed(2),
        hint: invoiceUrl
          ? "Можно перейти по ссылке на счёт NOWPayments или оплатить по реквизитам ниже."
          : undefined,
      };
    } catch (error) {
      await prisma.store_payment.update({
        where: { id: draft.id },
        data: {
          status: "FAILED",
          last_ipn_payload: JSON.stringify({
            error: error instanceof Error ? error.message : "NOWPayments failed",
          }),
        },
      });
      throw error;
    }
  }

  const solDeposit = process.env.STATIC_SOL_DEPOSIT_ADDRESS?.trim();

  if (solDeposit) {
    const draft = await prisma.store_payment.create({
      data: {
        provider: "STATIC_SOL",
        status: "AWAITING_PAYMENT",
        fiat_currency: "USD",
        amount_fiat: params.totalUsd,
        player_login: params.playerLogin,
        cart_snapshot: snapshot,
        pay_currency: "SOL",
        pay_address: solDeposit,
      },
    });

    const rate = await fetchSolUsdRate();
    const { solDisplay } = computeStaticSolLamports({
      totalUsd: params.totalUsd,
      paymentId: draft.id,
      solUsdRate: rate,
    });

    await prisma.store_payment.update({
      where: { id: draft.id },
      data: {
        pay_amount_expected: solDisplay,
      },
    });

    return {
      ok: true,
      paymentId: String(draft.id),
      provider: "STATIC_SOL",
      payCurrency: "SOL",
      payAddress: solDeposit,
      payAmount: solDisplay,
      invoiceUrl: null,
      fiatAmountUsd: params.totalUsd.toFixed(2),
      hint:
        "Отправьте на указанный адрес Solana ровно эту сумму в SOL (native). Ник уже привязан к заказу в магазине; платёж распознаётся по уникальной сумме. " +
        "Проверьте в кошельке/на OKX, что на адрес поступит именно указанное количество SOL после комиссии сети.",
    };
  }

  if (!staticWallet) {
    throw new Error(
      "Не настроена оплата: задайте NOWPAYMENTS_API_KEY, STATIC_SOL_DEPOSIT_ADDRESS или STATIC_USDT_TRC20_ADDRESS в .env",
    );
  }

  const draft = await prisma.store_payment.create({
    data: {
      provider: "STATIC_USDT_TRC20",
      status: "AWAITING_PAYMENT",
      fiat_currency: "USD",
      amount_fiat: params.totalUsd,
      player_login: params.playerLogin,
      cart_snapshot: snapshot,
      pay_currency: PAY_CURRENCY,
      pay_address: staticWallet,
    },
  });

  const payAmount = computeStaticUsdtAmount({
    totalUsd: params.totalUsd,
    paymentId: draft.id,
  });

  await prisma.store_payment.update({
    where: { id: draft.id },
    data: {
      pay_amount_expected: payAmount,
    },
  });

  return {
    ok: true,
    paymentId: String(draft.id),
    provider: "STATIC_USDT_TRC20",
    payCurrency: PAY_CURRENCY,
    payAddress: staticWallet,
    payAmount,
    invoiceUrl: null,
    fiatAmountUsd: params.totalUsd.toFixed(2),
    hint: "Переведите ровно указанную сумму USDT (TRC20) на адрес. Уникальный «хвост» суммы помогает сопоставить платёж.",
  };
}
