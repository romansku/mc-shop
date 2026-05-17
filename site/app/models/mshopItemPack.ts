import type { mshop_item_packs } from "@prisma/client";

export type MshopItemPackModel = {
  parentItemId: number;
  childItemId: number;
};

export function toMshopItemPackModel(row: mshop_item_packs): MshopItemPackModel {
  return {
    parentItemId: Number(row.parent_item_id),
    childItemId: Number(row.child_item_id),
  };
}
