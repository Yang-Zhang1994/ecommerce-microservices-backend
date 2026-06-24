import Link from 'next/link';
import { notFound, redirect } from 'next/navigation';
import MallShell from '@/components/layout/MallShell';
import OrderCancelButton from '@/components/order/OrderCancelButton';
import { getOrderDetailServer } from '@/lib/api';
import type { OrderDetail, OrderDetailItem } from '@/types/api';
import styles from './page.module.css';

function money(v: number | string | undefined | null): string {
  const n = Number(v ?? 0);
  if (!Number.isFinite(n)) return 'CA$0.00';
  return `CA$${n.toFixed(2)}`;
}

function formatTime(v: string | undefined): string {
  if (!v) return '—';
  return String(v).replace('T', ' ').slice(0, 19);
}

function statusClass(status: number | undefined): string {
  if (status === 0) return styles.statusPending;
  if (status === 1 || status === 2 || status === 3) return styles.statusPaid;
  return styles.statusClosed;
}

function lineSubtotal(item: OrderDetailItem): number {
  const qty = Number(item.skuQuantity ?? 0);
  const price = Number(item.skuPrice ?? 0);
  const real = Number(item.realAmount ?? 0);
  if (real > 0) return real;
  if (qty > 0 && price > 0) return qty * price;
  return 0;
}

function fullAddress(d: OrderDetail): string {
  const parts = [
    d.receiverProvince,
    d.receiverCity,
    d.receiverRegion,
    d.receiverDetailAddress,
  ].filter(Boolean);
  return parts.join(' ') || '—';
}

