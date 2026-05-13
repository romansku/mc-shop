import type { cms_item, cms_item_item_type } from "@prisma/client";

export type CmsItemModel = {
  id: number;
  itemType: cms_item_item_type;
  data: string;
  amount: number;
};

export function toCmsItemModel(row: cms_item): CmsItemModel {
  return {
    id: Number(row.id),
    itemType: row.item_type,
    data: row.data,
    amount: row.amount,
  };
}
