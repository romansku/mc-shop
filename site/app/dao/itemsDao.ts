import "server-only";

import { prisma } from "./prisma";
import { toCmsItemModel, type CmsItemModel } from "@/app/models/cmsItem";

export async function getAllItems(): Promise<CmsItemModel[]> {
  const items = await prisma.cms_item.findMany({
    orderBy: { id: "asc" },
  });

  return items.map(toCmsItemModel);
}

export async function getItemById(id: number): Promise<CmsItemModel | null> {
  if (!Number.isInteger(id) || id <= 0) {
    return null;
  }

  const item = await prisma.cms_item.findUnique({
    where: { id: BigInt(id) },
  });

  return item ? toCmsItemModel(item) : null;
}
