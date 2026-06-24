'use client';

import Link from 'next/link';
import { useEffect, useMemo, useState } from 'react';
import {
  discountPercent,
  formatPrice,
  formatSeckillStart,
  pad,
  splitCountdown,
} from '@/lib/seckillFormat';
import type { SeckillSku } from '@/types/api';
import styles from './UpcomingSeckillSection.module.css';

type Props = {
  products: SeckillSku[];
};

type SessionGroup = {
  key: string;
  startTime: number;
  endTime?: number;
  items: SeckillSku[];
};

function groupBySession(products: SeckillSku[]): SessionGroup[] {
  const map = new Map<string, SessionGroup>();
  for (const product of products) {
    const startTime = product.startTime ?? 0;
    const key = `${product.promotionSessionId ?? 's'}_${startTime}`;
    const existing = map.get(key);
    if (existing) {
      existing.items.push(product);
    } else {
      map.set(key, {
        key,
        startTime,
        endTime: product.endTime,
        items: [product],
      });
    }
  }
  return Array.from(map.values()).sort((a, b) => a.startTime - b.startTime);
}

function SessionCountdown({ targetMs }: { targetMs: number }) {
  const [parts, setParts] = useState(() => splitCountdown(targetMs - Date.now()));

  useEffect(() => {
    const tick = () => setParts(splitCountdown(targetMs - Date.now()));
    tick();
    const id = setInterval(tick, 1000);
    return () => clearInterval(id);
  }, [targetMs]);

  return (
    <div className={styles.countdownDigits} aria-live="polite">
      <span className={styles.digit}>{pad(parts.hours)}</span>
      <span className={styles.sep}>:</span>
      <span className={styles.digit}>{pad(parts.minutes)}</span>
      <span className={styles.sep}>:</span>
      <span className={styles.digit}>{pad(parts.seconds)}</span>
    </div>
  );
}

export default function UpcomingSeckillSection({ products }: Props) {
  const sessions = useMemo(() => groupBySession(products), [products]);

  if (!sessions.length) {
    return null;
  }

  return (
    <section className={styles.section} aria-labelledby="upcoming-seckill-heading">
      <header className={styles.header}>
        <div className={styles.headerText}>
          <span className={styles.badge}>Coming soon</span>
          <h2 id="upcoming-seckill-heading" className={styles.title}>
            Upcoming flash sales
          </h2>
          <p className={styles.subtitle}>
            Preview deals in the next 3 days — grab when the countdown hits zero
          </p>
        </div>
      </header>

      <div className={styles.sessions}>
        {sessions.map((session) => (
          <article key={session.key} className={styles.sessionBlock}>
            <div className={styles.sessionHead}>
              <div>
                <p className={styles.sessionLabel}>Starts at</p>
                <p className={styles.sessionTime}>{formatSeckillStart(session.startTime)}</p>
              </div>
              <div className={styles.countdownWrap}>
                <span className={styles.countdownLabel}>Starts in</span>
                <SessionCountdown targetMs={session.startTime} />
              </div>
            </div>

            <ul className={styles.grid}>
              {session.items.map((product) => {
                const skuId = product.skuId ?? 0;
                const title =
                  product.skuInfo?.skuTitle || product.skuInfo?.skuName || `Product ${skuId}`;
                const img = product.skuInfo?.skuDefaultImg?.trim() || '';
                const original = product.skuInfo?.price;
                const off = discountPercent(product.seckillPrice, original);
                const limit =
                  product.seckillLimit != null && product.seckillLimit > 0
                    ? Math.floor(product.seckillLimit)
                    : null;

                return (
                  <li key={`${session.key}_${skuId}`} className={styles.card}>
                    <Link href={`/item/${skuId}`} className={styles.mediaLink}>
                      <div className={styles.media}>
                        {img ? (
                          // eslint-disable-next-line @next/next/no-img-element
                          <img src={img} alt={title} loading="lazy" />
                        ) : (
                          <div className={styles.placeholder}>No image</div>
                        )}
                        {off != null && off > 0 && (
                          <span className={styles.discountBadge}>-{off}%</span>
                        )}
                      </div>
                      <div className={styles.body}>
                        <h3 className={styles.productTitle}>{title}</h3>
                        <div className={styles.prices}>
                          <span className={styles.seckillPrice}>
                            {formatPrice(product.seckillPrice)}
                          </span>
                          {original != null && original > (product.seckillPrice ?? 0) && (
                            <span className={styles.originalPrice}>{formatPrice(original)}</span>
                          )}
                        </div>
                        {limit != null && (
                          <p className={styles.meta}>Limit {limit} per order</p>
                        )}
                      </div>
                    </Link>
                    <div className={styles.actions}>
                      <span className={styles.soonTag}>Not started yet</span>
                    </div>
                  </li>
                );
              })}
            </ul>
          </article>
        ))}
      </div>
    </section>
  );
}
