import Link from "next/link";
import Image from "next/image";
import styles from "./Header.module.css";

const navLinks = [
  { href: "/", label: "Главная" },
  { href: "/page/cart", label: "Корзина" },
  { href: "/page/shop", label: "Магазин" },
  { href: "/page/rules", label: "Правила" },
];

export default function Header() {
  return (
    <header className={styles.header}>
      <div className={styles.container}>
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
        <nav className={styles.nav}>
          {navLinks.map((item) => (
            <Link key={item.href} href={item.href} className={styles.navLink}>
              {item.label}
            </Link>
          ))}
        </nav>
      </div>
    </header>
  );
}
