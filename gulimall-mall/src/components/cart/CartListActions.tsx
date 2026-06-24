'use client';

import { useRouter } from 'next/navigation';
import { useState } from 'react';

type Props = {
  skuId?: number;
  count?: number;
  checked?: boolean;
  mode?: 'check' | 'ops';
  className?: string;
  checkClassName?: string;
  btnClassName?: string;
  dangerBtnClassName?: string;
  /** When set, called after a cart mutation instead of {@code router.refresh()}. */
  onMutated?: () => void | Promise<void>;
};

export default function CartListActions({
  skuId,
  count,
  checked,
  mode = 'ops',
  className,
  checkClassName,
  btnClassName,
  dangerBtnClassName,
  onMutated,
}: Props) {
  const router = useRouter();
  const [pending, setPending] = useState(false);
  const sid = typeof skuId === 'number' ? skuId : NaN;
  const current = typeof count === 'number' && count > 0 ? count : 1;
  const isChecked = checked !== false;

  async function call(path: string, params: URLSearchParams) {
    if (!Number.isFinite(sid) || pending) return;
    setPending(true);
    try {
      const res = await fetch(`${path}?${params.toString()}`, {
        method: 'POST',
        credentials: 'include',
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      if (onMutated) {
        await onMutated();
      } else {
        router.refresh();
      }
    } finally {
      setPending(false);
    }
  }

  function onPlus() {
    const p = new URLSearchParams();
    p.set('skuId', String(sid));
    p.set('num', String(current + 1));
    void call('/api/cart/item/count', p);
  }

  function onMinus() {
    const p = new URLSearchParams();
    p.set('skuId', String(sid));
    p.set('num', String(Math.max(1, current - 1)));
    void call('/api/cart/item/count', p);
  }

  function onToggle() {
    const p = new URLSearchParams();
    p.set('skuId', String(sid));
    p.set('checked', String(!isChecked));
    void call('/api/cart/item/check', p);
  }

  function onDelete() {
    const p = new URLSearchParams();
    p.set('skuId', String(sid));
    void call('/api/cart/item/delete', p);
  }

  if (mode === 'check') {
    return (
      <label className={checkClassName} style={{ display: 'inline-flex', alignItems: 'center' }}>
        <input
          type="checkbox"
          checked={isChecked}
          onChange={onToggle}
          disabled={pending || !Number.isFinite(sid)}
          aria-label="selected"
        />
      </label>
    );
  }

  return (
    <div className={className} style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
      <button className={btnClassName} type="button" onClick={onMinus} disabled={pending || !Number.isFinite(sid)}>
        -
      </button>
      <button className={btnClassName} type="button" onClick={onPlus} disabled={pending || !Number.isFinite(sid)}>
        +
      </button>
      <button
        className={dangerBtnClassName || btnClassName}
        type="button"
        onClick={onDelete}
        disabled={pending || !Number.isFinite(sid)}
      >
        Delete
      </button>
    </div>
  );
}
