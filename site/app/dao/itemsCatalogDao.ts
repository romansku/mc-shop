import "server-only";

import type { mshop_categories, mshop_items } from "@prisma/client";
import { toMshopCatalogItemModel, type MshopCatalogItemModel } from "@/app/models/mshopCatalogItem";
import { prisma } from "./prisma";

export type ItemCard = MshopCatalogItemModel;
export type ShopCategorySection = {
  id: number | null;
  name: string;
  description: string;
  items: ItemCard[];
};

function toItemCard(item: mshop_items): ItemCard {
  return toMshopCatalogItemModel(item);
}

function toCategorySection(category: mshop_categories & { mshop_items: mshop_items[] }): ShopCategorySection {
  return {
    id: Number(category.id),
    name: category.name,
    description: category.description,
    items: category.mshop_items.map(toItemCard),
  };
}

export async function getAllSortByPrioritization(): Promise<ItemCard[]> {
  const items = await prisma.mshop_items.findMany({
    orderBy: {
      prioritization: "asc",
    },
  });

  return items.map(toItemCard);
}

export async function getShopCategoriesWithItems(): Promise<ShopCategorySection[]> {
  const categories = await prisma.mshop_categories.findMany({
    orderBy: {
      id: "desc",
    },
    include: {
      mshop_items: {
        orderBy: {
          prioritization: "asc",
        },
      },
    },
  });

  const sections = categories.map(toCategorySection);

  const uncategorized = await prisma.mshop_items.findMany({
    where: {
      category_id: null,
    },
    orderBy: {
      prioritization: "asc",
    },
  });

  if (uncategorized.length > 0) {
    sections.push({
      id: null,
      name: "Без категории",
      description: "Товары без назначенной категории.",
      items: uncategorized.map(toItemCard),
    });
  }

  return sections;
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
