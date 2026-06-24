'use client';

import Link from 'next/link';
import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import OrderAddressBlock from '@/components/order/OrderAddressBlock';
import OrderCancelButton from '@/components/order/OrderCancelButton';
import { bindSeckillOrderAddress } from '@/lib/api';
import type { OrderConfirmAddress, OrderDetailItem, SeckillOrderConfirmData } from '@/types/api';
import panelStyles from '@/app/order/confirm/page.module.css';

const SELECTED_ADDRESS_KEY = 'order_selected_address_id';

type Props = {
  confirm: SeckillOrderConfirmData;
};

function money(v: number | string | undefined | null): string {
  const n = Number(v ?? 0);
  if (!Number.isFinite(n)) return '$0.00';
  return `$${n.toFixed(2)}`;
}

export default function SeckillConfirmPanel({ confirm }: Props) {
  const router = useRouter();
  const orderSn = (confirm.orderSn || '').trim();
  const addresses: OrderConfirmAddress[] = Array.isArray(confirm.addresses) ? confirm.addresses : [];
  const items: OrderDetailItem[] = Array.isArray(confirm.items) ? confirm.items : [];
  const payAmount = confirm.payAmount ?? 0;
  const flashMinutes = confirm.flashPayTimeoutMinutes ?? 15;

  const defaultAddressId = useMemo(
    () => addresses.find((a) => a.defaultStatus === 1)?.id ?? addresses[0]?.id,
    [addresses],
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

  async function onConfirmAndPay() {
    if (!orderSn) {
      setErrorMsg('Order number missing.');
      return;
    }
    if (addressId == null || !addresses.some((a) => a.id === addressId)) {
      setErrorMsg('Please select or add a shipping address.');
      return;
    }
    setPending(true);
    setErrorMsg('');
    try {
      const resp = await bindSeckillOrderAddress({ orderSn, addressId });
      if (resp.code !== 0) {
        if (resp.code === 401) {
          window.location.assign(
            `/login?redirect=${encodeURIComponent(`/order/seckill/confirm?orderSn=${encodeURIComponent(orderSn)}`)}`,
          );
          return;
        }
        setErrorMsg(resp.msg || 'Could not save shipping address.');
        return;
      }
      const q = new URLSearchParams({
        orderSn,
        amount: String(payAmount),
        flash: '1',
      });
      router.push(`/order/pay?${q.toString()}`);
    } catch (e) {
      setErrorMsg(e instanceof Error ? e.message : 'Could not continue to payment');
    } finally {
      setPending(false);
    }
  }

  const savedAddress =
    confirm.receiverName && confirm.receiverDetailAddress
      ? [confirm.receiverName, confirm.receiverPhone, confirm.receiverDetailAddress]
          .filter(Boolean)
          .join(' · ')
      : null;

  return (
    <>
      <p className={panelStyles.confirmWarn}>
        Flash sale — complete address and payment within {flashMinutes} minutes or the order will be
        cancelled.
      </p>

      <div className={panelStyles.block}>
        <h2>Shipping Address</h2>
        {savedAddress && !confirm.needsAddress && (
          <p className={panelStyles.addressHint}>Current: {savedAddress}</p>
        )}
        <OrderAddressBlock addresses={addresses} />
      </div>

      <div className={panelStyles.block}>
        <h2>Flash sale item</h2>
        {items.length === 0 ? (
          <p>Loading item details…</p>
        ) : (
          <div className={panelStyles.itemsTable}>
            <div className={panelStyles.itemsHead}>
              <span>Item</span>
              <span>Qty</span>
              <span>Stock</span>
              <span>Subtotal</span>
            </div>
            {items.map((item, idx) => (
              <div key={`${item.skuId ?? 'sku'}-${idx}`} className={panelStyles.itemRow}>
                <div className={panelStyles.itemMain}>
                  {item.skuPic ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={item.skuPic} alt="" />
                  ) : (
                    <div className={panelStyles.itemImgPlaceholder} />
                  )}
                  <div>
                    <p>{item.skuName || 'Product'}</p>
                  </div>
                </div>
                <span>{item.skuQuantity ?? 1}</span>
                <span className={panelStyles.stockIn}>Locked</span>
                <span>{money(item.realAmount ?? item.skuPrice)}</span>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className={panelStyles.block}>
        <h2>Payable total</h2>
        <div className={panelStyles.amounts}>
          <p>Items: {money(confirm.payAmount)}</p>
          <p>Freight: {money(confirm.freightAmount ?? 0)}</p>
          <p className={panelStyles.total}>{money(payAmount)}</p>
        </div>
      </div>

      <div className={panelStyles.footer}>
        <Link href="/" className={panelStyles.backBtn}>
          Continue shopping
        </Link>
        <div className={panelStyles.submitPanel}>
          {errorMsg && <p className={panelStyles.formError}>{errorMsg}</p>}
          <button
            type="button"
            className={panelStyles.primaryBtn}
            disabled={pending || !addresses.length}
            onClick={onConfirmAndPay}
          >
            {pending ? 'Saving…' : 'Confirm address & pay'}
          </button>
          <OrderCancelButton
            orderSn={orderSn}
            isSeckill
            block
            redirectTo="/order?status=closed"
            label="Don't want it — cancel order"
          />
        </div>
      </div>
    </>
  );
}
