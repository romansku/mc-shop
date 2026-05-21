import Image from "next/image";
import { cdnCommon } from "@/lib/cdnImages";
import styles from "./HeroBanner.module.css";

export default function HeroBanner() {
  return (
    <section className={styles.banner}>
      <div className={styles.imageWrap}>
        <Image
          src={cdnCommon("main-banner.png")}
          alt="Minecraft Item Shop"
          fill
          priority
          unoptimized
          sizes="(max-width: 768px) 100vw, 1200px"
          className={styles.image}
        />
      </div>
    </section>
  );
}
