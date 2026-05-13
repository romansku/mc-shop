import type { cms_goods } from "@prisma/client";

export type CmsGoodsModel = {
  id: number;
  name: string;
  description: string | null;
  favorite: boolean;
  prioritization: number;
  price: number;
  imageLink: string | null;
};

export function toCmsGoodsModel(row: cms_goods): CmsGoodsModel {
  return {
    id: Number(row.id),
    name: row.name,
    description: row.description,
    favorite: row.favorite,
    prioritization: row.prioritization,
    price: Number(row.price),
    imageLink: row.image_link,
  };
}
