'use client';

import { useState } from 'react';
import styles from './TopBanner.module.css';

const STATIC = '/static/index/img';

export default function TopBanner() {
  const [visible, setVisible] = useState(true);

  if (!visible) return null;

  return (
    <div className={styles.wrapper}>
      <a href="/promo" className={styles.link}>
        <img src={`${STATIC}/img_01.png`} alt="Promo" />
      </a>
      <button
        type="button"
        className={styles.close}
        onClick={() => setVisible(false)}
        aria-label="Close banner"
      >
        ×
      </button>
    </div>
  );
}
