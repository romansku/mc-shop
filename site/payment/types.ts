export type CartLineSnapshot = {
  id: number;
  name: string;
  price: number;
};

export type YooMoneyPaymentType = "YOOMONEY_WALLET" | "CARD";

export type CreateYooMoneyOrderRequestBody = {
  items: CartLineSnapshot[];
  totalAmount: number;
  playerLogin: string;
  email: string;
  paymentType: YooMoneyPaymentType;
};

export type CreateYooMoneyOrderResponseBody = {
  ok: true;
  orderId: string;
  receiver: string;
  sum: string;
  label: string;
  paymentType: YooMoneyPaymentType;
  actionUrl: string;
};
