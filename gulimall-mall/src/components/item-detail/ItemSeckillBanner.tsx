'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useMemo, useState } from 'react';
import { seckillKill, waitForSeckillOrderConfirm } from '@/lib/api';
import {
  discountPercent,
  formatPrice,
  formatSeckillStart,
  pad,
  splitCountdown,
} from '@/lib/seckillFormat';
import type { SeckillSku } from '@/types/api';
import styles from './ItemSeckillBanner.module.css';

type Props = {
  skuId: number;
  seckill: SeckillSku;
};

export default function ItemSeckillBanner({ skuId, seckill }: Props) {
  const router = useRouter();
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState('');
  const [msgOk, setMsgOk] = useState(false);

  const live = Boolean(seckill.randomCode);
  const startTime = seckill.startTime ?? 0;
  const endTime = seckill.endTime ?? 0;
  const targetMs = live ? endTime : startTime;
  const original = seckill.skuInfo?.price;
  const off = discountPercent(seckill.seckillPrice, original);

  const [parts, setParts] = useState(() => splitCountdown(targetMs - Date.now()));

  useEffect(() => {
    const tick = () => setParts(splitCountdown(targetMs - Date.now()));
    tick();
    const id = setInterval(tick, 1000);
    return () => clearInterval(id);
  }, [targetMs]);

  const countdownLabel = live ? 'Ends in' : 'Starts in';

  const limit = useMemo(() => {
    if (seckill.seckillLimit == null || seckill.seckillLimit <= 0) return null;
    return Math.floor(seckill.seckillLimit);
  }, [seckill.seckillLimit]);

  async function handleGrab() {
    if (!seckill.randomCode || seckill.promotionSessionId == null) {
      return;
    }
    const killId = `${seckill.promotionSessionId}_${skuId}`;
    setBusy(true);
    setMsg('');
    try {
      const res = await seckillKill(killId, seckill.randomCode, 1);
      if (res.code === 0 && res.orderSn) {
        setMsgOk(true);
        setMsg('Grabbed — preparing checkout…');
        const ready = await waitForSeckillOrderConfirm(res.orderSn);
        if (ready.unauthorized) {
          const redirect = `${window.location.pathname}${window.location.search}`;
          window.location.href = `/login?redirect=${encodeURIComponent(redirect)}`;
          return;
        }
        if (!ready.ok) {
          setMsgOk(false);
          setMsg(ready.msg || 'Order is still being created. Check My orders.');
          return;
        }
        router.push(`/order/seckill/confirm?orderSn=${encodeURIComponent(res.orderSn)}`);
      } else if (res.code === 401) {
        const redirect = `${window.location.pathname}${window.location.search}`;
        window.location.href = `/login?redirect=${encodeURIComponent(redirect)}`;
      } else {
        setMsgOk(false);
        setMsg(res.msg || 'Could not grab this item.');
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.banner} aria-label="Flash sale">
      <div className={styles.topRow}>
        <div>
          <span className={live ? styles.liveBadge : styles.upcomingBadge}>
            {live ? 'Live flash sale' : 'Upcoming flash sale'}
          </span>
          {!live && startTime > 0 && (
            <p className={styles.startAt}>Starts {formatSeckillStart(startTime)}</p>
          )}
        </div>
        <div className={styles.countdown}>
          <span className={styles.countdownLabel}>{countdownLabel}</span>
          <div className={styles.digits}>
            <span>{pad(parts.hours)}</span>
            <span className={styles.sep}>:</span>
            <span>{pad(parts.minutes)}</span>
            <span className={styles.sep}>:</span>
            <span>{pad(parts.seconds)}</span>
          </div>
        </div>
      </div>

      <div className={styles.priceRow}>
        <span className={styles.seckillPrice}>{formatPrice(seckill.seckillPrice)}</span>
        {original != null && original > (seckill.seckillPrice ?? 0) && (
          <span className={styles.originalPrice}>{formatPrice(original)}</span>
        )}
        {off != null && off > 0 && <span className={styles.offBadge}>-{off}%</span>}
        {limit != null && <span className={styles.limit}>Limit {limit} per order</span>}
      </div>

      {live ? (
        <button
          type="button"
          className={styles.grabBtn}
          disabled={busy}
          onClick={handleGrab}
        >
          {busy ? 'Processing…' : 'Grab flash deal'}
        </button>
      ) : (
        <p className={styles.hint}>
          Come back when the sale starts, or check the{' '}
          <Link href="/" className={styles.homeLink}>
            homepage flash sale
          </Link>{' '}
          section.
        </p>
      )}

      {msg && (
        <p className={msgOk ? styles.msgOk : styles.msgErr} role="status">
          {msg}
        </p>
      )}
    </section>
  );
}
