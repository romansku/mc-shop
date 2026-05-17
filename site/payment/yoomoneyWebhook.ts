import crypto from "node:crypto";

export type YooMoneyWebhookPayload = {
  notification_type: string;
  operation_id: string;
  amount: string;
  withdraw_amount?: string;
  currency: string;
  datetime: string;
  sender: string;
  codepro: string;
  label: string;
  unaccepted?: string;
  sha1_hash?: string;
  sign?: string;
};

function fromUrlEncoded(text: string): Record<string, string> {
  const normalized = text.trim().replace(/^\{/, "").replace(/\}$/, "");
  const params = new URLSearchParams(normalized.includes("&") ? normalized : normalized.replace(/,\s*/g, "&"));
  const out: Record<string, string> = {};
  params.forEach((value, key) => {
    out[key] = value;
  });
  return out;
}

export function parseYooMoneyWebhookBody(rawBody: string): YooMoneyWebhookPayload {
  let parsed: Record<string, unknown> | null = null;

  try {
    parsed = JSON.parse(rawBody) as Record<string, unknown>;
  } catch {
    parsed = null;
  }

  const source = parsed ?? fromUrlEncoded(rawBody);
  const get = (key: string): string => {
    const value = source[key];
    if (typeof value === "string") {
      return value;
    }
    if (typeof value === "number" || typeof value === "boolean") {
      return String(value);
    }
    return "";
  };

  return {
    notification_type: get("notification_type"),
    operation_id: get("operation_id"),
    amount: get("amount"),
    withdraw_amount: get("withdraw_amount"),
    currency: get("currency"),
    datetime: get("datetime"),
    sender: get("sender"),
    codepro: get("codepro"),
    label: get("label"),
    unaccepted: get("unaccepted"),
    sha1_hash: get("sha1_hash"),
    sign: get("sign"),
  };
}

export function verifyYooMoneySha1(payload: YooMoneyWebhookPayload, secret: string): boolean {
  const material = [
    payload.notification_type,
    payload.operation_id,
    payload.amount,
    payload.currency,
    payload.datetime,
    payload.sender,
    payload.codepro,
    secret,
    payload.label,
  ].join("&");

  const computed = crypto.createHash("sha1").update(material, "utf8").digest("hex");
  return computed.toLowerCase() === payload.sha1_hash?.toLowerCase();
}
