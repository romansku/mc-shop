import styles from "./Footer.module.css";

const links = {
  telegram: "https://t.me/mc_rift",
  discord: "https://discord.gg/CKnu4qdC4z",
  userAgreement:
    "https://mc-s3.game-24.org/rift-mc/docs/user_agreement.pdf",
  privacy: "https://mc-s3.game-24.org/rift-mc/docs/privacy.pdf",
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
            Telegram
          </a>
          <a
            href={links.discord}
            target="_blank"
            rel="noreferrer"
            className={styles.link}
          >
            Discord
          </a>
        </div>

        <nav className={styles.legalBlock} aria-label="Документы">
          <a
            href={links.userAgreement}
            target="_blank"
            rel="noreferrer"
            className={styles.legalLink}
          >
            Пользовательское соглашение
          </a>
          <span className={styles.legalSep} aria-hidden="true">
            ·
          </span>
          <a
            href={links.privacy}
            target="_blank"
            rel="noreferrer"
            className={styles.legalLink}
          >
            Политика конфиденциальности
          </a>
        </nav>
      </div>
    </footer>
  );
}
