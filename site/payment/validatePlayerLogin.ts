/** Ник Minecraft (Java): буквы, цифры, подчёркивание, длина 3–32. */
export function validatePlayerLogin(value: unknown):
  | { ok: true; login: string }
  | { ok: false; message: string } {
  if (typeof value !== "string") {
    return { ok: false, message: "Укажите ник игрока" };
  }

  const login = value.trim();
  if (login.length < 3) {
    return { ok: false, message: "Ник слишком короткий (минимум 3 символа)" };
  }
  if (login.length > 32) {
    return { ok: false, message: "Ник слишком длинный (максимум 32 символа)" };
  }
  if (!/^[a-zA-Z0-9_]+$/.test(login)) {
    return {
      ok: false,
      message: "Ник: только латиница, цифры и символ _",
    };
  }

  return { ok: true, login };
}
