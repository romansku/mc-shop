import { createHmac, timingSafeEqual } from "crypto";

/**
 * NOWPayments: подпись IPN — HMAC-SHA512 от сырого тела запроса с ключом IPN secret.
 * Заголовок: x-nowpayments-sig (hex).
 */
export function verifyNowpaymentsIpnSignature(
  rawBody: string,
  signatureHeader: string | null,
  ipnSecret: string,
): boolean {
  if (!signatureHeader || !ipnSecret) {
    return false;
  }

  const expected = createHmac("sha512", ipnSecret)
    .update(rawBody)
    .digest("hex");

  try {
    const a = Buffer.from(expected, "hex");
    const b = Buffer.from(signatureHeader, "hex");
    if (a.length !== b.length) {
      return false;
    }
    return timingSafeEqual(a, b);
  } catch {
    return false;
  }
}
