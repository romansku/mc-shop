import { NextResponse } from "next/server";
import { createYooMoneyOrder } from "@/payment/createYooMoneyOrder";
import { resolveOrderPricing } from "@/payment/resolveOrderPricing";
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

    const validated = validateCreateYooMoneyOrderBody(body);
    if (!validated.ok) {
      return NextResponse.json({ ok: false, message: validated.message }, { status: 400 });
    }

    const pricing = await resolveOrderPricing(validated.itemIds, validated.totalAmountHint);
    if (!pricing.ok) {
      return NextResponse.json({ ok: false, message: pricing.message }, { status: 400 });
    }

    const payload = await createYooMoneyOrder({
      itemIds: pricing.data.itemIds,
      totalAmount: pricing.data.totalAmount,
      playerLogin: validated.playerLogin,
      email: validated.email,
      paymentType: validated.paymentType,
    });
    return NextResponse.json(payload);
  } catch (error) {
    return NextResponse.json(
      {
        ok: false,
        message: error instanceof Error ? error.message : "Не удалось создать заказ",
      },
      { status: 500 },
    );
  }
}
