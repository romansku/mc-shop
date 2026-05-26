import { NextResponse } from "next/server";
import { prisma } from "@/app/dao/prisma";
import { paymentLog } from "@/payment/paymentLogger";
import {
  parseYooMoneyWebhookBody,
  verifyYooMoneySha1,
  verifyYooMoneySign,
} from "@/payment/yoomoneyWebhook";

export async function POST(request: Request) {
  try {
    let verificationMode: "sign" | "sha1_legacy" | "skipped_no_secret" = "skipped_no_secret";
    const contentType = request.headers.get("content-type") ?? "unknown";
    const rawBody = await request.text();
    const payload = parseYooMoneyWebhookBody(rawBody);
    paymentLog("info", "yoomoney:webhook incoming", {
      verificationMode,
      contentType,
      label: payload.label,
      operationId: payload.operation_id,
      amount: payload.amount,
      currency: payload.currency,
    });

    if (!payload.label || !/^\d+$/.test(payload.label)) {
      paymentLog("warn", "yoomoney:webhook rejected invalid label", {
        label: payload.label,
      });
      return NextResponse.json({ ok: false, message: "Invalid label" }, { status: 400 });
    }

    const secret = process.env.YOOMONEY_NOTIFICATION_SECRET?.trim();
    if (!secret) {
      verificationMode = "skipped_no_secret";
      paymentLog("warn", "yoomoney:webhook secret missing, sha1 skipped", {
        verificationMode,
      });
    } else {
      if (payload.sign) {
        verificationMode = "sign";
        const verify = verifyYooMoneySign(payload, secret);
        if (!verify.ok) {
          paymentLog("warn", "yoomoney:webhook rejected invalid sign", {
            verificationMode,
            label: payload.label,
            operationId: payload.operation_id,
            receivedSign: verify.receivedSign || "(empty)",
            computedSign: verify.computedSign,
          });
          return NextResponse.json({ ok: false, message: "Invalid sign" }, { status: 401 });
        }
        paymentLog("info", "yoomoney:webhook sign verified", {
          verificationMode,
          label: payload.label,
          operationId: payload.operation_id,
        });
      } else {
        verificationMode = "sha1_legacy";
        // Backward compatibility for old notifications before sign rollout.
        const verify = verifyYooMoneySha1(payload, secret);
        if (!verify.ok) {
          paymentLog("warn", "yoomoney:webhook rejected invalid sha1_hash", {
            verificationMode,
            label: payload.label,
            operationId: payload.operation_id,
            receivedHash: verify.receivedHash || "(empty)",
            computedHash: verify.computedHash,
          });
          return NextResponse.json({ ok: false, message: "Invalid sha1_hash" }, { status: 401 });
        }
        paymentLog("warn", "yoomoney:webhook sha1 verified (legacy mode)", {
          verificationMode,
          label: payload.label,
          operationId: payload.operation_id,
        });
      }
    }

    const orderId = BigInt(payload.label);
    const order = await prisma.mshop_player_orders.findUnique({
      where: { id: orderId },
    });

    if (!order) {
      paymentLog("warn", "yoomoney:webhook rejected order not found", {
        verificationMode,
        orderId: payload.label,
      });
      return NextResponse.json({ ok: false, message: "Order not found" }, { status: 404 });
    }

    const paidAmount = Number(payload.amount);
    const orderAmount = Number(order.amount);
    if (!Number.isFinite(paidAmount) || paidAmount <= 0) {
      paymentLog("warn", "yoomoney:webhook rejected invalid amount", {
        verificationMode,
        orderId: payload.label,
        amount: payload.amount,
      });
      return NextResponse.json({ ok: false, message: "Invalid amount" }, { status: 400 });
    }
    if (Math.round(paidAmount * 100) !== Math.round(orderAmount * 100)) {
      paymentLog("warn", "yoomoney:webhook rejected amount mismatch", {
        verificationMode,
        orderId: payload.label,
        paidAmount,
        orderAmount,
      });
      return NextResponse.json({ ok: false, message: "Amount mismatch" }, { status: 409 });
    }

    const paymentId = /^\d+$/.test(payload.operation_id)
      ? BigInt(payload.operation_id)
      : null;

    await prisma.mshop_player_orders.update({
      where: { id: order.id },
      data: {
        status: "PAID",
        payment_id: paymentId,
        yoomoney_operation_id: payload.operation_id || null,
        yoomoney_sender: payload.sender || null,
        yoomoney_payload: rawBody,
      },
    });
    paymentLog("info", "yoomoney:webhook order marked paid", {
      verificationMode,
      orderId: payload.label,
      operationId: payload.operation_id,
      amount: paidAmount,
    });

    return NextResponse.json({ ok: true });
  } catch (error) {
    paymentLog("error", "yoomoney:webhook processing error", {
      verificationMode: "skipped_no_secret",
      error,
    });
    return NextResponse.json(
      {
        ok: false,
        message: error instanceof Error ? error.message : "Webhook processing failed",
      },
      { status: 500 },
    );
  }
}
