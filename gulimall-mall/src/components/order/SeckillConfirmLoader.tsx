'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import SeckillConfirmPanel from '@/components/order/SeckillConfirmPanel';
import { waitForSeckillOrderConfirm } from '@/lib/api';
import type { SeckillOrderConfirmData } from '@/types/api';
import styles from '@/app/order/confirm/page.module.css';

type Props = {
  orderSn: string;
};

export default function SeckillConfirmLoader({ orderSn }: Props) {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [confirm, setConfirm] = useState<SeckillOrderConfirmData | null>(null);
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    let cancelled = false;
    void (async () => {
      setLoading(true);
      setErrorMsg('');
      setConfirm(null);
      const result = await waitForSeckillOrderConfirm(orderSn);
      if (cancelled) {
        return;
      }
      if (result.unauthorized) {
        router.replace(
          `/login?redirect=${encodeURIComponent(`/order/seckill/confirm?orderSn=${encodeURIComponent(orderSn)}`)}`,
        );
        return;
      }
      if (result.ok && result.data) {
        setConfirm(result.data);
        setLoading(false);
        return;
      }
      setErrorMsg(result.msg || 'This order is unavailable or has expired.');
      setLoading(false);
    })();
    return () => {
      cancelled = true;
    };
  }, [orderSn, router]);

  if (loading) {
    return (
      <section className={styles.card}>
        <h1 className={styles.title}>Flash sale order</h1>
        <p>Creating your order…</p>
      </section>
    );
  }

  if (!confirm) {
    return (
      <section className={styles.card}>
        <h1 className={styles.title}>Flash sale order</h1>
        <p>{errorMsg || 'This order is unavailable or has expired.'}</p>
        <Link href="/order?status=pending" className={styles.backBtn}>
          My pending orders
        </Link>
      </section>
    );
  }

  return (
    <section className={styles.card}>
      <div className={styles.head}>
        <h1 className={styles.title}>Confirm flash sale order</h1>
        <p className={styles.subtitle}>
          Order <strong>{confirm.orderSn?.trim() || orderSn}</strong> — choose shipping address,
          then proceed to payment.
        </p>
      </div>
      <SeckillConfirmPanel confirm={confirm} />
    </section>
  );
}
