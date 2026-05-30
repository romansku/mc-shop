import type { mshop_items } from "@prisma/client";

export type MshopCatalogItemModel = {
  id: number;
  categoryId: number | null;
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
    categoryId: row.category_id ? Number(row.category_id) : null,
    name: row.name,
    description: row.description,
    favorite: row.favorite,
    prioritization: row.prioritization,
    price: Number(row.price),
    imageLink: row.image_link,
  };
}
