'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import CartCheckoutBar from '@/components/cart/CartCheckoutBar';
import CartListActions from '@/components/cart/CartListActions';
import { getCurrentCartClient, getSkuHasStockMapClient } from '@/lib/api';
import type { Cart, CartItem } from '@/types/api';
import styles from '@/components/cart/cartList.module.css';

function toNumber(v: number | string | undefined | null): number {
  if (typeof v === 'number') return v;
  if (typeof v === 'string') {
    const n = Number(v);
    return Number.isFinite(n) ? n : 0;
  }
  return 0;
}

function money(v: number | string | undefined | null): string {
  return `$${toNumber(v).toFixed(2)}`;
}

function fallbackTotal(items: CartItem[]): number {
  return items
    .filter((it) => it.check !== false)
    .reduce((sum, it) => {
      const sub = it.totalPrice != null ? toNumber(it.totalPrice) : toNumber(it.price) * toNumber(it.count);
      return sum + sub;
    }, 0);
}

export default function CartListContent() {
  const [loading, setLoading] = useState(true);
  const [cart, setCart] = useState<Cart>({ items: [] });
  const [stockMap, setStockMap] = useState<Map<number, boolean>>(new Map());

  const loadCart = useCallback(async () => {
    setLoading(true);
    try {
      const next = await getCurrentCartClient();
      const items = Array.isArray(next.items) ? next.items : [];
      setCart(next);
      const skuIds = items
        .map((it) => (it.skuId != null ? Number(it.skuId) : NaN))
        .filter((id) => Number.isFinite(id) && id > 0);
      const stock = await getSkuHasStockMapClient(skuIds);
      setStockMap(stock);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadCart();
  }, [loadCart]);

  const items = Array.isArray(cart.items) ? cart.items : [];
  const countNum = typeof cart.countNum === 'number' ? cart.countNum : items.reduce((n, it) => n + (it.count ?? 0), 0);
  const countType =
    typeof cart.countType === 'number' ? cart.countType : items.filter((it) => it.check !== false).length;
  const totalAmount = cart.totalAmount != null ? toNumber(cart.totalAmount) : fallbackTotal(items);
  const reduceAmount = cart.reduce != null ? toNumber(cart.reduce) : 0;
  const linesWithStock = items.map((it) => ({
    skuId: it.skuId,
    check: it.check,
    hasStock: it.skuId != null ? stockMap.get(Number(it.skuId)) ?? null : null,
  }));

  return (
    <section className={styles.card}>
      <div className={styles.head}>
        <h1 className={styles.title}>My Cart</h1>
      </div>

      {loading ? (
        <div className={styles.loading}>Loading cart…</div>
      ) : items.length === 0 ? (
        <div className={styles.empty}>
          <p>Your cart is empty.</p>
          <p>
            <Link href="/search">Go shopping</Link>
          </p>
        </div>
      ) : (
        <>
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Select</th>
                  <th>Item</th>
                  <th>Unit Price</th>
                  <th>Quantity</th>
                  <th>Subtotal</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item) => {
                  const skuId = item.skuId != null ? String(item.skuId) : '';
                  const title = item.title?.trim() || (skuId ? `SKU ${skuId}` : 'Product');
                  const image = item.image?.trim() || '';
                  const subtotal =
                    item.totalPrice != null ? item.totalPrice : toNumber(item.price) * toNumber(item.count);
                  const skuIdNum = item.skuId != null ? Number(item.skuId) : NaN;
                  const hasStock = Number.isFinite(skuIdNum) ? stockMap.get(skuIdNum) : undefined;
                  return (
                    <tr key={`${skuId}-${title}`}>
                      <td>
                        <CartListActions
                          skuId={item.skuId}
                          count={item.count}
                          checked={item.check}
                          mode="check"
                          checkClassName={styles.checkCell}
                          onMutated={loadCart}
                        />
                      </td>
                      <td>
                        <div className={styles.skuCell}>
                          {image ? (
                            // eslint-disable-next-line @next/next/no-img-element
                            <img className={styles.skuImg} src={image} alt="" />
                          ) : (
                            <div className={styles.skuImgPlaceholder} />
                          )}
                          <div>
                            <p className={styles.skuTitle}>
                              <Link href={skuId ? `/item/${skuId}` : '/'}>{title}</Link>
                            </p>
                            {!!item.skuAttr?.length && <p className={styles.skuAttrs}>{item.skuAttr.join(' · ')}</p>}
                            {hasStock === false && (
                              <p className={styles.stockOut}>Out of stock — save for later checkout</p>
                            )}
                            {hasStock === true && <p className={styles.stockIn}>In stock</p>}
                          </div>
                        </div>
                      </td>
                      <td>{money(item.price)}</td>
                      <td>{item.count ?? 0}</td>
                      <td>{money(subtotal)}</td>
                      <td>
                        <CartListActions
                          skuId={item.skuId}
                          count={item.count}
                          checked={item.check}
                          className={styles.actionGroup}
                          btnClassName={styles.actionBtn}
                          dangerBtnClassName={styles.actionBtnDanger}
                          onMutated={loadCart}
                        />
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div className={styles.summary}>
            <div className={styles.summaryLeft}>
              <span>Selected items: {countNum}</span>
              <span>Selected lines: {countType}</span>
            </div>
            <div className={styles.summaryRight}>
              <div className={styles.summaryAmount}>
                <div className={styles.summaryLabel}>Total</div>
                <div className={styles.summaryTotal}>{money(totalAmount)}</div>
                <div className={styles.summarySave}>You save: {money(reduceAmount)}</div>
              </div>
              <CartCheckoutBar items={linesWithStock} />
            </div>
          </div>
        </>
      )}
    </section>
  );
}
