import Image from "next/image";
import styles from "./HeroBanner.module.css";

export default function HeroBanner() {
  return (
    <section className={styles.banner}>
      <div className={styles.imageWrap}>
        <Image
          src="/main-banner.png"
          alt="Minecraft Item Shop"
          fill
          priority
          sizes="(max-width: 768px) 100vw, 1200px"
          className={styles.image}
        />
      </div>
    </section>
  );
}
