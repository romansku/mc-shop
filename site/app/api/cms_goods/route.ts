import { NextResponse } from "next/server";
import { prisma } from "@/app/dao/prisma";
import { toCmsGoodsModel } from "@/app/models/cmsGoods";

type CreateCmsGoodsRequest = {
  name?: string;
  description?: string | null;
  favorite?: boolean;
  prioritization?: number;
  price?: number;
  imageLink?: string | null;
};

export async function GET() {
  const goods = await prisma.mshop_goods.findMany({
    orderBy: { prioritization: "asc" },
  });

  return NextResponse.json({
    ok: true,
    items: goods.map(toCmsGoodsModel),
  });
}

export async function POST(request: Request) {
  try {
    const body = (await request.json()) as CreateCmsGoodsRequest;

    if (!body.name || body.name.trim().length === 0) {
      throw new Error("Поле name обязательно");
    }

    if (body.price === undefined || Number.isNaN(Number(body.price))) {
      throw new Error("Поле price обязательно и должно быть числом");
    }

    const created = await prisma.mshop_goods.create({
      data: {
        name: body.name.trim(),
        description: body.description ?? null,
        favorite: Boolean(body.favorite),
        prioritization: Number(body.prioritization ?? 0),
        price: body.price,
        image_link: body.imageLink ?? null,
      },
    });

    return NextResponse.json({ ok: true, item: toCmsGoodsModel(created) }, { status: 201 });
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
