import "server-only";

import { prisma } from "./prisma";
import { toMshopItemModel, type MshopItemModel } from "@/app/models/mshopItem";

export async function getAllItems(): Promise<MshopItemModel[]> {
  const items = await prisma.mshop_items.findMany({
    orderBy: { id: "asc" },
  });

  return items.map(toMshopItemModel);
}

export async function getItemById(id: number): Promise<MshopItemModel | null> {
  if (!Number.isInteger(id) || id <= 0) {
    return null;
  }

  const item = await prisma.mshop_items.findUnique({
    where: { id: BigInt(id) },
  });

  return item ? toMshopItemModel(item) : null;
}
