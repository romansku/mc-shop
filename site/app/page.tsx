import HeroBanner from "./component/HeroBanner";
import PopularProducts from "./component/PopularProducts";
import styles from "./page.module.css";
import { getAllFavorites } from "./dao/itemsCatalogDao";

/** Avoid Prisma at `next build` (no DATABASE_URL in CI/Docker build). */
export const dynamic = "force-dynamic";

export default async function Home() {
  const favoriteItems = await getAllFavorites();

  return (
    <div className={styles.page}>
      <main className={`${styles.main} ${styles.homeMain}`}>
        <HeroBanner />
        <section className={styles.introSection}>
          <h1 className={styles.introTitle}>
            Rift MC - сервер про нормальное Minecraft-выживание
          </h1>
          <p className={styles.introText}>
            Без модов, без Pay2Win и без продажи силы. Вещи добываются руками,
            базы строятся неделями, а экономика формируется самими игроками.
          </p>
          <p className={styles.introText}>
            Мы сохраняем ванильный дух Minecraft и добавляем только то, что
            делает игру удобнее: приваты, дома, телепорты и базовые команды.
            Донат дает комфорт и косметику, но не преимущество в бою.
          </p>
          <p className={styles.introHint}>
            PvP разрешено, но мы за честную игру и уважение. И да - без вайпов:
            если строите большое, оно останется надолго.
          </p>
        </section>
        {favoriteItems.length > 0 ? (
          <PopularProducts products={favoriteItems} />
        ) : null}
      </main>
    </div>
  );
}
