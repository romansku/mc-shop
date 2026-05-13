"use client";

import styles from "./Payment.module.css";
import ykStyles from "./YooKassaPayment.module.css";

type YooKassaPaymentProps = {
  onBack: () => void;
};

export default function YooKassaPayment({ onBack }: YooKassaPaymentProps) {
  return (
    <div>
      <button type="button" className={styles.back} onClick={onBack}>
        ← Назад к выбору
      </button>
      <p className={ykStyles.note}>
        Оплата картой через ЮKassa будет доступна в следующем обновлении.
      </p>
      <button type="button" className={ykStyles.stubButton} disabled>
        Оплатить через ЮKassa
      </button>
    </div>
  );
}
