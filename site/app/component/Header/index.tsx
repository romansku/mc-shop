"use client";

import Link from "next/link";
import Image from "next/image";
import { useState } from "react";
import { useCartState } from "@/app/state/cartState";
import styles from "./Header.module.css";

const navLinks = [
  { href: "/", label: "Главная" },
  { href: "/page/cart", label: "Корзина" },
  { href: "/page/shop", label: "Магазин" },
  { href: "/page/rules", label: "Правила" },
];

export default function Header() {
  const [copyStatus, setCopyStatus] = useState<"success" | "error" | null>(null);
  const { items } = useCartState();

  async function handleCopyServerAddress() {
    try {
      await navigator.clipboard.writeText("play.game-24.org");
      setCopyStatus("success");
    } catch {
      setCopyStatus("error");
    }
    setTimeout(() => setCopyStatus(null), 1600);
  }

  return (
    <header className={styles.header}>
      <div className={styles.container}>
        <div className={styles.brand}>
          <Link href="/" className={styles.logoLink} aria-label="На главную">
            <Image
              src="/server-name.png"
              alt="RIFT MC"
              width={320}
              height={80}
              priority
              className={styles.logoImage}
            />
          </Link>
          <div className={styles.serverWrap}>
            <span
              role="button"
              tabIndex={0}
              onClick={() => void handleCopyServerAddress()}
              onKeyDown={(event) => {
                if (event.key === "Enter" || event.key === " ") {
                  event.preventDefault();
                  void handleCopyServerAddress();
                }
              }}
              className={styles.serverLink}
              title="Нажмите, чтобы скопировать адрес сервера"
              aria-label="Скопировать адрес сервера play.game-24.org"
            >
              play.game-24.org
            </span>
            {copyStatus ? (
              <span
                className={`${styles.copyToast} ${copyStatus === "success" ? styles.copyToastSuccess : styles.copyToastError}`}
                role="status"
                aria-live="polite"
              >
                {copyStatus === "success" ? "Copied" : "Не удалось скопировать"}
              </span>
            ) : null}
          </div>
        </div>
        <nav className={styles.nav}>
          {navLinks.map((item) => (
            <Link key={item.href} href={item.href} className={styles.navLink}>
              {item.label}
              {item.href === "/page/cart" ? (
                <span className={styles.cartBadge} aria-label={`Товаров в корзине: ${items.length}`}>
                  {items.length}
                </span>
              ) : null}
            </Link>
          ))}
        </nav>
      </div>
    </header>
  );
}
