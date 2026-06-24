import Link from 'next/link';
import MallShell from '@/components/layout/MallShell';
import styles from './page.module.css';

type Search = Record<string, string | string[] | undefined>;

function firstParam(sp: Search, key: string): string {
  const v = sp[key];
  if (Array.isArray(v)) return v[0] || '';
  return v || '';
}

export default function PaymentSuccessPage({ searchParams }: { searchParams: Search }) {
  const paid = firstParam(searchParams, 'paid');
  const orderSn = firstParam(searchParams, 'orderSn');

  return (
    <MallShell>
      <section className={styles.card}>
        <div className={styles.head}>
          <div className={styles.titleRow}>
            <span className={styles.check} aria-hidden="true" />
            <div>
              <h1 className={styles.title}>Payment {paid === '1' ? 'Succeeded' : 'Status Updated'}</h1>
              <p className={styles.subtitle}>Thank you. Your order payment has been received.</p>
            </div>
          </div>
        </div>

        <div className={styles.meta}>
          <p>
            Order No.: <span className={styles.orderSn}>{orderSn || 'Unavailable'}</span>
          </p>
        </div>

        <div className={styles.body}>
          <ol className={styles.steps}>
            <li>Order status is updated to paid.</li>
            <li>Fulfillment workflow will continue in the background.</li>
            <li>You can track progress from your order list.</li>
          </ol>

          <div className={styles.actions}>
            <Link href="/order" className={styles.btnPrimary}>
              View Order List
            </Link>
            <Link href="/" className={styles.btnGhost}>
              Back to Home
            </Link>
          </div>
        </div>
      </section>
    </MallShell>
  );
}
