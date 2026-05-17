import { NextResponse } from "next/server";
import { prisma } from "@/app/dao/prisma";
import { toMshopCatalogItemModel } from "@/app/models/mshopCatalogItem";

type CreateMshopItemRequest = {
  name?: string;
  description?: string;
  favorite?: boolean;
  prioritization?: number;
  price?: number;
  imageLink?: string | null;
  itemType?: "COMMAND" | "ITEM" | "PACK";
  data?: string;
  amount?: number;
  active?: boolean;
};

export async function GET() {
  const items = await prisma.mshop_items.findMany({
    orderBy: { prioritization: "asc" },
  });

  return NextResponse.json({
    ok: true,
    items: items.map(toMshopCatalogItemModel),
  });
}

export async function POST(request: Request) {
  try {
    const body = (await request.json()) as CreateMshopItemRequest;

    if (!body.name || body.name.trim().length === 0) {
      throw new Error("Поле name обязательно");
    }
    if (!body.description || body.description.trim().length === 0) {
      throw new Error("Поле description обязательно");
    }
    if (body.price === undefined || Number.isNaN(Number(body.price))) {
      throw new Error("Поле price обязательно и должно быть числом");
    }

    const created = await prisma.mshop_items.create({
      data: {
        item_type: body.itemType ?? "PACK",
        data: body.data?.trim() || body.name.trim(),
        amount: Number.isInteger(body.amount) && Number(body.amount) > 0 ? Number(body.amount) : 1,
        name: body.name.trim(),
        description: body.description.trim(),
        favorite: Boolean(body.favorite),
        prioritization: Number(body.prioritization ?? 0),
        price: body.price,
        image_link: body.imageLink ?? null,
        active: Boolean(body.active),
      },
    });

    return NextResponse.json({ ok: true, item: toMshopCatalogItemModel(created) }, { status: 201 });
  } catch (error) {
    return NextResponse.json(
      {
        ok: false,
        message: error instanceof Error ? error.message : "Не удалось создать товар",
      },
      { status: 400 },
    );
  }
}
