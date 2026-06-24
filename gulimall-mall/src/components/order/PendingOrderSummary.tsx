'use client';

import { useEffect, useState } from 'react';
import type { OrderListOrder } from '@/types/api';

type Props = {
  order: OrderListOrder;
  amountText: string;
  className?: string;
};

function parseCreateTime(v: string | undefined): number | null {
  if (!v) return null;
  const trimmed = v.trim();
  let normalized = trimmed.includes('T') ? trimmed : trimmed.replace(' ', 'T');
  // Backend Jackson dates are UTC wall-clock without offset; treat as UTC in the browser.
  if (!/[zZ]|[+-]\d{2}:?\d{2}$/.test(normalized)) {
    normalized += 'Z';
  }
  const t = Date.parse(normalized);
  return Number.isFinite(t) ? t : null;
}

function isSeckillOrder(order: OrderListOrder): boolean {
  return Boolean(order.memberUsername?.trim().startsWith('seckill-'));
}

function paymentTimeoutMinutes(flash: boolean): number {
  const raw = flash
    ? process.env.NEXT_PUBLIC_ORDER_FLASH_PAY_TIMEOUT_MINUTES?.trim()
    : process.env.NEXT_PUBLIC_ORDER_PAY_TIMEOUT_MINUTES?.trim();
  const fallback = flash ? 15 : 30;
  const n = raw ? Number(raw) : fallback;
  if (!Number.isFinite(n) || n <= 0) return fallback;
  return Math.round(n);
}

function formatRemaining(ms: number): string {
  const totalSec = Math.max(0, Math.floor(ms / 1000));
  const m = Math.floor(totalSec / 60);
  const s = totalSec % 60;
  return `${m}m ${String(s).padStart(2, '0')}s`;
}

export default function PendingOrderSummary({ order, amountText, className }: Props) {
  const flash = isSeckillOrder(order);
  const timeoutMs = paymentTimeoutMinutes(flash) * 60 * 1000;
  const created = parseCreateTime(order.createTime);
  const deadline = created == null ? null : created + timeoutMs;
  const [now, setNow] = useState(() => Date.now());
  useEffect(() => {
    if (order.status !== 0 || deadline == null) return;
    const timer = window.setInterval(() => setNow(Date.now()), 1000);
    return () => window.clearInterval(timer);
  }, [deadline, order.status]);
  const remaining = deadline == null ? null : deadline - now;

  if (order.status === 1 || order.status === 2 || order.status === 3) {
    return (
      <p className={className}>
        Payment received. We are preparing shipment. You can open details for fulfillment updates.
      </p>
    );
  }

  if (order.status === 4 || order.status === 5) {
    return (
      <p className={className}>
        This order has been closed. If needed, you can place a new order from search.
      </p>
    );
  }

  if (order.status !== 0) {
    return <p className={className}>Open details for the latest order progress.</p>;
  }

  if (remaining == null) {
    return (
      <p className={className}>
        Amount due {amountText}. Pending payment - complete within {paymentTimeoutMinutes(flash)} minutes.
      </p>
    );
  }

  if (remaining <= 0) {
    return (
      <p className={className}>
        Amount due {amountText}. Payment window has ended; refresh to see latest order status.
      </p>
    );
  }

  return (
    <p className={className}>
      Amount due {amountText}. Pending payment - time left {formatRemaining(remaining)}.
    </p>
  );
}

