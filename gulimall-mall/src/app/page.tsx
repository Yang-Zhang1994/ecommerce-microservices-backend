import Link from 'next/link';
import MallShell from '@/components/layout/MallShell';
import HomeMainLayout from '@/components/home/HomeMainLayout';
import ProductPromoCarousel from '@/components/home/ProductPromoCarousel';
import { HOME_PROMO_BANNERS } from '@/components/home/promoBanners';
import ProductCard from '@/components/ProductCard';
import SeckillSection from '@/components/seckill/SeckillSection';
import UpcomingSeckillSection from '@/components/seckill/UpcomingSeckillSection';
import {
  getCategoryTree,
  getCurrentSeckillSkus,
  getUpcomingSeckillSkus,
  searchProducts,
} from '@/lib/api';
import { BRAND_NAME } from '@/lib/brand';
import type { CategoryEntity, SeckillSku, SkuEsModel } from '@/types/api';
import styles from './page.module.css';

export const dynamic = 'force-dynamic';

export default async function HomePage() {
  let categoryError = '';
  let categories: CategoryEntity[] = [];
  let productError = '';
  let products: SkuEsModel[] = [];
  let seckillSkus: SeckillSku[] = [];
  let upcomingSeckillSkus: SeckillSku[] = [];

  try {
    categories = await getCategoryTree();
  } catch (e) {
    categoryError = e instanceof Error ? e.message : 'Failed to load categories';
  }

  try {
    const result = await searchProducts({ pageNum: 1, pageSize: 12 });
    products = result.list;
  } catch (e) {
    productError = e instanceof Error ? e.message : 'Failed to load products';
  }

  [seckillSkus, upcomingSeckillSkus] = await Promise.all([
    getCurrentSeckillSkus(),
    getUpcomingSeckillSkus(),
  ]);

  return (
    <MallShell>
      <section className={styles.heroStrip}>
        <p className={styles.heroKicker}>Shop smarter</p>
        <h1>Welcome to {BRAND_NAME}</h1>
        <p>Search, add to cart, and checkout with Stripe.</p>
        <div className={styles.heroActions}>
          <Link href="/search" className={styles.primaryBtn}>
            Browse all products
          </Link>
          <Link href="/cart/list" className={styles.secondaryBtn}>
            View cart
          </Link>
        </div>
      </section>

      <HomeMainLayout
        categories={categories}
        categoryError={categoryError || undefined}
        bottom={
          <>
            <SeckillSection products={seckillSkus} />
            <UpcomingSeckillSection products={upcomingSeckillSkus} />
          </>
        }
      >
        <ProductPromoCarousel slides={HOME_PROMO_BANNERS} />

        {productError && <p className={styles.error}>{productError}</p>}

        <div className={styles.sectionHead}>
          <h2>Featured products</h2>
          <Link href="/search">See all →</Link>
        </div>

        {products.length === 0 && !productError ? (
          <p className={styles.empty}>No products in search index yet. Try syncing catalog to Elasticsearch.</p>
        ) : (
          <ul className={styles.grid}>
            {products.map((sku) => (
              <li key={sku.skuId}>
                <ProductCard sku={sku} />
              </li>
            ))}
          </ul>
        )}
      </HomeMainLayout>
    </MallShell>
  );
}
