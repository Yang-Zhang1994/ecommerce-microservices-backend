'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useMemo, useState } from 'react';
import { seckillKill, waitForSeckillOrderConfirm } from '@/lib/api';
import { discountPercent, formatPrice, pad } from '@/lib/seckillFormat';
import type { SeckillSku } from '@/types/api';
import styles from './SeckillSection.module.css';

type Props = {
  products: SeckillSku[];
};

type GrabStatus = {
  skuId: number;
  ok: boolean;
  msg: string;
  orderSn?: string;
};

export default function SeckillSection({ products }: Props) {
  const router = useRouter();
  const [hours, setHours] = useState(0);
  const [minutes, setMinutes] = useState(0);
  const [seconds, setSeconds] = useState(0);
  const [statusBySku, setStatusBySku] = useState<Record<number, GrabStatus>>({});
  const [busySkuId, setBusySkuId] = useState<number | null>(null);

  const endTime = useMemo(() => {
    const t = products.find((p) => p.endTime)?.endTime;
    if (t) return t;
    const d = new Date();
    d.setHours(23, 59, 59, 999);
    return d.getTime();
  }, [products]);

  useEffect(() => {
    const tick = () => {
      const diff = Math.max(0, endTime - Date.now());
      setHours(Math.floor(diff / 3600000));
      setMinutes(Math.floor((diff % 3600000) / 60000));
      setSeconds(Math.floor((diff % 60000) / 1000));
    };
    tick();
    const id = setInterval(tick, 1000);
    return () => clearInterval(id);
  }, [endTime]);

  async function handleGrab(product: SeckillSku) {
    const skuId = product.skuId;
    if (!skuId || !product.randomCode || product.promotionSessionId == null) {
      setStatusBySku((prev) => ({
        ...prev,
        [skuId ?? 0]: { skuId: skuId ?? 0, ok: false, msg: 'This deal is not live yet.' },
      }));
      return;
    }
    const killId = `${product.promotionSessionId}_${skuId}`;
    setBusySkuId(skuId);
    setStatusBySku((prev) => {
      const next = { ...prev };
      delete next[skuId];
      return next;
    });
    try {
      const res = await seckillKill(killId, product.randomCode, 1);
      if (res.code === 0 && res.orderSn) {
        setStatusBySku((prev) => ({
          ...prev,
          [skuId]: {
            skuId,
            ok: true,
            msg: 'Grabbed — preparing checkout…',
            orderSn: res.orderSn,
          },
        }));
        const ready = await waitForSeckillOrderConfirm(res.orderSn);
        if (ready.unauthorized) {
          const redirect = `${window.location.pathname}${window.location.search}`;
          window.location.href = `/login?redirect=${encodeURIComponent(redirect)}`;
          return;
        }
        if (!ready.ok) {
          setStatusBySku((prev) => ({
            ...prev,
            [skuId]: {
              skuId,
              ok: false,
              msg: ready.msg || 'Order is still being created. Check My orders.',
            },
          }));
          return;
        }
        router.push(`/order/seckill/confirm?orderSn=${encodeURIComponent(res.orderSn)}`);
      } else if (res.code === 401) {
        const redirect = `${window.location.pathname}${window.location.search}`;
        window.location.href = `/login?redirect=${encodeURIComponent(redirect)}`;
      } else {
        setStatusBySku((prev) => ({
          ...prev,
          [skuId]: { skuId, ok: false, msg: res.msg || 'Could not grab this item.' },
        }));
      }
    } finally {
      setBusySkuId(null);
    }
  }

  if (!products?.length) {
    return null;
  }

  return (
    <section className={styles.section} aria-labelledby="seckill-heading">
      <header className={styles.header}>
        <div className={styles.headerText}>
          <span className={styles.liveBadge}>
            <span className={styles.liveDot} aria-hidden />
            Live now
          </span>
          <h2 id="seckill-heading" className={styles.title}>
            Flash sale
          </h2>
          <p className={styles.subtitle}>Limited stock · one-click grab while the timer runs</p>
        </div>
        <div className={styles.countdown} aria-live="polite">
          <span className={styles.countdownLabel}>Ends in</span>
          <div className={styles.countdownDigits}>
            <span className={styles.digit}>{pad(hours)}</span>
            <span className={styles.sep}>:</span>
            <span className={styles.digit}>{pad(minutes)}</span>
            <span className={styles.sep}>:</span>
            <span className={styles.digit}>{pad(seconds)}</span>
          </div>
        </div>
      </header>

      <ul className={styles.grid}>
        {products.map((product) => {
          const skuId = product.skuId ?? 0;
          const status = statusBySku[skuId];
          const title =
            product.skuInfo?.skuTitle || product.skuInfo?.skuName || `Product ${skuId}`;
          const img = product.skuInfo?.skuDefaultImg?.trim() || '';
          const live = Boolean(product.randomCode);
          const original = product.skuInfo?.price;
          const off = discountPercent(product.seckillPrice, original);
          const limit =
            product.seckillLimit != null && product.seckillLimit > 0
              ? Math.floor(product.seckillLimit)
              : null;

          return (
            <li key={`${product.promotionSessionId ?? 's'}_${skuId}`} className={styles.card}>
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
                <button
                  type="button"
                  className={styles.grabBtn}
                  disabled={!live || busySkuId === skuId}
                  onClick={() => handleGrab(product)}
                >
                  {busySkuId === skuId ? 'Processing…' : live ? 'Grab now' : 'Coming soon'}
                </button>
                {status && (
                  <p className={status.ok ? styles.msgOk : styles.msgErr} role="status">
                    {status.msg}
                    {status.ok && status.orderSn && (
                      <>
                        {' '}
                        <Link
                          href={`/order/seckill/confirm?orderSn=${encodeURIComponent(status.orderSn)}`}
                          className={styles.payLink}
                        >
                          Confirm & pay
                        </Link>
                      </>
                    )}
                  </p>
                )}
              </div>
            </li>
          );
        })}
      </ul>
    </section>
  );
}
