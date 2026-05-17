import { NextResponse } from "next/server";
import { prisma } from "@/app/dao/prisma";
import { parseYooMoneyWebhookBody, verifyYooMoneySha1 } from "@/payment/yoomoneyWebhook";

export async function POST(request: Request) {
  try {
    const rawBody = await request.text();
    const payload = parseYooMoneyWebhookBody(rawBody);

    if (!payload.label || !/^\d+$/.test(payload.label)) {
      return NextResponse.json({ ok: false, message: "Invalid label" }, { status: 400 });
    }

    const secret = process.env.YOOMONEY_NOTIFICATION_SECRET?.trim();
    if (secret && !verifyYooMoneySha1(payload, secret)) {
      return NextResponse.json({ ok: false, message: "Invalid sha1_hash" }, { status: 401 });
    }

    const orderId = BigInt(payload.label);
    const order = await prisma.mshop_player_orders.findUnique({
      where: { id: orderId },
    });

    if (!order) {
      return NextResponse.json({ ok: false, message: "Order not found" }, { status: 404 });
    }

    const paidAmount = Number(payload.amount);
    const orderAmount = Number(order.amount);
    if (!Number.isFinite(paidAmount) || paidAmount <= 0) {
      return NextResponse.json({ ok: false, message: "Invalid amount" }, { status: 400 });
    }
    if (Math.round(paidAmount * 100) !== Math.round(orderAmount * 100)) {
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

    return NextResponse.json({ ok: true });
  } catch (error) {
    return NextResponse.json(
      {
        ok: false,
        message: error instanceof Error ? error.message : "Webhook processing failed",
      },
      { status: 500 },
    );
  }
}
