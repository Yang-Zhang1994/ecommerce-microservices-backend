import Link from 'next/link';
import { redirect } from 'next/navigation';
import MallShell from '@/components/layout/MallShell';
import OrderListPendingActions from '@/components/order/OrderListPendingActions';
import PendingOrderSummary from '@/components/order/PendingOrderSummary';
import { getMyOrdersServer } from '@/lib/api';
import type { OrderListOrder } from '@/types/api';
import styles from './page.module.css';

type Search = Record<string, string | string[] | undefined>;
type StatusTab = 'all' | 'pending' | 'paid' | 'closed';

function firstParam(sp: Search, key: string): string {
  const v = sp[key];
  if (Array.isArray(v)) return v[0] || '';
  return v || '';
}

function normalizeStatusTab(v: string): StatusTab {
  if (v === 'pending' || v === 'paid' || v === 'closed') return v;
  return 'all';
}

function money(v: number | string | undefined | null): string {
  const n = Number(v ?? 0);
  if (!Number.isFinite(n)) return 'CA$0.00';
  return `CA$${n.toFixed(2)}`;
}

function statusText(status: number | undefined): string {
  if (status === 0) return 'Pending payment';
  if (status === 1 || status === 2 || status === 3) return 'Paid';
  if (status === 4 || status === 5) return 'Closed';
  return 'Unknown';
}

function statusClass(status: number | undefined): string {
  if (status === 0) return styles.statusPending;
  if (status === 1 || status === 2 || status === 3) return styles.statusPaid;
  return styles.statusClosed;
}

function isSeckillOrder(o: OrderListOrder): boolean {
  return Boolean(o.memberUsername?.trim().startsWith('seckill-'));
}

function toPayAmount(o: OrderListOrder): number | string {
  return o.payAmount ?? o.totalAmount ?? 0;
}

function payHref(o: OrderListOrder): string {
  const sn = (o.orderSn || '').trim();
  if (isSeckillOrder(o)) {
    return `/order/seckill/confirm?orderSn=${encodeURIComponent(sn)}`;
  }
  const q = new URLSearchParams({
    orderSn: sn,
    amount: String(toPayAmount(o)),
  });
  return `/order/pay?${q.toString()}`;
}

function formatTime(v: string | undefined): string {
  if (!v) return '-';
  return String(v).replace('T', ' ').slice(0, 19);
}

function productTitle(o: OrderListOrder): string {
  if (o.firstItemTitle) return o.firstItemTitle;
  if (o.status === 0) return 'Pending payment';
  if (o.status === 1 || o.status === 2 || o.status === 3) return 'Payment received';
  if (o.status === 4 || o.status === 5) return 'Order closed';
  return 'Order';
}

function productMeta(o: OrderListOrder): string | null {
  const count = Number(o.itemCount ?? 0);
  if (count > 1) return `${count} items`;
  if (count === 1) return '1 item';
  return null;
}

function iconByStatus(status: number | undefined): string {
  if (status === 0) return '⏳';
  if (status === 1 || status === 2 || status === 3) return '✅';
  if (status === 4 || status === 5) return '⛔';
  return '🧾';
}

export default async function OrderListPage({ searchParams }: { searchParams: Search }) {
  const tab = normalizeStatusTab(firstParam(searchParams, 'status'));
  const result = await getMyOrdersServer(tab);

  if (!result.ok && result.unauthorized) {
    const redirectTo = tab === 'all' ? '/order' : `/order?status=${tab}`;
    redirect(`/login?redirect=${encodeURIComponent(redirectTo)}`);
  }

  const orders = result.data;

  return (
    <MallShell>
      <section className={styles.main}>
        <header className={styles.mainHead}>
          <h1 className={styles.mainTitle}>My Orders</h1>
          <div className={styles.tabs}>
            <Link href="/order" className={`${styles.tab} ${tab === 'all' ? styles.tabActive : ''}`}>
              All
            </Link>
            <Link href="/order?status=pending" className={`${styles.tab} ${tab === 'pending' ? styles.tabActive : ''}`}>
              Pending payment
            </Link>
            <Link href="/order?status=paid" className={`${styles.tab} ${tab === 'paid' ? styles.tabActive : ''}`}>
              Paid
            </Link>
            <Link href="/order?status=closed" className={`${styles.tab} ${tab === 'closed' ? styles.tabActive : ''}`}>
              Closed
            </Link>
          </div>
        </header>

        <div className={styles.tableWrap}>
          {orders.length === 0 ? (
            <div className={styles.empty}>No orders under this status.</div>
          ) : (
            orders.map((o) => (
              <article className={styles.orderCard} key={o.orderSn || String(o.id)}>
                <div className={styles.orderMeta}>
                  <span>{formatTime(o.createTime)}</span>
                  <span>
                    Order No.: <b className={styles.orderSn}>{o.orderSn || '-'}</b>
                  </span>
                </div>
                <div className={styles.row}>
                  <div className={styles.product}>
                    {o.firstItemPic ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img className={styles.productThumb} src={o.firstItemPic} alt="" />
                    ) : (
                      <div className={styles.productIcon} aria-hidden>
                        {iconByStatus(o.status)}
                      </div>
                    )}
                    <div>
                      <p className={styles.productName}>{productTitle(o)}</p>
                      {productMeta(o) && <p className={styles.productMeta}>{productMeta(o)}</p>}
                      <PendingOrderSummary
                        order={o}
                        amountText={money(toPayAmount(o))}
                        className={styles.productQty}
                      />
                    </div>
                  </div>
                  <div className={styles.money}>{money(toPayAmount(o))}</div>
                  <div className={statusClass(o.status)}>{statusText(o.status)}</div>
                  <div className={styles.actions}>
                    {o.status === 0 ? (
                      <OrderListPendingActions
                        orderSn={(o.orderSn || '').trim()}
                        payHref={payHref(o)}
                        isSeckill={isSeckillOrder(o)}
                      />
                    ) : (
                      <>
                        <Link
                          href={`/order/${encodeURIComponent((o.orderSn || '').trim())}`}
                          className={`${styles.btn} ${styles.btnPrimary}`}
                        >
                          View details
                        </Link>
                        <Link href="/search" className={styles.btn}>
                          Shop more
                        </Link>
                      </>
                    )}
                  </div>
                </div>
              </article>
            ))
          )}
        </div>
      </section>
    </MallShell>
  );
}
