import ShopCatalog from "@/app/component/ShopCatalog";
import { getShopCategoriesWithItems } from "@/app/dao/itemsCatalogDao";
import styles from "@/app/page.module.css";

/** Avoid Prisma at `next build` (no DATABASE_URL in CI/Docker build). */
export const dynamic = "force-dynamic";

export default async function ShopPage() {
  const categories = await getShopCategoriesWithItems();

  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <ShopCatalog categories={categories} />
      </main>
    </div>
  );
}
