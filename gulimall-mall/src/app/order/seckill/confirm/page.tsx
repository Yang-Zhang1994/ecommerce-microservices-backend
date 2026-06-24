import { redirect } from 'next/navigation';
import MallShell from '@/components/layout/MallShell';
import SeckillConfirmLoader from '@/components/order/SeckillConfirmLoader';

type Search = Record<string, string | string[] | undefined>;

function firstParam(sp: Search, key: string): string {
  const v = sp[key];
  if (Array.isArray(v)) return v[0] || '';
  return v || '';
}

export const dynamic = 'force-dynamic';

export default function SeckillConfirmPage({ searchParams }: { searchParams: Search }) {
  const orderSn = firstParam(searchParams, 'orderSn').trim();
  if (!orderSn) {
    redirect('/');
  }

  return (
    <MallShell>
      <SeckillConfirmLoader orderSn={orderSn} />
    </MallShell>
  );
}
