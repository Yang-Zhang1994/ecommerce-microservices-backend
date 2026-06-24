'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import type { OrderConfirmAddress, OrderConfirmItem } from '@/types/api';
import { submitOrder } from '@/lib/api';
import panelStyles from '@/app/order/confirm/page.module.css';

const SELECTED_ADDRESS_KEY = 'order_selected_address_id';

type Props = {
  orderToken: string;
  addresses: OrderConfirmAddress[];
  items: OrderConfirmItem[];
  className?: string;
};

export default function OrderSubmitPanel({ orderToken, addresses, items, className }: Props) {
  const checkoutBlocked = items.some((it) => it.hasStock === false);
  const router = useRouter();
  const defaultAddressId = useMemo(
    () => addresses.find((a) => a.defaultStatus === 1)?.id ?? addresses[0]?.id,
    [addresses]
  );
  const [addressId, setAddressId] = useState<number | undefined>(() => {
    if (typeof window === 'undefined') return defaultAddressId;
    const raw = window.localStorage.getItem(SELECTED_ADDRESS_KEY);
    const saved = raw ? Number(raw) : NaN;
    return addresses.find((a) => a.id === saved)?.id ?? defaultAddressId;
  });
  const [pending, setPending] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (addressId == null || typeof window === 'undefined') return;
    window.localStorage.setItem(SELECTED_ADDRESS_KEY, String(addressId));
  }, [addressId]);

  useEffect(() => {
    if (typeof window === 'undefined') return;
    const onAddressSelected = (e: Event) => {
      const id = Number((e as CustomEvent<{ addressId?: number }>).detail?.addressId);
      if (!Number.isFinite(id)) return;
      if (!addresses.some((a) => a.id === id)) return;
      setAddressId(id);
    };
    window.addEventListener('order-address-selected', onAddressSelected as EventListener);
    return () => window.removeEventListener('order-address-selected', onAddressSelected as EventListener);
  }, [addresses]);

  useEffect(() => {
    if (!addresses.length) {
      setAddressId(undefined);
      return;
    }
    if (addressId == null || !addresses.some((a) => a.id === addressId)) {
      setAddressId(defaultAddressId);
    }
  }, [addresses, addressId, defaultAddressId]);

  async function onSubmit() {
    if (checkoutBlocked) {
      setErrorMsg('Some items are out of stock. Return to cart or refresh when stock is available.');
      return;
    }
    if (addressId == null || !addresses.some((a) => a.id === addressId)) {
      setErrorMsg('Please select a shipping address.');
      return;
    }
    if (!orderToken) {
      setErrorMsg('Order token missing, please refresh page.');
      return;
    }
    setPending(true);
    setErrorMsg('');
    try {
      const resp = await submitOrder({
        orderToken,
        addressId,
        payType: 1,
      });
      if (resp.code !== 0) {
        if (resp.code === 401) {
          window.location.assign(`/login?redirect=${encodeURIComponent('/order/confirm')}`);
          return;
        }
        if (resp.code === 3) {
          const msg = resp.msg || 'Order price changed, refreshing confirmation page...';
          setErrorMsg(msg);
          window.alert(msg);
          router.refresh();
          return;
        }
        if (resp.code === 5) {
          setErrorMsg('System busy, please retry shortly.');
          return;
        }
        if (resp.code === 4) {
          setErrorMsg('Insufficient stock. Refresh the page or remove out-of-stock items in cart.');
          router.refresh();
          return;
        }
        setErrorMsg(resp.msg || 'Submit failed');
        return;
      }
      const q = new URLSearchParams();
      if (resp.orderSn) q.set('orderSn', String(resp.orderSn));
      if (resp.payAmount != null && resp.payAmount !== '') q.set('amount', String(resp.payAmount));
      router.push(`/order/pay?${q.toString()}`);
      router.refresh();
    } catch (e) {
      setErrorMsg(e instanceof Error ? e.message : 'Submit failed');
    } finally {
      setPending(false);
    }
  }

  return (
    <div className={className}>
      {checkoutBlocked && (
        <p className={panelStyles.submitWarn}>
          Out-of-stock items cannot be ordered. Update cart or wait for restock, then refresh this page.
        </p>
      )}
      <button type="button" onClick={onSubmit} disabled={pending || checkoutBlocked}>
        {pending ? 'Submitting...' : 'Submit Order'}
      </button>
      {!!errorMsg && <p>{errorMsg}</p>}
    </div>
  );
}
