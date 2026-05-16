import type { mshop_items, mshop_item_item_types } from "@prisma/client";

export type CmsItemModel = {
  id: number;
  itemType: mshop_item_item_type;
  data: string;
  amount: number;
};

export function toCmsItemModel(row: mshop_item): CmsItemModel {
  return {
    id: Number(row.id),
    itemType: row.item_type,
    data: row.data,
    amount: row.amount,
  };
}
