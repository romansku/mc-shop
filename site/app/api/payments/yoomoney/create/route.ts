import { NextResponse } from "next/server";
import { createYooMoneyOrder } from "@/payment/createYooMoneyOrder";
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

    const payload = await createYooMoneyOrder(validated);
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
