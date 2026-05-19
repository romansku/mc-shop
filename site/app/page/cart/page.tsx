"use client";

import { useState } from "react";
import Payment from "@/app/component/Payment";
import styles from "./page.module.css";
import { useCartState } from "@/app/state/cartState";

export default function CartPage() {
  const { items, total, removeItem, clearCart } = useCartState();
  const [paymentOpen, setPaymentOpen] = useState(false);
  const [paymentSession, setPaymentSession] = useState(0);

  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <section className={styles.section}>
          <div className={styles.topBar}>
            <h1 className={styles.title}>Корзина</h1>
            <p className={styles.total}>Итого: {total.toFixed(2)} ₽</p>
          </div>

          {items.length === 0 ? (
            <p className={styles.emptyState}>
              Корзина пока пуста. Перейдите в магазин и добавьте товары.
            </p>
          ) : (
            <div className={styles.list}>
              {items.map((item) => (
                <article key={item.id} className={styles.card}>
                  <div>
                    <h2 className={styles.cardTitle}>Товар {item.name}</h2>
                    <p className={styles.cardDescription}>
                      {item.description ?? "Описание скоро появится."}
                    </p>
                  </div>
                  <div className={styles.cardMeta}>
                    <span className={styles.price}>{item.price.toFixed(2)} ₽</span>
                    <button
                      type="button"
                      className={styles.removeButton}
                      onClick={() => removeItem(item.id)}
                    >
                      Убрать из корзины
                    </button>
                  </div>
                </article>
              ))}
              <div className={styles.actions}>
                <button
                  type="button"
                  className={styles.payButton}
                  onClick={() => {
                    setPaymentSession((value) => value + 1);
                    setPaymentOpen(true);
                  }}
                >
                  Оплатить
                </button>
                <button
                  type="button"
                  className={styles.clearButton}
                  onClick={clearCart}
                >
                  Очистить корзину
                </button>
              </div>
            </div>
          )}
        </section>
      </main>

      <Payment
        key={paymentSession}
        open={paymentOpen}
        onClose={() => setPaymentOpen(false)}
        items={items}
        totalUsd={total}
        onPaymentCreated={clearCart}
      />
    </div>
  );
}
