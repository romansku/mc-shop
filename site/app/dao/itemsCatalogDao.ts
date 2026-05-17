import "server-only";

import type { mshop_items } from "@prisma/client";
import { toMshopCatalogItemModel, type MshopCatalogItemModel } from "@/app/models/mshopCatalogItem";
import { prisma } from "./prisma";

export type ItemCard = MshopCatalogItemModel;

function toItemCard(item: mshop_items): ItemCard {
  return toMshopCatalogItemModel(item);
}

export async function getAllSortByPrioritization(): Promise<ItemCard[]> {
  const items = await prisma.mshop_items.findMany({
    orderBy: {
      prioritization: "asc",
    },
  });

  return items.map(toItemCard);
}

export async function getAllFavorites(): Promise<ItemCard[]> {
  const items = await prisma.mshop_items.findMany({
    where: {
      favorite: true,
    },
    orderBy: {
      prioritization: "asc",
    },
  });

  return items.map(toItemCard);
}

export async function getByIds(ids: number[]): Promise<ItemCard[]> {
  if (ids.length === 0) {
    return [];
  }

  const idsAsBigInt = ids.map((id) => BigInt(id));
  const items = await prisma.mshop_items.findMany({
    where: {
      id: {
        in: idsAsBigInt,
      },
    },
  });

  const byId = new Map(items.map((item) => [Number(item.id), item]));

  return ids
    .map((id) => byId.get(id))
    .filter((item): item is mshop_items => Boolean(item))
    .map(toItemCard);
}
