import Link from 'next/link';
import { notFound } from 'next/navigation';
import MallShell from '@/components/layout/MallShell';
import ItemSeckillBanner from '@/components/item-detail/ItemSeckillBanner';
import { getSeckillSkuInfo, getSkuHasStockServer, getSkuItem } from '@/lib/api';
import { buildProductGalleryUrls, buildSkuThumbUrls, uniqueUrls } from '@/lib/productGallery';
import { ItemGalleryClient } from '@/components/item-detail/ItemGalleryClient';
import { ItemSaleAttrPicker } from '@/components/item-detail/ItemSaleAttrPicker';
import ItemBuyBar from './ItemBuyBar';
import styles from './page.module.css';

export const dynamic = 'force-dynamic';

type Props = { params: { skuId: string } };

function money(v: number | undefined): string {
  if (v == null || Number.isNaN(v)) return '—';
  return `$${Number(v).toFixed(2)}`;
}

export default async function ItemDetailPage({ params }: Props) {
  const skuId = params.skuId;
  const item = await getSkuItem(skuId);
  const info = item?.info;
  if (!info) {
    notFound();
  }

  const title = info.skuTitle || info.skuName || `SKU ${skuId}`;
  const skuImages = uniqueUrls((item?.images ?? []).map((i) => i.imgUrl));
  const spuProductImages = uniqueUrls((item?.spuImages ?? []).map((i) => i.imgUrl));
  const productGallery = buildProductGalleryUrls(
    spuProductImages,
    item?.desc?.decript,
  );
  const galleryImages = buildSkuThumbUrls(skuImages, info.skuDefaultImg);
  const mainImg = galleryImages[0] || '';
  const price = info.price;
  const hasStock = await getSkuHasStockServer(Number(skuId));
  const seckill = await getSeckillSkuInfo(Number(skuId));

  return (
    <MallShell>
      <nav className={styles.crumb}>
        <Link href="/">Home</Link>
        <span>/</span>
        <Link href="/search">Products</Link>
        <span>/</span>
        <span>{title}</span>
      </nav>

      <div className={styles.layout}>
        <div className={styles.gallery}>
          <ItemGalleryClient images={galleryImages} alt={title} />
        </div>

        <div className={styles.info}>
          <h1 className={styles.title}>{title}</h1>
          {info.skuSubtitle && <p className={styles.subtitle}>{info.skuSubtitle}</p>}
          <p className={styles.price}>{money(price)}</p>
          {hasStock === false && (
            <p className={styles.stockOut} role="status">
              Out of stock — you can add to cart now and checkout when it is back in stock.
            </p>
          )}
          {hasStock === true && (
            <p className={styles.stockIn} role="status">
              In stock
            </p>
          )}
          {info.saleCount != null && <p className={styles.meta}>Sold: {info.saleCount}</p>}

          {item.saleAttr && item.saleAttr.length > 0 && (
            <ItemSaleAttrPicker skuId={Number(skuId)} saleAttrs={item.saleAttr} />
          )}

          {seckill && (
            <ItemSeckillBanner skuId={Number(skuId)} seckill={seckill} />
          )}

          <ItemBuyBar skuId={Number(skuId)} title={title} img={mainImg || undefined} />

          <Link href="/cart/list" className={styles.cartLink}>
            View cart →
          </Link>
        </div>
      </div>

      {item?.groupAttrs && item.groupAttrs.length > 0 && (
        <section className={styles.attrs}>
          <h2>Specifications</h2>
          <table>
            <tbody>
              {item.groupAttrs.flatMap((g) =>
                (g.attrs ?? []).map((a) => (
                  <tr key={`${g.groupId}-${a.attrId}`}>
                    <th>{a.attrName}</th>
                    <td>{a.attrValue}</td>
                  </tr>
                )),
              )}
            </tbody>
          </table>
        </section>
      )}

      {productGallery.length > 0 && (
        <section className={styles.spuGallery}>
          <h2>Product gallery</h2>
          <ul className={styles.spuGalleryList}>
            {productGallery.map((url) => (
              <li key={url}>
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img src={url} alt="" loading="lazy" />
              </li>
            ))}
          </ul>
        </section>
      )}
    </MallShell>
  );
}
