import RulesContent from "./RulesContent";
import styles from "@/app/page.module.css";

export default function RulesPage() {
  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <RulesContent />
      </main>
    </div>
  );
}
