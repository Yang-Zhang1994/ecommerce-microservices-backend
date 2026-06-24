'use client';

import Link from 'next/link';
import styles from '@/components/cart/cartList.module.css';

type Line = {
  skuId?: number;
  check?: boolean;
  hasStock?: boolean | null;
};

type Props = {
  items: Line[];
};

export default function CartCheckoutBar({ items }: Props) {
  const checked = items.filter((it) => it.check !== false);
  const outOfStockChecked = checked.filter((it) => it.hasStock === false);
  const blocked = outOfStockChecked.length > 0;

  return (
    <div className={styles.checkoutWrap}>
      {blocked && (
        <p className={styles.checkoutWarn} role="status">
          {outOfStockChecked.length === checked.length
            ? 'Selected items are out of stock. You can keep them in the cart and checkout when stock is back.'
            : 'Some selected items are out of stock. Uncheck them or wait for restock before checkout.'}
        </p>
      )}
      {blocked ? (
        <span className={styles.checkoutBtnDisabled} aria-disabled="true">
          Go to Checkout
        </span>
      ) : (
        <Link href="/order/confirm" className={styles.checkoutBtn}>
          Go to Checkout
        </Link>
      )}
    </div>
  );
}
