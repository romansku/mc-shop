import "server-only";

import { prisma } from "@/app/dao/prisma";
import { amountsEqualRub, sumRub } from "./money";

export type ResolvedOrderPricing = {
  itemIds: number[];
  totalAmount: number;
};

export async function resolveOrderPricing(
  itemIds: number[],
  totalAmountHint: number,
): Promise<{ ok: true; data: ResolvedOrderPricing } | { ok: false; message: string }> {
  const uniqueIds = [...new Set(itemIds)];
  const idsAsBigInt = uniqueIds.map((id) => BigInt(id));

  const rows = await prisma.mshop_items.findMany({
    where: { id: { in: idsAsBigInt } },
    select: { id: true, price: true, active: true },
  });

  if (rows.length !== uniqueIds.length) {
    return { ok: false, message: "Товар не найден" };
  }

  const byId = new Map(rows.map((row) => [Number(row.id), row]));

  for (const id of uniqueIds) {
    const row = byId.get(id);
    if (!row?.active) {
      return { ok: false, message: "Товар недоступен" };
    }
  }

  const prices = uniqueIds.map((id) => Number(byId.get(id)!.price));
  const serverTotal = sumRub(prices);

  if (!amountsEqualRub(serverTotal, totalAmountHint)) {
    return { ok: false, message: "Сумма заказа не совпадает с корзиной" };
  }

  return {
    ok: true,
    data: {
      itemIds: uniqueIds,
      totalAmount: serverTotal,
    },
  };
}
