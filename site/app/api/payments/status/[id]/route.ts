import { NextResponse } from "next/server";
import { prisma } from "@/app/dao/prisma";
import { refreshStaticPaymentStatus } from "@/payment/refreshStaticPaymentStatus";
import { solanaExplorerTxSignature } from "@/payment/scanSolTransfer";
import { tronExplorerUrl } from "@/payment/scanTronUsdt";
import type { PaymentStatusResponseBody } from "@/payment/types";

export async function GET(
  _request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    const { id } = await context.params;
    if (!/^\d+$/.test(id)) {
      return NextResponse.json({ ok: false, message: "Invalid id" }, { status: 400 });
    }

    const paymentId = BigInt(id);
    const refreshed = await refreshStaticPaymentStatus(paymentId);
    if (!refreshed) {
      return NextResponse.json({ ok: false, message: "Not found" }, { status: 404 });
    }

    const payment =
      refreshed.id === paymentId
        ? refreshed
        : await prisma.store_payment.findUnique({ where: { id: paymentId } });
    if (!payment) {
      return NextResponse.json({ ok: false, message: "Not found" }, { status: 404 });
    }

    const chainRef = payment.chain_tx_hash ?? payment.external_payment_id;

    let explorerUrl: string | null = null;
    if (chainRef) {
      if (payment.provider === "STATIC_USDT_TRC20") {
        explorerUrl = tronExplorerUrl(chainRef);
      } else if (payment.provider === "STATIC_SOL") {
        explorerUrl = solanaExplorerTxSignature(chainRef);
      }
    }

    const body: PaymentStatusResponseBody = {
      ok: true,
      paymentId: String(payment.id),
      provider:
        payment.provider === "STATIC_USDT_TRC20"
          ? "STATIC_USDT_TRC20"
          : payment.provider === "STATIC_SOL"
            ? "STATIC_SOL"
            : "NOWPAYMENTS",
      status: payment.status,
      chainTxHash: chainRef,
      explorerUrl,
    };

    return NextResponse.json(body);
  } catch (error) {
    return NextResponse.json(
      {
        ok: false,
        message: error instanceof Error ? error.message : "Status check failed",
      },
      { status: 500 },
    );
  }
}
