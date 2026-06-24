'use client';

import { FormEvent, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import OrderAddressSelector from '@/components/order/OrderAddressSelector';
import { saveMemberAddress } from '@/lib/api';
import { loadGulimallSession } from '@/lib/gulimallSession';
import type { OrderConfirmAddress } from '@/types/api';
import styles from '@/app/order/confirm/page.module.css';

type Props = {
  addresses: OrderConfirmAddress[];
};

type FormState = {
  name: string;
  phone: string;
  postCode: string;
  province: string;
  city: string;
  region: string;
  detailAddress: string;
  defaultStatus: boolean;
};

const emptyForm = (): FormState => ({
  name: '',
  phone: '',
  postCode: '',
  province: '',
  city: '',
  region: '',
  detailAddress: '',
  defaultStatus: false,
});

function toFormState(address?: OrderConfirmAddress | null): FormState {
  if (!address) return emptyForm();
  return {
    name: address.name ?? '',
    phone: address.phone ?? '',
    postCode: address.postCode ?? '',
    province: address.province ?? '',
    city: address.city ?? '',
    region: address.region ?? '',
    detailAddress: address.detailAddress ?? '',
    defaultStatus: address.defaultStatus === 1,
  };
}

export default function OrderAddressBlock({ addresses }: Props) {
  const router = useRouter();
  const [mode, setMode] = useState<'none' | 'add' | 'edit'>('none');
  const [editingId, setEditingId] = useState<number | undefined>();
  const [form, setForm] = useState<FormState>(emptyForm);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState('');

  const editingAddress = useMemo(
    () => addresses.find((a) => a.id === editingId),
    [addresses, editingId],
  );

  const openAdd = () => {
    setMode('add');
    setEditingId(undefined);
    setForm(emptyForm());
    setError('');
  };

  const openEdit = (address: OrderConfirmAddress) => {
    setMode('edit');
    setEditingId(address.id);
    setForm(toFormState(address));
    setError('');
  };

  const closeForm = () => {
    setMode('none');
    setEditingId(undefined);
    setForm(emptyForm());
    setError('');
  };

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const memberId = loadGulimallSession()?.member?.id;
    if (memberId == null || Number.isNaN(Number(memberId))) {
      setError('Please sign in again before saving an address.');
      return;
    }
    if (!form.name.trim() || !form.phone.trim() || !form.detailAddress.trim()) {
      setError('Name, phone and street address are required.');
      return;
    }
    setPending(true);
    setError('');
    try {
      await saveMemberAddress({
        id: mode === 'edit' ? editingId : undefined,
        memberId: Number(memberId),
        name: form.name.trim(),
        phone: form.phone.trim(),
        postCode: form.postCode.trim() || undefined,
        province: form.province.trim() || undefined,
        city: form.city.trim() || undefined,
        region: form.region.trim() || undefined,
        detailAddress: form.detailAddress.trim(),
        defaultStatus: form.defaultStatus ? 1 : 0,
      });
      closeForm();
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save address');
    } finally {
      setPending(false);
    }
  };

  return (
    <div className={styles.addressBlock}>
      {addresses.length === 0 ? (
        <p className={styles.addressHint}>No saved address yet. Add one below to ship your order.</p>
      ) : (
        <>
          <OrderAddressSelector addresses={addresses} />
          <div className={styles.addressActions}>
            {addresses.map((a) => (
              <button key={a.id} type="button" className={styles.linkBtn} onClick={() => openEdit(a)}>
                Edit {a.name}
              </button>
            ))}
          </div>
        </>
      )}

      <div className={styles.addressToolbar}>
        {mode === 'none' ? (
          <button type="button" className={styles.secondaryBtn} onClick={openAdd}>
            {addresses.length === 0 ? 'Add shipping address' : 'Add new address'}
          </button>
        ) : (
          <button type="button" className={styles.linkBtn} onClick={closeForm} disabled={pending}>
            Cancel
          </button>
        )}
      </div>

      {mode !== 'none' && (
        <form className={styles.addressForm} onSubmit={onSubmit}>
          <h3>{mode === 'edit' ? `Edit address${editingAddress ? `: ${editingAddress.name}` : ''}` : 'New address'}</h3>
          {error && <p className={styles.formError}>{error}</p>}
          <div className={styles.formGrid}>
            <label>
              Recipient
              <input
                value={form.name}
                onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                disabled={pending}
                required
              />
            </label>
            <label>
              Phone
              <input
                value={form.phone}
                onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))}
                disabled={pending}
                required
              />
            </label>
            <label>
              Province / State
              <input
                value={form.province}
                onChange={(e) => setForm((f) => ({ ...f, province: e.target.value }))}
                disabled={pending}
              />
            </label>
            <label>
              City
              <input
                value={form.city}
                onChange={(e) => setForm((f) => ({ ...f, city: e.target.value }))}
                disabled={pending}
              />
            </label>
            <label>
              District
              <input
                value={form.region}
                onChange={(e) => setForm((f) => ({ ...f, region: e.target.value }))}
                disabled={pending}
              />
            </label>
            <label>
              Post code
              <input
                value={form.postCode}
                onChange={(e) => setForm((f) => ({ ...f, postCode: e.target.value }))}
                disabled={pending}
              />
            </label>
            <label className={styles.formFull}>
              Street address
              <input
                value={form.detailAddress}
                onChange={(e) => setForm((f) => ({ ...f, detailAddress: e.target.value }))}
                disabled={pending}
                required
              />
            </label>
          </div>
          <label className={styles.checkboxRow}>
            <input
              type="checkbox"
              checked={form.defaultStatus}
              onChange={(e) => setForm((f) => ({ ...f, defaultStatus: e.target.checked }))}
              disabled={pending}
            />
            Set as default address
          </label>
          <button type="submit" className={styles.primaryBtn} disabled={pending}>
            {pending ? 'Saving…' : 'Save address'}
          </button>
        </form>
      )}
    </div>
  );
}
