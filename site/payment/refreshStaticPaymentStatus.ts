import "server-only";

import { prisma } from "@/app/dao/prisma";
import { findIncomingSolTransfer } from "./scanSolTransfer";
import { findIncomingUsdtTransfer } from "./scanTronUsdt";

const CHECKABLE_STATUSES = new Set(["AWAITING_PAYMENT", "PROCESSING"]);

export async function refreshStaticPaymentStatus(paymentId: bigint) {
  const payment = await prisma.store_payment.findUnique({
    where: { id: paymentId },
  });
  if (!payment) {
    return null;
  }

  if (payment.provider === "STATIC_SOL") {
    if (!CHECKABLE_STATUSES.has(payment.status)) {
      return payment;
    }
    if (!payment.pay_address || !payment.pay_amount_expected) {
      return payment;
    }

    const expectedSol = String(payment.pay_amount_expected);
    const tx = await findIncomingSolTransfer({
      recipientAddress: payment.pay_address,
      expectedSolDecimal: expectedSol,
      fromTimestampMs: payment.created_at.getTime() - 2 * 60 * 1000,
    });

    if (!tx) {
      if (payment.status !== "PROCESSING") {
        return prisma.store_payment.update({
          where: { id: payment.id },
          data: { status: "PROCESSING" },
        });
      }
      return payment;
    }

    return prisma.store_payment.update({
      where: { id: payment.id },
      data: {
        status: "CONFIRMED",
        chain_tx_hash: tx.signature,
        last_ipn_payload: JSON.stringify({
          source: "solana_rpc_scan",
          signature: tx.signature,
          lamports: tx.lamports.toString(),
        }),
      },
    });
  }

  if (payment.provider !== "STATIC_USDT_TRC20") {
    return payment;
  }
  if (!CHECKABLE_STATUSES.has(payment.status)) {
    return payment;
  }
  if (!payment.pay_address || !payment.pay_amount_expected) {
    return payment;
  }

  const expectedAmount = Number(payment.pay_amount_expected);
  if (!Number.isFinite(expectedAmount) || expectedAmount <= 0) {
    return payment;
  }

  const tx = await findIncomingUsdtTransfer({
    address: payment.pay_address,
    expectedAmount,
    fromTimestampMs: payment.created_at.getTime() - 2 * 60 * 1000,
  });

  if (!tx) {
    if (payment.status !== "PROCESSING") {
      return prisma.store_payment.update({
        where: { id: payment.id },
        data: { status: "PROCESSING" },
      });
    }
    return payment;
  }

  return prisma.store_payment.update({
    where: { id: payment.id },
    data: {
      status: "CONFIRMED",
      external_payment_id: tx.txHash,
      last_ipn_payload: JSON.stringify({
        source: "trongrid_scan",
        txHash: tx.txHash,
        amount: tx.amount,
      }),
    },
  });
}
