import Link from 'next/link';
import MallShell from '@/components/layout/MallShell';

export default function ItemNotFound() {
  return (
    <MallShell>
      <div style={{ padding: '48px 0', textAlign: 'center' }}>
        <h1>Product not found</h1>
        <p style={{ margin: '12px 0 20px', color: '#71717a' }}>This SKU may be unavailable or removed.</p>
        <Link href="/search" style={{ color: 'var(--mall-brand, #c81623)' }}>
          Back to search
        </Link>
      </div>
    </MallShell>
  );
}
