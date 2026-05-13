import styles from "./Footer.module.css";

const links = {
  telegram: "https://t.me/minecraft_hub_news",
  discord: "https://discord.gg/blockverse",
};

export default function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.container}>
        <div className={styles.infoBlock}>
          <h2 className={styles.title}>Контакты</h2>
          <p className={styles.text}>
            Нужна помощь с покупкой или выдачей привилегий? Напишите нам в
            соцсети - обычно отвечаем быстро.
          </p>
        </div>

        <div className={styles.linksBlock}>
          <a
            href={links.telegram}
            target="_blank"
            rel="noreferrer"
            className={styles.link}
          >
            Telegram канал
          </a>
          <a
            href={links.discord}
            target="_blank"
            rel="noreferrer"
            className={styles.link}
          >
            Discord канал
          </a>
        </div>
      </div>
    </footer>
  );
}
