'use client';

import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import OrderCancelButton from '@/components/order/OrderCancelButton';
import styles from './page.module.css';
import { createStripeCheckoutSession } from '@/lib/api';
import { useState } from 'react';

function money(v: string | null): string {
  if (v == null || v === '') return '—';
  const n = Number(v);
  if (!Number.isFinite(n)) return '—';
  return `$${n.toFixed(2)}`;
}

function paymentWindowText(flash = false): string {
  const raw = flash
    ? process.env.NEXT_PUBLIC_ORDER_FLASH_PAY_TIMEOUT_MINUTES?.trim()
    : process.env.NEXT_PUBLIC_ORDER_PAY_TIMEOUT_MINUTES?.trim();
  const fallback = flash ? 15 : 30;
  const minutes = raw ? Number(raw) : fallback;
  if (!Number.isFinite(minutes) || minutes <= 0) {
    return '30 minutes';
  }
  if (minutes % 60 === 0) {
    const hours = minutes / 60;
    return `${hours} hour${hours > 1 ? 's' : ''}`;
  }
  return `${Math.round(minutes)} minute${Math.round(minutes) > 1 ? 's' : ''}`;
}

export default function PayPanel() {
  const search = useSearchParams();
  const orderSn = search.get('orderSn')?.trim() || '';
  const amountRaw = search.get('amount')?.trim() ?? '';
  const isFlashOrder = search.get('flash') === '1';
  const [pending, setPending] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  async function goStripeCheckout() {
    if (!orderSn || pending) return;
    setPending(true);
    setErrorMsg('');
    try {
      const data = await createStripeCheckoutSession(orderSn);
      window.location.href = data.checkoutUrl;
    } catch (e) {
      setErrorMsg(e instanceof Error ? e.message : 'Failed to start Stripe checkout');
      setPending(false);
    }
  }

  if (!orderSn) {
    return (
      <section className={styles.card}>
        <div className={styles.missing}>
          <p>No order number in the link.</p>
          <p>
            <Link href="/order/confirm" className={styles.backBtn}>
              Back to confirm order
            </Link>
          </p>
        </div>
      </section>
    );
  }

  return (
    <section className={styles.card}>
      <div className={styles.head}>
        <h1 className={styles.title}>Payment</h1>
        <span className={styles.badge}>Cashier</span>
      </div>

      <div className={styles.successBanner}>
        <div className={styles.bannerText}>
          <p>
            Order placed successfully. Please complete payment soon. Order no.:{' '}
            <span className={styles.orderSn}>{orderSn}</span>
          </p>
          <p className={styles.amountRow}>Amount due: {money(amountRaw)}</p>
          <p className={styles.hint}>
            {isFlashOrder
              ? `Flash sale order — complete payment within ${paymentWindowText(true)} or it will be cancelled automatically.`
              : `Please complete payment within ${paymentWindowText()} or the order may be cancelled.`}
          </p>
        </div>
      </div>

      <div className={styles.block}>
        <h2>Payment methods</h2>
        <div className={styles.methods}>
          <button type="button" className={styles.methodBtn} onClick={goStripeCheckout} disabled={pending}>
            {pending ? 'Redirecting to Stripe...' : 'Pay with card (Stripe)'}
          </button>
        </div>
        {!!errorMsg && <p className={styles.hint}>{errorMsg}</p>}
      </div>

      <div className={styles.footer}>
        <div className={styles.footerLinks}>
          <Link href="/" className={styles.backBtn}>
            Home
          </Link>
          <Link href="/cart/list" className={styles.backBtn}>
            Continue shopping
          </Link>
        </div>
        <OrderCancelButton
          orderSn={orderSn}
          isSeckill={isFlashOrder}
          redirectTo="/order?status=closed"
          label="Don't want it — cancel order"
        />
      </div>
    </section>
  );
}
