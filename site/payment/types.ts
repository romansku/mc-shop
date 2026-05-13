export type CartLineSnapshot = {
  id: number;
  name: string;
  price: number;
};

export type CreatePaymentRequestBody = {
  items: CartLineSnapshot[];
  totalUsd: number;
  playerLogin: string;
};

export type CreatePaymentResponseBody = {
  ok: true;
  paymentId: string;
  provider: "NOWPAYMENTS" | "STATIC_SOL" | "STATIC_USDT_TRC20";
  payCurrency: string;
  payAddress: string | null;
  payAmount: string;
  invoiceUrl: string | null;
  fiatAmountUsd: string;
  hint?: string;
};

export type PaymentStatusResponseBody = {
  ok: true;
  paymentId: string;
  provider: "NOWPAYMENTS" | "STATIC_SOL" | "STATIC_USDT_TRC20";
  status: string;
  chainTxHash: string | null;
  explorerUrl: string | null;
};

export type NowpaymentsCreatePaymentResponse = {
  payment_id?: string | number;
  pay_address?: string;
  pay_amount?: number | string;
  pay_currency?: string;
  invoice_url?: string;
  payment_status?: string;
};
