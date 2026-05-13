import type { cms_item_pack } from "@prisma/client";

export type CmsItemPackModel = {
  goodsId: number;
  includedItemId: number;
};

export function toCmsItemPackModel(row: cms_item_pack): CmsItemPackModel {
  return {
    goodsId: Number(row.goods_id),
    includedItemId: Number(row.included_item_id),
  };
}
