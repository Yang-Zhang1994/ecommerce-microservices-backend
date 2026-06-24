import Link from 'next/link';
import { redirect } from 'next/navigation';
import MallShell from '@/components/layout/MallShell';
import OrderSubmitPanel from '@/components/order/OrderSubmitPanel';
import OrderAddressBlock from '@/components/order/OrderAddressBlock';
import { getOrderConfirmServer } from '@/lib/api';
import type { OrderConfirmAddress, OrderConfirmItem } from '@/types/api';
import styles from './page.module.css';

function money(v: number | string | undefined | null): string {
  const n = Number(v ?? 0);
  if (!Number.isFinite(n)) return '$0.00';
  return `$${n.toFixed(2)}`;
}

export default async function OrderConfirmPage() {
  const result = await getOrderConfirmServer();
  if (!result.ok && result.unauthorized) {
    redirect('/login?redirect=/order/confirm');
  }
  const data = result.data ?? {};
  const addresses: OrderConfirmAddress[] = Array.isArray(data.addresses) ? data.addresses : [];
  const items: OrderConfirmItem[] = Array.isArray(data.items) ? data.items : [];
  const hasOutOfStock = items.some((it) => it.hasStock === false);

  return (
    <MallShell>
      <section className={styles.card}>
        <div className={styles.head}>
          <h1 className={styles.title}>Confirm Order</h1>
          <p className={styles.subtitle}>Review address, items and payable amount before placing order.</p>
          {hasOutOfStock && (
            <p className={styles.confirmWarn}>
              Some items are out of stock. They stay in your cart; submit the order after stock is restored
              (refresh this page).
            </p>
          )}
        </div>

        <div className={styles.block}>
          <h2>Shipping Address</h2>
          <OrderAddressBlock addresses={addresses} />
        </div>

        <div className={styles.block}>
          <h2>Items</h2>
          {items.length === 0 ? (
            <p>No checked cart items.</p>
          ) : (
            <div className={styles.itemsTable}>
              <div className={styles.itemsHead}>
                <span>Item</span>
                <span>Qty</span>
                <span>Stock</span>
                <span>Subtotal</span>
              </div>
              {items.map((item, idx) => (
                <div key={`${item.skuId ?? 'sku'}-${idx}`} className={styles.itemRow}>
                  <div className={styles.itemMain}>
                    {item.image ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img src={item.image} alt="" />
                    ) : (
                      <div className={styles.itemImgPlaceholder} />
                    )}
                    <div>
                      <p>{item.title || 'Product'}</p>
                      {!!item.skuAttr?.length && <small>{item.skuAttr.join(' · ')}</small>}
                    </div>
                  </div>
                  <span>{item.count ?? 0}</span>
                  <span
                    className={
                      item.hasStock === true ? styles.stockIn : item.hasStock === false ? styles.stockOut : styles.stockUnknown
                    }
                  >
                    {item.hasStock === true ? 'In stock' : item.hasStock === false ? 'Out of stock' : '—'}
                  </span>
                  <span>{money(item.totalPrice)}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className={styles.block}>
          <h2>Payable Total</h2>
          <div className={styles.amounts}>
            <p>Available points: {data.integration ?? 0}</p>
            <p>Integration discount: -{money(data.integrationAmount)}</p>
            <p>Items: {money(data.totalAmount)}</p>
            <p>Freight: {money(data.freightAmount)}</p>
            <p className={styles.total}>{money(data.payPrice)}</p>
          </div>
        </div>

        <div className={styles.footer}>
          <Link href="/cart/list" className={styles.backBtn}>
            Back to Cart
          </Link>
          <OrderSubmitPanel
            className={styles.submitPanel}
            orderToken={data.orderToken || ''}
            addresses={addresses}
            items={items}
          />
        </div>
      </section>
    </MallShell>
  );
}
