import "server-only";

import { prisma } from "@/app/dao/prisma";

export async function resolveRegisteredPlayer(
  playerLogin: string,
): Promise<{ ok: true } | { ok: false; message: string }> {
  const username = playerLogin.toLowerCase();

  const player = await prisma.mshop_players.findFirst({
    where: { username },
    select: { registered: true },
  });

  if (!player) {
    return { ok: false, message: "Игрок не найден на сервере" };
  }

  if (!player.registered) {
    return { ok: false, message: "Игрок не зарегистрирован на сервере" };
  }

  return { ok: true };
}
