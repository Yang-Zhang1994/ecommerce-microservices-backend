import Link from 'next/link';
import HighlightedTitle, { plainTitle } from '@/components/HighlightedTitle';
import type { SkuEsModel } from '@/types/api';
import styles from './ProductCard.module.css';

function priceLabel(v: number | undefined): string {
  if (v == null || Number.isNaN(v)) return '—';
  return `$${Number(v).toFixed(2)}`;
}

export default function ProductCard({ sku }: { sku: SkuEsModel }) {
  const img = sku.skuImg?.trim() || '';
  const titlePlain = plainTitle(sku.skuTitle) || 'Product';
  const brand = sku.brandName?.trim();
  return (
    <Link href={`/item/${sku.skuId}`} className={styles.card}>
      <div className={styles.imgWrap}>
        {img ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={img} alt={titlePlain} loading="lazy" />
        ) : (
          <div className={styles.placeholder}>No image</div>
        )}
        {sku.hasStock === false && <span className={styles.badge}>Out of stock</span>}
      </div>
      <div className={styles.body}>
        <h3 className={styles.title}>
          <HighlightedTitle html={sku.skuTitle} className={styles.titleText} />
        </h3>
        {brand && <p className={styles.brand}>{brand}</p>}
        <p className={styles.price}>{priceLabel(sku.skuPrice)}</p>
      </div>
    </Link>
  );
}
