import { NextResponse } from "next/server";
import { prisma } from "@/app/dao/prisma";
import { paymentLog } from "@/payment/paymentLogger";
import {
  parseYooMoneyWebhookBody,
  verifyYooMoneySha1,
  verifyYooMoneySign,
} from "@/payment/yoomoneyWebhook";

export async function POST(request: Request) {
  let orderIdForLog: string | null = null;
  try {
    let verificationMode: "sign" | "sha1_legacy" | "skipped_no_secret" = "skipped_no_secret";
    const contentType = request.headers.get("content-type") ?? "unknown";
    const rawBody = await request.text();
    const payload = parseYooMoneyWebhookBody(rawBody);
    orderIdForLog = payload.label;
    paymentLog("info", "yoomoney:webhook incoming", {
      verificationMode,
      contentType,
      label: payload.label,
      orderId: payload.label,
      operationId: payload.operation_id,
      amount: payload.amount,
      withdrawAmount: payload.withdraw_amount,
      currency: payload.currency,
    });

    const secret = process.env.YOOMONEY_NOTIFICATION_SECRET?.trim();
    if (!secret) {
      verificationMode = "skipped_no_secret";
      paymentLog("warn", "yoomoney:webhook secret missing, sha1 skipped", {
        verificationMode,
        orderId: payload.label,
      });
    } else {

      if (!payload.label || !/^\d+$/.test(payload.label)) {
          paymentLog("warn", "yoomoney:webhook rejected invalid label", {
              label: payload.label,
              orderId: payload.label,
          });
          return NextResponse.json({ok: false, message: "Invalid label"}, {status: 400});
      }


      if (payload.sign) {
        verificationMode = "sign";
        const verify = verifyYooMoneySign(payload, secret);
        if (!verify.ok) {
          paymentLog("warn", "yoomoney:webhook rejected invalid sign", {
            verificationMode,
            label: payload.label,
            orderId: payload.label,
            operationId: payload.operation_id,
            receivedSign: verify.receivedSign || "(empty)",
            computedSign: verify.computedSign,
          });
          return NextResponse.json({ ok: false, message: "Invalid sign" }, { status: 401 });
        }

        paymentLog("info", "yoomoney:webhook sign verified", {
          verificationMode,
          label: payload.label,
          orderId: payload.label,
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
            orderId: payload.label,
            operationId: payload.operation_id,
            receivedHash: verify.receivedHash || "(empty)",
            computedHash: verify.computedHash,
          });
          return NextResponse.json({ ok: false, message: "Invalid sha1_hash" }, { status: 401 });
        }
        paymentLog("warn", "yoomoney:webhook sha1 verified (legacy mode)", {
          verificationMode,
          label: payload.label,
          orderId: payload.label,
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

    const netAmountParsed = Number(payload.amount);
    const withdrawAmount = Number(payload.withdraw_amount);
    const orderAmount = Number(order.amount);

    if (!Number.isFinite(withdrawAmount) || withdrawAmount <= 0) {
      paymentLog("warn", "yoomoney:webhook rejected invalid withdraw_amount", {
        verificationMode,
        orderId: payload.label,
        withdrawAmountRaw: payload.withdraw_amount,
      });
      return NextResponse.json(
        { ok: false, message: "Invalid withdraw_amount" },
        { status: 400 },
      );
    }

    if (!Number.isFinite(netAmountParsed) || netAmountParsed <= 0) {
      paymentLog("warn", "yoomoney:webhook rejected invalid amount (net)", {
        verificationMode,
        orderId: payload.label,
        amountRaw: payload.amount,
      });
      return NextResponse.json({ ok: false, message: "Invalid amount" }, { status: 400 });
    }
    const netAmount = netAmountParsed;

    if (Math.round(withdrawAmount * 100) !== Math.round(orderAmount * 100)) {
      paymentLog("warn", "yoomoney:webhook rejected amount mismatch", {
        verificationMode,
        orderId: payload.label,
        withdrawAmount,
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
        net_amount: netAmount,
      },
    });
    paymentLog("info", "yoomoney:webhook order marked paid", {
      verificationMode,
      orderId: payload.label,
      operationId: payload.operation_id,
      netAmount,
      withdrawAmount,
    });

    return NextResponse.json({ ok: true });
  } catch (error) {
    paymentLog("error", "yoomoney:webhook processing error", {
      verificationMode: "skipped_no_secret",
      orderId: orderIdForLog,
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
