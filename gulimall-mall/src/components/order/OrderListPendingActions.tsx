'use client';

import Link from 'next/link';
import OrderCancelButton from '@/components/order/OrderCancelButton';
import listStyles from '@/app/order/page.module.css';

type Props = {
  orderSn: string;
  payHref: string;
  isSeckill?: boolean;
};

export default function OrderListPendingActions({ orderSn, payHref, isSeckill }: Props) {
  return (
    <div className={listStyles.actions}>
      <Link href={payHref} className={`${listStyles.btn} ${listStyles.btnPrimary}`}>
        Pay now
      </Link>
      <OrderCancelButton orderSn={orderSn} isSeckill={isSeckill} />
      <Link href="/search" className={listStyles.btn}>
        Shop more
      </Link>
    </div>
  );
}
