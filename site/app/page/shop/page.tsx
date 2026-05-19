import ShopCatalog from "@/app/component/ShopCatalog";
import { getAllSortByPrioritization } from "@/app/dao/itemsCatalogDao";
import styles from "@/app/page.module.css";

/** Avoid Prisma at `next build` (no DATABASE_URL in CI/Docker build). */
export const dynamic = "force-dynamic";

export default async function ShopPage() {
  const items = await getAllSortByPrioritization();

  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <ShopCatalog products={items} />
      </main>
    </div>
  );
}
