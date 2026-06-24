'use client';

import { useEffect, useMemo, useState } from 'react';
import type { OrderConfirmAddress } from '@/types/api';
import styles from '@/app/order/confirm/page.module.css';

const SELECTED_ADDRESS_KEY = 'order_selected_address_id';

type Props = {
  addresses: OrderConfirmAddress[];
};

function addressText(a: OrderConfirmAddress): string {
  return [a.province, a.city, a.region, a.detailAddress].filter(Boolean).join(' ');
}

export default function OrderAddressSelector({ addresses }: Props) {
  const defaultAddressId = useMemo(
    () => addresses.find((a) => a.defaultStatus === 1)?.id ?? addresses[0]?.id,
    [addresses]
  );
  const [selectedId, setSelectedId] = useState<number | undefined>(defaultAddressId);

  useEffect(() => {
    if (!addresses.length) return;
    const raw = window.localStorage.getItem(SELECTED_ADDRESS_KEY);
    const saved = raw ? Number(raw) : NaN;
    const hit = addresses.find((a) => a.id === saved)?.id;
    const finalId = hit ?? defaultAddressId;
    setSelectedId(finalId);
    if (finalId != null) {
      window.localStorage.setItem(SELECTED_ADDRESS_KEY, String(finalId));
      window.dispatchEvent(new CustomEvent('order-address-selected', { detail: { addressId: finalId } }));
    }
  }, [addresses, defaultAddressId]);

  const onPick = (id: number) => {
    setSelectedId(id);
    window.localStorage.setItem(SELECTED_ADDRESS_KEY, String(id));
    window.dispatchEvent(new CustomEvent('order-address-selected', { detail: { addressId: id } }));
  };

  return (
    <ul className={styles.addressList}>
      {addresses.map((a) => {
        const active = a.id === selectedId;
        return (
          <li
            key={a.id}
            className={`${styles.addressItem} ${active ? styles.addressItemActive : ''}`}
            onClick={() => onPick(a.id)}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                onPick(a.id);
              }
            }}
          >
            <div>
              <strong>{a.name}</strong> · {a.phone}
            </div>
            <div>{addressText(a)}</div>
          </li>
        );
      })}
    </ul>
  );
}