export default async function OrderDetailPage({ params }: { params: { orderSn: string } }) {
  const orderSn = decodeURIComponent(params.orderSn || '').trim();
  const result = await getOrderDetailServer(orderSn);

  if (!result.ok && result.unauthorized) {
    redirect(`/login?redirect=${encodeURIComponent(`/order/${encodeURIComponent(orderSn)}`)}`);
  }
  if (!result.ok || !result.data) {
    notFound();
  }

  const d = result.data;
  const items = Array.isArray(d.items) ? d.items : [];
  const payAmount = d.payAmount ?? d.totalAmount ?? 0;
  const isSeckill = Boolean(d.note?.trim().startsWith('Seckill order'));

  return (
    <MallShell>
      <section className={styles.card}>
        <header className={styles.head}>
          <h1 className={styles.title}>Order details</h1>
          <p className={styles.subtitle}>{d.statusText || 'Order'}</p>
          <div className={styles.metaRow}>
            <span>
              Order No.: <b className={styles.orderSn}>{d.orderSn || '—'}</b>
            </span>
            <span>Placed: {formatTime(d.createTime)}</span>
            {d.paymentTime && <span>Paid: {formatTime(d.paymentTime)}</span>}
            <span className={statusClass(d.status)}>{d.statusText || '—'}</span>
          </div>
        </header>

        <div className={styles.block}>
          <h2>Shipping address</h2>
          <p className={styles.address}>
            {d.receiverName || '—'} · {d.receiverPhone || '—'}
            <br />
            {fullAddress(d)}
            {d.receiverPostCode ? ` · ${d.receiverPostCode}` : ''}
          </p>
        </div>

        <div className={styles.block}>
          <h2>Items ({items.length})</h2>
          {items.length === 0 ? (
            <p className={styles.empty}>No line items found.</p>
          ) : (
            <div className={styles.itemsTable}>
              <div className={styles.itemsHead}>
                <span>Product</span>
                <span>Qty</span>
                <span>Subtotal</span>
              </div>
              {items.map((item, idx) => (
                <div key={`${item.skuId ?? 'sku'}-${idx}`} className={styles.itemRow}>
                  <div className={styles.itemMain}>
                    {item.skuPic ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img src={item.skuPic} alt="" />
                    ) : (
                      <div className={styles.itemImgPlaceholder} />
                    )}
                    <div>
                      <p>{item.skuName || item.spuName || 'Product'}</p>
                      {!!item.skuAttrs?.length && <small>{item.skuAttrs.join(' · ')}</small>}
                    </div>
                  </div>
                  <span>{item.skuQuantity ?? 0}</span>
                  <span>{money(lineSubtotal(item))}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className={styles.block}>
          <h2>Amount</h2>
          <div className={styles.amountGrid}>
            <div className={styles.amountRow}>
              <span>Subtotal</span>
              <span>{money(d.totalAmount)}</span>
            </div>
            {Number(d.freightAmount ?? 0) > 0 && (
              <div className={styles.amountRow}>
                <span>Shipping</span>
                <span>{money(d.freightAmount)}</span>
              </div>
            )}
            {Number(d.promotionAmount ?? 0) > 0 && (
              <div className={styles.amountRow}>
                <span>Promotions</span>
                <span>-{money(d.promotionAmount)}</span>
              </div>
            )}
            {Number(d.couponAmount ?? 0) > 0 && (
              <div className={styles.amountRow}>
                <span>Coupon</span>
                <span>-{money(d.couponAmount)}</span>
              </div>
            )}
            <div className={`${styles.amountRow} ${styles.payTotal}`}>
              <span>Amount paid</span>
              <strong>{money(payAmount)}</strong>
            </div>
          </div>
        </div>

        <div className={styles.block}>
          <h2>Payment</h2>
          {d.payment?.paid ? (
            <>
              <p className={styles.paymentLine}>
                Status: <strong>Paid</strong>
                {d.payment.paymentStatus ? ` (${d.payment.paymentStatus})` : ''}
              </p>
              {d.payment.payMethodLabel && (
                <p className={styles.paymentLine}>Method: {d.payment.payMethodLabel}</p>
              )}
              <p className={styles.paymentLine}>Paid at: {formatTime(d.payment.paymentTime ?? d.paymentTime)}</p>
              {d.payment.tradeNo && (
                <p className={styles.paymentLine}>
                  Reference: <span className={styles.orderSn}>{d.payment.tradeNo}</span>
                </p>
              )}
            </>
          ) : (
            <p className={styles.paymentLine}>Payment not completed. Complete checkout before the order expires.</p>
          )}
        </div>

        {(d.deliverySn || d.deliveryCompany) && (
          <div className={styles.block}>
            <h2>Shipping</h2>
            {d.deliveryCompany && <p className={styles.paymentLine}>Carrier: {d.deliveryCompany}</p>}
            {d.deliverySn && (
              <p className={styles.paymentLine}>
                Tracking: <span className={styles.orderSn}>{d.deliverySn}</span>
              </p>
            )}
            {d.deliveryTime && <p className={styles.paymentLine}>Shipped: {formatTime(d.deliveryTime)}</p>}
          </div>
        )}

        {d.note && (
          <div className={styles.block}>
            <h2>Order note</h2>
            <p className={styles.paymentLine}>{d.note}</p>
          </div>
        )}

        <div className={styles.actions}>
          <Link href="/order" className={styles.btn}>
            Back to orders
          </Link>
          {d.status === 0 && (
            <>
              <Link
                href={
                  isSeckill
                    ? `/order/seckill/confirm?orderSn=${encodeURIComponent((d.orderSn || '').trim())}`
                    : `/order/pay?orderSn=${encodeURIComponent(d.orderSn || '')}&amount=${encodeURIComponent(String(payAmount))}`
                }
                className={`${styles.btn} ${styles.btnPrimary}`}
              >
                {isSeckill ? 'Confirm & pay' : 'Pay now'}
              </Link>
              <OrderCancelButton
                orderSn={d.orderSn || orderSn}
                isSeckill={isSeckill}
                redirectTo="/order?status=closed"
              />
            </>
          )}
          <Link href="/search" className={styles.btn}>
            Shop more
          </Link>
        </div>
      </section>
    </MallShell>
  );
}
