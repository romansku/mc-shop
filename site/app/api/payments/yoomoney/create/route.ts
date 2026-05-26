import { NextResponse } from "next/server";
import { createYooMoneyOrder } from "@/payment/createYooMoneyOrder";
import { paymentLog } from "@/payment/paymentLogger";
import { resolveOrderPricing } from "@/payment/resolveOrderPricing";
import { resolveRegisteredPlayer } from "@/payment/resolveRegisteredPlayer";
import { validateCreateYooMoneyOrderBody } from "@/payment/validateYooMoneyOrder";

export async function POST(request: Request) {
  try {
    const body = (await request.json()) as {
      items?: unknown;
      totalAmount?: unknown;
      playerLogin?: unknown;
      email?: unknown;
      paymentType?: unknown;
    };
    paymentLog("info", "yoomoney:create incoming request", {
      itemCount: Array.isArray(body.items) ? body.items.length : 0,
      paymentType: body.paymentType,
      hasPlayerLogin: typeof body.playerLogin === "string" && body.playerLogin.trim().length > 0,
      hasEmail: typeof body.email === "string" && body.email.trim().length > 0,
    });

    const validated = validateCreateYooMoneyOrderBody(body);
    if (!validated.ok) {
      paymentLog("warn", "yoomoney:create validation failed", { message: validated.message });
      return NextResponse.json({ ok: false, message: validated.message }, { status: 400 });
    }

    const player = await resolveRegisteredPlayer(validated.playerLogin);
    if (!player.ok) {
      paymentLog("warn", "yoomoney:create player check failed", {
        login: validated.playerLogin,
        message: player.message,
      });
      return NextResponse.json({ ok: false, message: player.message }, { status: 400 });
    }

    const pricing = await resolveOrderPricing(validated.itemIds, validated.totalAmountHint);
    if (!pricing.ok) {
      paymentLog("warn", "yoomoney:create pricing failed", { message: pricing.message });
      return NextResponse.json({ ok: false, message: pricing.message }, { status: 400 });
    }

    const payload = await createYooMoneyOrder({
      itemIds: pricing.data.itemIds,
      totalAmount: pricing.data.totalAmount,
      playerLogin: validated.playerLogin,
      email: validated.email,
      paymentType: validated.paymentType,
    });
    paymentLog("info", "yoomoney:create order created", {
      orderId: payload.orderId,
      amount: payload.sum,
      paymentType: payload.paymentType,
    });
    return NextResponse.json(payload);
  } catch (error) {
    paymentLog("error", "yoomoney:create internal error", { error });
    return NextResponse.json(
      {
        ok: false,
        message: error instanceof Error ? error.message : "Не удалось создать заказ",
      },
      { status: 500 },
    );
  }
}
