import type { Metadata } from 'next';
import MallShell from '@/components/layout/MallShell';
import CartListContent from '@/components/cart/CartListContent';

export const dynamic = 'force-dynamic';

export const metadata: Metadata = {
  title: 'Cart',
};

export default function CartListPage() {
  return (
    <MallShell>
      <CartListContent />
    </MallShell>
  );
}
