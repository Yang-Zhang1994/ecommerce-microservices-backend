import Link from 'next/link';
import { Suspense } from 'react';
import CartListNavLink from '@/components/cart/CartListNavLink';
import HeaderAuthNav from '@/components/HeaderAuthNav';
import HeaderSearchForm from '@/components/layout/HeaderSearchForm';
import { BRAND_LOGO_LETTER, BRAND_NAME } from '@/lib/brand';
import styles from './MallShell.module.css';

type Props = {
  children: React.ReactNode;
  showSearch?: boolean;
};

export default function MallShell({ children, showSearch = true }: Props) {
  return (
    <div className={styles.shell}>
      <div className={styles.topBar}>
        <div className={styles.topBarInner}>
          <HeaderAuthNav variant="top" />
          <span className={styles.topSep}>|</span>
          <Link href="/order">My orders</Link>
        </div>
      </div>
      <header className={styles.header}>
        <div className={styles.headerInner}>
          <Link href="/" className={styles.logo}>
            <span className={styles.logoMark}>{BRAND_LOGO_LETTER}</span>
            <span>{BRAND_NAME}</span>
          </Link>
          {showSearch ? (
            <Suspense
              fallback={
                <form className={styles.searchForm} action="/search" method="get">
                  <input
                    className={styles.searchInput}
                    type="search"
                    name="keyword"
                    placeholder="Search products…"
                    aria-label="Search"
                    disabled
                  />
                  <button type="submit" className={styles.searchBtn}>
                    Search
                  </button>
                </form>
              }
            >
              <HeaderSearchForm />
            </Suspense>
          ) : (
            <div />
          )}
          <CartListNavLink href="/cart/list" className={styles.cartLink}>
            <span aria-hidden>🛒</span>
            <span>Cart</span>
          </CartListNavLink>
        </div>
      </header>
      <main className={styles.main}>{children}</main>
      <footer className={styles.footer}>
        <p>
          © {new Date().getFullYear()} {BRAND_NAME} · Demo storefront
        </p>
      </footer>
    </div>
  );
}
