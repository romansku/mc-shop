import ShopCatalog from "../component/ShopCatalog";
import { getAllSortByPrioritization } from "../dao/goodsDao";
import styles from "../page.module.css";

/** Avoid Prisma at `next build` (no DATABASE_URL in CI/Docker build). */
export const dynamic = "force-dynamic";

export default async function ShopPage() {
  const goods = await getAllSortByPrioritization();

  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <ShopCatalog products={goods} />
      </main>
    </div>
  );
}
