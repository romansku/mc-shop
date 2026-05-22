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
          <div className={styles.introInner}>
            <h1 className={styles.introTitle}>
              Rift MC — сервер про Minecraft-выживание
            </h1>
            <p className={styles.introText}>
              Без модов, без Pay2Win и без продажи силы.<br/>
              Вещи добываются руками, базы строятся неделями, а экономика формируется самими игроками.
              <br/><br/>
              Мы сохраняем ванильный дух Minecraft и добавляем только то, что
              делает игру удобнее:
            </p>
            <ul className={styles.introList}>
              <li>большие приваты</li>
              <li>несколько домов</li>
              <li>телепорты</li>
              <li>удобное меню</li>
            </ul>

            <p className={styles.introText}>
              Философия сервера может уместиться в паре фраз:
            </p>

            <ul className={`${styles.introList}`}>
                <li>PvP разрешено, но мы за честную игру и уважение</li>
                <li>Донат даёт комфорт и косметику, но не преимущества в бою и добыче</li>
                <li>Без вайпов: если строите большое, не переживайте о сносе мира</li>
            </ul>
          </div>
        </section>
        {favoriteItems.length > 0 ? (
          <PopularProducts products={favoriteItems} />
        ) : null}
      </main>
    </div>
  );
}
