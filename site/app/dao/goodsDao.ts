import "server-only";

import type { mshop_goods } from "@prisma/client";
import { toCmsGoodsModel, type CmsGoodsModel } from "@/app/models/cmsGoods";
import { prisma } from "./prisma";

export type GoodsCard = CmsGoodsModel;

function toGoodsCard(item: mshop_goods): GoodsCard {
  return toCmsGoodsModel(item);
}

export async function getAllSortByPrioritization(): Promise<GoodsCard[]> {
  const goods = await prisma.mshop_goods.findMany({
    orderBy: {
      prioritization: "asc",
    },
  });

  return goods.map(toGoodsCard);
}

export async function getAllFavorites(): Promise<GoodsCard[]> {
  const goods = await prisma.mshop_goods.findMany({
    where: {
      favorite: true,
    },
    orderBy: {
      prioritization: "asc",
    },
  });

  return goods.map(toGoodsCard);
}

export async function getByIds(ids: number[]): Promise<GoodsCard[]> {
  if (ids.length === 0) {
    return [];
  }

  const idsAsBigInt = ids.map((id) => BigInt(id));
  const goods = await prisma.mshop_goods.findMany({
    where: {
      id: {
        in: idsAsBigInt,
      },
    },
  });

  const byId = new Map(goods.map((item) => [Number(item.id), item]));

  return ids
    .map((id) => byId.get(id))
    .filter((item): item is mshop_goods => Boolean(item))
    .map(toGoodsCard);
}
