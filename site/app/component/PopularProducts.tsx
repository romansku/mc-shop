"use client";

import Image from "next/image";
import Link from "next/link";
import { useMemo, useRef, useState } from "react";
import styles from "./PopularProducts.module.css";
import type { GoodsCard } from "@/app/dao/goodsDao";
import { useCartState } from "@/app/state/cartState";

type PopularProductsProps = {
  products: GoodsCard[];
};

export default function PopularProducts({ products }: PopularProductsProps) {
  const [lastAddedItem, setLastAddedItem] = useState<string | null>(null);
  const carouselRef = useRef<HTMLDivElement>(null);
  const hasCarousel = products.length > 3;
  const { addItem, hasItem, items } = useCartState();

  const cartTitle = useMemo(() => {
    if (!lastAddedItem) return "Популярные товары";
    return `${lastAddedItem} добавлен в корзину`;
  }, [lastAddedItem]);

  const handleAddToCart = async (product: GoodsCard) => {
    try {
      addItem(product);
      setLastAddedItem(`Товар ${product.name}`);
    } catch {
      setLastAddedItem("Не удалось добавить товар");
    }
  };

  const scrollCarousel = (direction: "left" | "right") => {
    const container = carouselRef.current;
    if (!container) return;

    const distance = Math.max(container.clientWidth - 120, 200);
    const left = direction === "left" ? -distance : distance;
    container.scrollBy({ left, behavior: "smooth" });
  };

  return (
    <section className={styles.section}>
      <div className={styles.topBar}>
        <h2 className={styles.title}>{cartTitle}</h2>
        <Link href="/cart" className={styles.cartLink}>
          В корзине: {items.length}
        </Link>
      </div>

      {hasCarousel ? (
        <div className={styles.carouselActions}>
          <button
            className={styles.carouselButton}
            type="button"
            onClick={() => scrollCarousel("left")}
          >
            ← Назад
          </button>
          <button
            className={styles.carouselButton}
            type="button"
            onClick={() => scrollCarousel("right")}
          >
            Далее →
          </button>
        </div>
      ) : null}

      {products.length === 0 ? (
        <p className={styles.emptyState}>
          Пока нет избранных товаров. Добавьте `favorite = true` в таблице
          `mshop_goods`.
        </p>
      ) : null}

      <div
        ref={carouselRef}
        className={hasCarousel ? styles.carouselTrack : styles.grid}
      >
        {products.map((product) => (
          <article key={product.id} className={styles.card}>
            <div className={styles.imageWrap}>
              {product.imageLink ? (
                <Image
                  src={product.imageLink}
                  alt={product.name}
                  fill
                  className={styles.image}
                  sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw"
                />
              ) : (
                <div className={styles.imagePlaceholder}>Нет изображения</div>
              )}
            </div>
            <div>
              <h3 className={styles.cardTitle}>Товар {product.name}</h3>
              <p className={styles.cardDescription}>
                {product.description ?? "Описание скоро появится."}
              </p>
            </div>
            <div className={styles.cardFooter}>
              <span className={styles.price}>${product.price.toFixed(2)}</span>
              <button
                type="button"
                onClick={() => handleAddToCart(product)}
                className={styles.addButton}
                disabled={hasItem(product.id)}
              >
                {hasItem(product.id) ? "Уже в корзине" : "Добавить в корзину"}
              </button>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
