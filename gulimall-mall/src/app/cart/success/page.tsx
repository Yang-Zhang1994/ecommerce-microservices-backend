import type { Metadata } from 'next';
import Link from 'next/link';
import CartListNavLink from '@/components/cart/CartListNavLink';
import MallShell from '@/components/layout/MallShell';
import { getCurrentCartServer } from '@/lib/api';
import { cartSuccessMessages as t } from '@/i18n/cartSuccess';
import styles from './page.module.css';

export const dynamic = 'force-dynamic';

export const metadata: Metadata = {
  title: t.metaTitle,
};

type Search = Record<string, string | string[] | undefined>;

function firstParam(sp: Search, key: string): string | undefined {
  const v = sp[key];
  if (Array.isArray(v)) return v[0];
  return v ?? undefined;
}

function ErrorCard({ title, detail }: { title: string; detail: string }) {
  return (
    <div className={styles.shell}>
      <div className={styles.cardError}>
        <div className={styles.warnMark}>!</div>
        <h1 className={styles.errTitle}>{title}</h1>
        <p className={styles.errDetail}>{detail}</p>
        <p className={styles.links}>
          <Link href="/">{t.home}</Link>
          <span className={styles.linksSep}>|</span>
          <CartListNavLink href="/cart/list" className={styles.linksSecondary}>
            {t.cart}
          </CartListNavLink>
        </p>
      </div>
    </div>
  );
}

export default async function CartSuccessPage({ searchParams }: { searchParams: Search }) {
  const err = firstParam(searchParams, 'err');
  const skuIdRaw = firstParam(searchParams, 'skuId');

  if (err === 'invalidSku') {
    return (
      <MallShell>
        <ErrorCard title={t.errInvalidSku} detail={t.errInvalidSkuDetail} />
      </MallShell>
    );
  }

  if (!skuIdRaw?.trim()) {
    return (
      <MallShell>
        <ErrorCard title={t.errMissingSku} detail={t.errMissingSkuDetail} />
      </MallShell>
    );
  }

  const skuIdStr = skuIdRaw.trim();
  const cartResult = await getCurrentCartServer();

  if (!cartResult.ok) {
    return (
      <MallShell>
        <ErrorCard title={t.errVerifyFailed} detail={t.errVerifyFailedDetail} />
      </MallShell>
    );
  }

  const items = cartResult.cart?.items ?? [];
  const match = items.find((it) => String(it.skuId) === skuIdStr);

  if (!match) {
    return (
      <MallShell>
        <ErrorCard title={t.errNotInCart} detail={t.errNotInCartDetail} />
      </MallShell>
    );
  }

  const displayTitle = match.title?.trim() || `SKU ${skuIdStr}`;
  const displayImg = match.image?.trim() || '';
  const qty = typeof match.count === 'number' && match.count > 0 ? match.count : 1;
  const detailHref = `/item/${encodeURIComponent(skuIdStr)}`;

  return (
    <MallShell>
      <div className={styles.shell}>
        <article className={styles.cardSuccess}>
          <div className={styles.cardHead}>
            <div className={styles.rowTitle}>
              <span className={styles.checkIcon} aria-hidden />
              <h1 className={styles.h1}>
                {t.titleAdded}
                {' · '}
                SKU {skuIdStr}
              </h1>
            </div>
          </div>
          <div className={styles.productRow}>
            <div className={styles.imgWrap}>
              {displayImg ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img src={displayImg} alt="" width={96} height={96} />
              ) : (
                <div className={styles.imgPlaceholder} />
              )}
            </div>
            <div className={styles.info}>
              <p className={styles.productName}>
                <Link href={detailHref}>{displayTitle}</Link>
              </p>
              <p className={styles.qty}>{t.qty(qty)}</p>
            </div>
          </div>
          <div className={styles.actions}>
            <Link href={detailHref} className={styles.btnGhost}>
              {t.viewItem}
            </Link>
            <CartListNavLink href="/cart/list" className={styles.btnPrimary}>
              {t.proceedCart}
            </CartListNavLink>
          </div>
        </article>
      </div>
    </MallShell>
  );
}
