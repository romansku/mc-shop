"use client";

import Image from "next/image";
import { useMemo, useState } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import type { ShopCategorySection } from "@/app/dao/itemsCatalogDao";
import { useCartState } from "@/app/state/cartState";
import styles from "./ShopCatalog.module.css";

type ShopCatalogProps = {
  categories: ShopCategorySection[];
};

export default function ShopCatalog({ categories }: ShopCatalogProps) {
  const [query, setQuery] = useState("");
  const [infoOpen, setInfoOpen] = useState(false);
  const [collapsedCategories, setCollapsedCategories] = useState<Record<string, boolean>>({});
  const { addItem, hasItem } = useCartState();

  const getCategoryKey = (id: number | null): string => {
    return id === null ? "uncategorized" : String(id);
  };

  const isCollapsed = (id: number | null): boolean => {
    return collapsedCategories[getCategoryKey(id)] ?? false;
  };

  const toggleCategory = (id: number | null) => {
    const key = getCategoryKey(id);
    setCollapsedCategories((current) => ({
      ...current,
      [key]: !(current[key] ?? false),
    }));
  };

  const filteredCategories = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    const withFilteredItems = categories.map((category) => {
      if (!normalized) {
        return category;
      }
      return {
        ...category,
        items: category.items.filter((product) =>
          product.name.toLowerCase().includes(normalized),
        ),
      };
    });

    return withFilteredItems.filter((category) => category.items.length > 0);
  }, [categories, query]);

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

      <div className={styles.infoBlock}>
        <button
          type="button"
          className={styles.infoToggle}
          onClick={() => setInfoOpen((open) => !open)}
          aria-expanded={infoOpen}
          aria-controls="shop-purchase-info"
        >
          <span className={styles.burger} aria-hidden="true">
            <span />
            <span />
            <span />
          </span>
          <span className={styles.infoToggleLabel}>Условия покупки</span>
        </button>
        <div
          id="shop-purchase-info"
          className={`${styles.infoPanel} ${infoOpen ? styles.infoPanelOpen : ""}`}
          aria-hidden={!infoOpen}
        >
          <p className={styles.infoText}>
            Все товары в магазине действуют ограниченное время. Если вы покупаете
            позицию, которая у вас уже есть, срок владения продлевается на период,
            указанный для этого товара.
          </p>
        </div>
      </div>

      {filteredCategories.length === 0 ? (
        <p className={styles.emptyState}>Товары по запросу не найдены.</p>
      ) : (
        <div className={styles.categories}>
          {filteredCategories.map((category) => (
            <section key={category.id ?? "uncategorized"} className={styles.categorySection}>
              <div
                className={styles.categoryHeader}
                role="button"
                tabIndex={0}
                onClick={() => toggleCategory(category.id)}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    toggleCategory(category.id);
                  }
                }}
                aria-expanded={!isCollapsed(category.id)}
              >
                <h2 className={styles.categoryTitle}>{category.name}</h2>
                <span className={styles.categoryToggleLabel}>
                  {isCollapsed(category.id) ? "Развернуть" : "Свернуть"}
                </span>
              </div>

              {!isCollapsed(category.id) ? (
                <>
                  <div className={styles.categoryDescription}>
                    <ReactMarkdown remarkPlugins={[remarkGfm]}>
                      {category.description}
                    </ReactMarkdown>
                  </div>

                  <div className={styles.grid}>
                    {category.items.map((product) => (
                      <article key={product.id} className={styles.card}>
                        <div className={styles.imageWrap}>
                          {product.imageLink ? (
                            <Image
                              src={product.imageLink}
                              alt={product.name}
                              fill
                              unoptimized
                              className={styles.image}
                              sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw"
                            />
                          ) : (
                            <div className={styles.imagePlaceholder}>Нет изображения</div>
                          )}
                        </div>

                        <h3 className={styles.cardTitle}>{product.name}</h3>
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
                </>
              ) : null}
            </section>
          ))}
        </div>
      )}
    </section>
  );
}
