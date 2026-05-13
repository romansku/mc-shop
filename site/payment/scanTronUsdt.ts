import "server-only";

const TRON_USDT_CONTRACT =
  process.env.TRON_USDT_CONTRACT_ADDRESS?.trim() ||
  "TXLAQ63Xg1NAzckPwKHvzw7CSEmLMEqcdj";

type Trc20Tx = {
  transaction_id?: string;
  to?: string;
  token_info?: {
    address?: string;
    decimals?: number | string;
    symbol?: string;
  };
  value?: string;
  block_timestamp?: number;
};

type TronGridResponse = {
  data?: Trc20Tx[];
};

function normalizeAddress(address: string): string {
  return address.trim();
}

function toTokenAmount(valueRaw: string | undefined, decimalsRaw: number | string | undefined): number | null {
  if (!valueRaw) return null;
  const decimals = Number(decimalsRaw ?? 6);
  if (!Number.isInteger(decimals) || decimals < 0) return null;

  const base = Number(valueRaw);
  if (!Number.isFinite(base)) return null;
  return base / 10 ** decimals;
}

export async function findIncomingUsdtTransfer(params: {
  address: string;
  expectedAmount: number;
  fromTimestampMs: number;
}): Promise<{ txHash: string; amount: number } | null> {
  const address = normalizeAddress(params.address);
  const url = new URL(
    `https://api.trongrid.io/v1/accounts/${address}/transactions/trc20`,
  );
  url.searchParams.set("limit", "50");
  url.searchParams.set("only_to", "true");
  url.searchParams.set("min_timestamp", String(params.fromTimestampMs));
  url.searchParams.set("contract_address", TRON_USDT_CONTRACT);

  const headers: Record<string, string> = {};
  const tronKey = process.env.TRONGRID_API_KEY?.trim();
  if (tronKey) {
    headers["TRON-PRO-API-KEY"] = tronKey;
  }

  const response = await fetch(url.toString(), {
    method: "GET",
    headers,
    cache: "no-store",
  });
  if (!response.ok) {
    throw new Error(`TronGrid error: ${response.status}`);
  }

  const payload = (await response.json()) as TronGridResponse;
  const rows = Array.isArray(payload.data) ? payload.data : [];
  const expected = Number(params.expectedAmount.toFixed(6));

  for (const tx of rows) {
    if (!tx.transaction_id || !tx.to) continue;
    if (normalizeAddress(tx.to) !== address) continue;

    const contract = tx.token_info?.address;
    if (contract && contract !== TRON_USDT_CONTRACT) continue;

    const amount = toTokenAmount(tx.value, tx.token_info?.decimals);
    if (amount === null) continue;

    // Для статического сценария клиент платит точную сумму до 6 знаков.
    const normalized = Number(amount.toFixed(6));
    if (Math.abs(normalized - expected) <= 0.000001) {
      return { txHash: tx.transaction_id, amount: normalized };
    }
  }

  return null;
}

export function tronExplorerUrl(txHash: string): string {
  return `https://tronscan.org/#/transaction/${encodeURIComponent(txHash)}`;
}
