import "server-only";

import { prisma } from "./prisma";
import { toCmsItemPackModel, type CmsItemPackModel } from "@/app/models/cmsItemPack";

export async function getItemsPackByGoodsId(goodsId: number): Promise<CmsItemPackModel[]> {
  if (!Number.isInteger(goodsId) || goodsId <= 0) {
    return [];
  }

  const links = await prisma.cms_item_pack.findMany({
    where: { goods_id: BigInt(goodsId) },
    orderBy: { included_item_id: "asc" },
  });

  return links.map(toCmsItemPackModel);
}
