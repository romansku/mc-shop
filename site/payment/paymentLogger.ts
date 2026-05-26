import "server-only";

import { appendFile, mkdir } from "node:fs/promises";
import path from "node:path";

type PaymentLogLevel = "info" | "warn" | "error";

function resolveLogDir(): string {
  const configured = process.env.PAYMENT_LOG_DIR?.trim();
  if (!configured) {
    return path.join(process.cwd(), "logs");
  }
  return path.resolve(process.cwd(), configured);
}

function getMonthlyLogPath(date: Date): string {
  const year = date.getUTCFullYear();
  const month = String(date.getUTCMonth() + 1).padStart(2, "0");
  return path.join(resolveLogDir(), `payments-${year}-${month}.log`);
}

function toSerializable(value: unknown): unknown {
  if (value instanceof Error) {
    return {
      name: value.name,
      message: value.message,
      stack: value.stack,
    };
  }
  return value;
}

function serializeMeta(meta: Record<string, unknown> | undefined): string {
  if (!meta) {
    return "{}";
  }

  return JSON.stringify(meta, (_key, value) => {
    if (typeof value === "bigint") {
      return value.toString();
    }
    return toSerializable(value);
  });
}

async function appendPaymentLine(line: string): Promise<void> {
  await mkdir(resolveLogDir(), { recursive: true });
  const logPath = getMonthlyLogPath(new Date());
  await appendFile(logPath, line, { encoding: "utf8" });
}

export function paymentLog(
  level: PaymentLogLevel,
  event: string,
  meta?: Record<string, unknown>,
): void {
  const timestamp = new Date().toISOString();
  const payload = serializeMeta(meta);
  const line = `${timestamp} ${level.toUpperCase()} ${event} ${payload}\n`;

  const method =
    level === "error" ? console.error : level === "warn" ? console.warn : console.info;
  method(`[payment] ${event}`, meta ?? {});

  void appendPaymentLine(line).catch((error) => {
    console.error("[payment] failed to write log file", error);
  });
}
