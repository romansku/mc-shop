export const CDN_COMMON_BASE =
  "https://mc-s3.game-24.org/rift-mc/images/common/";

export const CDN_ITEMS_BASE =
  "https://mc-s3.game-24.org/rift-mc/images/items/";

export function cdnCommon(file: string): string {
  return `${CDN_COMMON_BASE}${file}`;
}

export function cdnItem(file: string): string {
  return `${CDN_ITEMS_BASE}${file}`;
}
