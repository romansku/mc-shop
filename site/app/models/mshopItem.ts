import type { mshop_item_item_type, mshop_items } from "@prisma/client";

export type MshopItemModel = {
  id: number;
  itemType: mshop_item_item_type;
  data: string;
  amount: number;
};

export function toMshopItemModel(row: mshop_items): MshopItemModel {
  return {
    id: Number(row.id),
    itemType: row.item_type,
    data: row.data,
    amount: row.amount,
  };
}
