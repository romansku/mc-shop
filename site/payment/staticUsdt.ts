/** USDT TRC20: сумма заказа уже в USD; добавляем уникальный хвост по id платежа (до 6 знаков). */
export function computeStaticUsdtAmount(params: {
  totalUsd: number;
  paymentId: bigint;
}): string {
  if (!Number.isFinite(params.totalUsd) || params.totalUsd <= 0) {
    throw new Error("Order total in USD must be positive");
  }
  const base = params.totalUsd;
  const remainder = params.paymentId % BigInt(997);
  const suffix = (Number(remainder) + 1) * 1e-6;
  const value = base + suffix;
  return value.toFixed(6);
}
