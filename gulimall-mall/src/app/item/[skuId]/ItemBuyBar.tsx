'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './page.module.css';

type Props = {
  skuId: number;
  title?: string | null;
  img?: string | null;
};

export default function ItemBuyBar({ skuId, title, img }: Props) {
  const router = useRouter();
  const [qty, setQty] = useState(1);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const addToCart = async () => {
    setLoading(true);
    setMessage('');
    try {
      const params = new URLSearchParams({ skuId: String(skuId), num: String(qty) });
      if (title) params.set('title', title);
      if (img) params.set('img', img);
      // Cart service adds the SKU then responds with 302 → /cart/success (not JSON).
      const res = await fetch(`/api/cart/add?${params}`, {
        credentials: 'include',
        redirect: 'manual',
      });
      if (res.status === 302 || res.status === 303 || res.status === 0) {
        const loc = res.headers.get('Location');
        if (loc) {
          const path = loc.startsWith('http')
            ? `${new URL(loc).pathname}${new URL(loc).search}`
            : loc;
          router.push(path);
          router.refresh();
          return;
        }
        router.push(`/cart/success?${params}`);
        router.refresh();
        return;
      }
      const contentType = res.headers.get('content-type') ?? '';
      if (contentType.includes('application/json')) {
        const data = (await res.json()) as { code?: number; msg?: string };
        if (res.ok && data.code === 0) {
          router.push('/cart/list');
          router.refresh();
          return;
        }
        setMessage(data.msg || 'Could not add to cart');
        return;
      }
      if (res.ok) {
        router.push(`/cart/success?${params}`);
        router.refresh();
        return;
      }
      setMessage('Could not add to cart');
    } catch {
      setMessage('Network error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.buyBar}>
      <div className={styles.qty}>
        <button type="button" onClick={() => setQty((q) => Math.max(1, q - 1))} disabled={loading}>
          −
        </button>
        <span>{qty}</span>
        <button type="button" onClick={() => setQty((q) => Math.min(99, q + 1))} disabled={loading}>
          +
        </button>
      </div>
      <button type="button" className={styles.addBtn} onClick={addToCart} disabled={loading}>
        {loading ? 'Adding…' : 'Add to cart'}
      </button>
      {message && <p className={styles.buyMsg}>{message}</p>}
    </div>
  );
}
