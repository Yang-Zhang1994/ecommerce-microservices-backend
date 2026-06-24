import { Suspense } from 'react';
import MallShell from '@/components/layout/MallShell';
import styles from './page.module.css';
import PayPanel from './PayPanel';

export const metadata = {
  title: 'Payment',
  description: 'Complete payment for your order',
};

export default function OrderPayPage() {
  return (
    <MallShell>
      <main className={styles.shell}>
        <Suspense
          fallback={
            <section className={styles.card}>
              <div className={styles.fallbackInner}>Loading payment…</div>
            </section>
          }
        >
          <PayPanel />
        </Suspense>
      </main>
    </MallShell>
  );
}
