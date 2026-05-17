import type { mshop_items } from "@prisma/client";

export type MshopCatalogItemModel = {
  id: number;
  name: string;
  description: string | null;
  favorite: boolean;
  prioritization: number;
  price: number;
  imageLink: string | null;
};

export function toMshopCatalogItemModel(row: mshop_items): MshopCatalogItemModel {
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
