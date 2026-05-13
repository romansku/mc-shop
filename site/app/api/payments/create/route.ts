import { NextResponse } from "next/server";
import { createCryptoPaymentRecord } from "@/payment/createCryptoPayment";
import { validateCreatePaymentBody } from "@/payment/validateCart";

export async function POST(request: Request) {
  try {
    const body = (await request.json()) as {
      items?: unknown;
      totalUsd?: unknown;
      playerLogin?: unknown;
    };

    const validated = validateCreatePaymentBody(body);
    if (!validated.ok) {
      return NextResponse.json(
        { ok: false, message: validated.message },
        { status: 400 },
      );
    }

    const result = await createCryptoPaymentRecord({
      items: validated.items,
      totalUsd: validated.totalUsd,
      playerLogin: validated.playerLogin,
    });

    return NextResponse.json(result);
  } catch (error) {
    return NextResponse.json(
      {
        ok: false,
        message:
          error instanceof Error
            ? error.message
            : "Не удалось создать платёж",
      },
      { status: 500 },
    );
  }
}
