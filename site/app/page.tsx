import HeroBanner from "./component/HeroBanner";
import PopularProducts from "./component/PopularProducts";
import styles from "./page.module.css";
import { getAllFavorites } from "./dao/goodsDao";

/** Avoid Prisma at `next build` (no DATABASE_URL in CI/Docker build). */
export const dynamic = "force-dynamic";

export default async function Home() {
  const favoriteGoods = await getAllFavorites();

  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <HeroBanner />
        <PopularProducts products={favoriteGoods} />
      </main>
    </div>
  );
}
