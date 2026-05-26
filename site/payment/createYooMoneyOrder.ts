import "server-only";

import { prisma } from "@/app/dao/prisma";
import { formatOrderAmount } from "./money";
import { paymentLog } from "./paymentLogger";
import type { CreateYooMoneyOrderResponseBody, YooMoneyPaymentType } from "./types";

const YOOMONEY_ACTION_URL = "https://yoomoney.ru/quickpay/confirm";

function getReceiverWallet(): string {
  const wallet = process.env.YOOMONEY_WALLET?.trim();
  if (!wallet) {
    paymentLog("error", "yoomoney:create-order wallet missing");
    throw new Error("Что-то идет не так, попробуйте позже");
  }
  return wallet;
}

export async function createYooMoneyOrder(params: {
  itemIds: number[];
  totalAmount: number;
  playerLogin: string;
  email: string;
  paymentType: YooMoneyPaymentType;
}): Promise<CreateYooMoneyOrderResponseBody> {
  const receiver = getReceiverWallet();
  const amountStr = formatOrderAmount(params.totalAmount);
  paymentLog("info", "yoomoney:create-order start", {
    playerLogin: params.playerLogin,
    itemCount: params.itemIds.length,
    amount: amountStr,
    paymentType: params.paymentType,
  });

  return await prisma.$transaction(async (tx) => {
    const parentItemIds = params.itemIds.map((id) => BigInt(id));
    const packRows = await tx.mshop_item_packs.findMany({
      where: { parent_item_id: { in: parentItemIds } },
      select: { child_item_id: true },
    });

    let childItemIds = Array.from(
      new Set(packRows.map((row) => row.child_item_id.toString())),
    ).map((id) => BigInt(id));

    if (childItemIds.length === 0) {
      childItemIds = parentItemIds;
      paymentLog("info", "yoomoney:create-order fallback to parent items");
    }

    const createdOrder = await tx.mshop_player_orders.create({
      data: {
        user_name: params.playerLogin,
        email: params.email,
        payment_method: "YOOMONEY",
        payment_type: params.paymentType,
        amount: amountStr,
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
    paymentLog("info", "yoomoney:create-order committed", {
      orderId,
      orderItemsCount: childItemIds.length,
    });

    return {
      ok: true,
      orderId,
      receiver,
      sum: amountStr,
      label: orderId,
      paymentType: params.paymentType,
      actionUrl: YOOMONEY_ACTION_URL,
    };
  });
}
