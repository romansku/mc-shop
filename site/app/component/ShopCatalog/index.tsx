"use client";

import Image from "next/image";
import { useMemo, useState } from "react";
import type { ItemCard } from "@/app/dao/itemsCatalogDao";
import { useCartState } from "@/app/state/cartState";
import styles from "./ShopCatalog.module.css";

type ShopCatalogProps = {
  products: ItemCard[];
};

export default function ShopCatalog({ products }: ShopCatalogProps) {
  const [query, setQuery] = useState("");
  const { addItem, hasItem } = useCartState();

  const filtered = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) {
      return products;
    }

    return products.filter((product) =>
      product.name.toLowerCase().includes(normalized),
    );
  }, [products, query]);

  return (
    <section className={styles.section}>
      <div className={styles.topBar}>
        <h1 className={styles.title}>Магазин</h1>
        <input
          className={styles.search}
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Поиск по названию..."
        />
      </div>

      {filtered.length === 0 ? (
        <p className={styles.emptyState}>Товары по запросу не найдены.</p>
      ) : (
        <div className={styles.grid}>
          {filtered.map((product) => (
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

              <h2 className={styles.cardTitle}>{product.name}</h2>
              <p className={styles.cardDescription}>
                {product.description ?? "Описание скоро появится."}
              </p>

              <div className={styles.cardFooter}>
                <span className={styles.price}>{product.price.toFixed(2)} ₽</span>
                <button
                  type="button"
                  className={styles.addButton}
                  onClick={() => addItem(product)}
                  disabled={hasItem(product.id)}
                >
                  {hasItem(product.id) ? "Уже в корзине" : "Добавить в корзину"}
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
