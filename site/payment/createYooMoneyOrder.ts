import "server-only";

import { prisma } from "@/app/dao/prisma";
import type { CartLineSnapshot, CreateYooMoneyOrderResponseBody, YooMoneyPaymentType } from "./types";

const YOOMONEY_ACTION_URL = "https://yoomoney.ru/quickpay/confirm";

function getReceiverWallet(): string {
  const wallet = process.env.YOOMONEY_WALLET?.trim();
  if (!wallet) {
    throw new Error("Не задан YOOMONEY_WALLET в .env");
  }
  return wallet;
}

export async function createYooMoneyOrder(params: {
  items: CartLineSnapshot[];
  totalAmount: number;
  playerLogin: string;
  email: string;
  paymentType: YooMoneyPaymentType;
}): Promise<CreateYooMoneyOrderResponseBody> {
  const receiver = getReceiverWallet();

  return await prisma.$transaction(async (tx) => {
    const parentItemIds = params.items.map((item) => BigInt(item.id));
    const packRows = await tx.mshop_item_packs.findMany({
      where: { parent_item_id: { in: parentItemIds } },
      select: { child_item_id: true },
    });

    let childItemIds = Array.from(
      new Set(packRows.map((row) => row.child_item_id.toString())),
    ).map((id) => BigInt(id));

    // Fallback: if pack links are absent, treat selected catalog items as order items.
    if (childItemIds.length === 0) {
      childItemIds = parentItemIds;
    }

    const createdOrder = await tx.mshop_player_orders.create({
      data: {
        user_name: params.playerLogin,
        email: params.email,
        payment_method: "YOOMONEY",
        payment_type: params.paymentType,
        amount: params.totalAmount.toFixed(2),
        amount_currency: "RUB",
        status: "CREATED",
      },
    });

    await tx.mshop_order_items.createMany({
      data: childItemIds.map((itemId) => ({
        order_id: createdOrder.id,
        item_id: itemId,
      })),
      skipDuplicates: true,
    });

    const orderId = String(createdOrder.id);

    return {
      ok: true,
      orderId,
      receiver,
      sum: params.totalAmount.toFixed(2),
      label: orderId,
      paymentType: params.paymentType,
      actionUrl: YOOMONEY_ACTION_URL,
    };
  });
}
