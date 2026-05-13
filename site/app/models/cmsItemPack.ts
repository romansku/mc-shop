import type { mshop_item_packs } from "@prisma/client";

export type CmsItemPackModel = {
  goodsId: number;
  includedItemId: number;
};

export function toCmsItemPackModel(row: mshop_item_packs): CmsItemPackModel {
  return {
    goodsId: Number(row.goods_id),
    includedItemId: Number(row.included_item_id),
  };
}
