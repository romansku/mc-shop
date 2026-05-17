import "server-only";

import { prisma } from "./prisma";
import { toMshopItemPackModel, type MshopItemPackModel } from "@/app/models/mshopItemPack";

export async function getItemsPackByParentItemId(parentItemId: number): Promise<MshopItemPackModel[]> {
  if (!Number.isInteger(parentItemId) || parentItemId <= 0) {
    return [];
  }

  const links = await prisma.mshop_item_packs.findMany({
    where: { parent_item_id: BigInt(parentItemId) },
    orderBy: { child_item_id: "asc" },
  });

  return links.map(toMshopItemPackModel);
}
