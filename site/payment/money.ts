/** Нормализация суммы в рублях до копеек (для сравнения и суммирования). */
export function toMinorUnits(rub: number): number {
  return Math.round(rub * 100);
}

export function fromMinorUnits(minor: number): number {
  return minor / 100;
}

export function sumRub(prices: number[]): number {
  const minor = prices.reduce((acc, price) => acc + toMinorUnits(price), 0);
  return fromMinorUnits(minor);
}

export function amountsEqualRub(a: number, b: number): boolean {
  return toMinorUnits(a) === toMinorUnits(b);
}

export function formatOrderAmount(amount: number): string {
  return fromMinorUnits(toMinorUnits(amount)).toFixed(2);
}
