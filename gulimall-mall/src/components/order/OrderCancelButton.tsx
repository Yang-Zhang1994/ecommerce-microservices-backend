'use client';

import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { cancelOrder } from '@/lib/api';
import styles from './OrderCancelButton.module.css';

type Props = {
  orderSn: string;
  /** Flash-sale orders restore grab quota when cancelled. */
  isSeckill?: boolean;
  className?: string;
  block?: boolean;
  /** Where to go after a successful cancel. Defaults to order list pending tab. */
  redirectTo?: string;
  label?: string;
  onCancelled?: () => void;
};

export default function OrderCancelButton({
  orderSn,
  isSeckill = false,
  className,
  block = false,
  redirectTo = '/order?status=pending',
  label = "Don't want it — cancel order",
  onCancelled,
}: Props) {
  const router = useRouter();
  const [pending, setPending] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const sn = orderSn.trim();
  if (!sn) {
    return null;
  }

  async function onCancel() {
    if (pending) return;
    const extra = isSeckill
      ? ' Your flash-sale slot will be released so you can grab again.'
      : ' Locked stock will be released.';
    const ok = window.confirm(
      `Cancel this unpaid order?${extra}\n\nOrder No.: ${sn}`,
    );
    if (!ok) return;

    setPending(true);
    setErrorMsg('');
    try {
      const res = await cancelOrder(sn);
      if (res.code === 401) {
        window.location.assign(
          `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`,
        );
        return;
      }
      if (res.code !== 0) {
        setErrorMsg(res.msg || 'Could not cancel this order.');
        return;
      }
      onCancelled?.();
      if (redirectTo) {
        router.push(redirectTo);
      } else {
        router.refresh();
      }
    } catch (e) {
      setErrorMsg(e instanceof Error ? e.message : 'Could not cancel this order.');
    } finally {
      setPending(false);
    }
  }

  return (
    <div className={className}>
      <button
        type="button"
        className={`${styles.cancelBtn} ${block ? styles.cancelBtnBlock : ''}`}
        disabled={pending}
        onClick={onCancel}
      >
        {pending ? 'Cancelling…' : label}
      </button>
      {!!errorMsg && (
        <p className={styles.error} role="alert">
          {errorMsg}
        </p>
      )}
    </div>
  );
}
