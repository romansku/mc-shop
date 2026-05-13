import { NextResponse } from "next/server";
import { prisma } from "@/app/dao/prisma";
import { verifyNowpaymentsIpnSignature } from "@/payment/verifyNowpaymentsIpn";

function mapNowpaymentsStatus(status: string | undefined): string {
  switch (status) {
    case "finished":
      return "CONFIRMED";
    case "failed":
    case "refunded":
      return "FAILED";
    case "expired":
      return "EXPIRED";
    case "waiting":
    case "confirming":
    case "confirmed":
    case "sending":
    case "partially_paid":
      return "PROCESSING";
    default:
      return "AWAITING_PAYMENT";
  }
}

export async function POST(request: Request) {
  const rawBody = await request.text();
  const ipnSecret = process.env.NOWPAYMENTS_IPN_SECRET?.trim();
  const signature = request.headers.get("x-nowpayments-sig");

  if (ipnSecret) {
    const ok = verifyNowpaymentsIpnSignature(rawBody, signature, ipnSecret);
    if (!ok) {
      return NextResponse.json({ ok: false, message: "Invalid signature" }, { status: 401 });
    }
  }

  let payload: Record<string, unknown>;
  try {
    payload = JSON.parse(rawBody) as Record<string, unknown>;
  } catch {
    return NextResponse.json({ ok: false, message: "Invalid JSON" }, { status: 400 });
  }

  const orderId = payload.order_id;
  const paymentId = payload.payment_id;
  const paymentStatus = typeof payload.payment_status === "string"
    ? payload.payment_status
    : undefined;

  const nextStatus = mapNowpaymentsStatus(paymentStatus);

  let row = null;
  if (orderId !== undefined && orderId !== null && orderId !== "") {
    try {
      row = await prisma.store_payment.findUnique({
        where: { id: BigInt(String(orderId)) },
      });
    } catch {
      row = null;
    }
  }

  if (!row && paymentId !== undefined && paymentId !== null) {
    row = await prisma.store_payment.findFirst({
      where: { external_payment_id: String(paymentId) },
    });
  }

  if (!row) {
    return NextResponse.json({ ok: false, message: "Payment not found" }, { status: 404 });
  }

  await prisma.store_payment.update({
    where: { id: row.id },
    data: {
      status: nextStatus,
      last_ipn_payload: rawBody,
      ...(paymentId !== undefined && paymentId !== null
        ? { external_payment_id: String(paymentId) }
        : {}),
    },
  });

  return NextResponse.json({ ok: true });
}
