import type { NowpaymentsCreatePaymentResponse } from "./types";

const API_BASE = "https://api.nowpayments.io/v1";

export async function createNowpaymentsPayment(params: {
  apiKey: string;
  priceAmount: number;
  priceCurrency: string;
  payCurrency: string;
  orderId: string;
  ipnCallbackUrl: string;
  orderDescription: string;
}): Promise<NowpaymentsCreatePaymentResponse> {
  const response = await fetch(`${API_BASE}/payment`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "x-api-key": params.apiKey,
    },
    body: JSON.stringify({
      price_amount: params.priceAmount,
      price_currency: params.priceCurrency.toLowerCase(),
      pay_currency: params.payCurrency,
      order_id: params.orderId,
      ipn_callback_url: params.ipnCallbackUrl,
      order_description: params.orderDescription,
    }),
  });

  const data = (await response.json()) as NowpaymentsCreatePaymentResponse & {
    message?: string;
  };

  if (!response.ok) {
    throw new Error(
      typeof data.message === "string"
        ? data.message
        : `NOWPayments error: ${response.status}`,
    );
  }

  return data;
}
