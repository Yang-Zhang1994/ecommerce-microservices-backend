'use client';

import SearchLink from './SearchLink';
import { useRouter } from 'next/navigation';
import { useEffect, useState, type KeyboardEvent } from 'react';
import {
  SEARCH_SORT_OPTIONS,
  nextSortOnClick,
  normalizeSortParam,
  sortArrowChar,
  sortToButtonId,
} from '@/lib/searchSort';
import {
  buildSearchPath,
  formatSkuPriceRange,
  parseSkuPriceRange,
  toggleHasStockHref,
  type SearchQueryState,
} from '@/lib/searchQuery';
import styles from './page.module.css';

type Props = {
  keyword: string;
  sort: string;
  sortBtn?: string;
  pageNum: number;
  totalPages: number;
  query: SearchQueryState;
};

export default function SearchToolbar({
  keyword,
  sort,
  sortBtn,
  pageNum,
  totalPages,
  query,
}: Props) {
  const router = useRouter();
  const currentSort = normalizeSortParam(sort);
  const activeBtnId = sortToButtonId(sort, sortBtn);
  const stockOnly = query.hasStock === 1;

  const parsedPrice = parseSkuPriceRange(query.skuPrice);
  const [priceMin, setPriceMin] = useState(parsedPrice.min);
  const [priceMax, setPriceMax] = useState(parsedPrice.max);

  useEffect(() => {
    const next = parseSkuPriceRange(query.skuPrice);
    setPriceMin(next.min);
    setPriceMax(next.max);
  }, [query.skuPrice]);

  const pageHref = (p: number) =>
    buildSearchPath({
      ...query,
      keyword,
      sort: currentSort,
      sortBtn: activeBtnId,
      pageNum: p,
    });

  const clearFiltersHref = buildSearchPath({
    ...query,
    keyword,
    catalog3Ids: [],
    brandIds: [],
    attrs: [],
    hasStock: undefined,
    skuPrice: undefined,
    pageNum: 1,
  });

  const hasActiveFilters =
    query.catalog3Ids.length > 0 ||
    query.brandIds.length > 0 ||
    query.attrs.length > 0 ||
    stockOnly ||
    Boolean(query.skuPrice);

  const applyPriceRange = () => {
    const skuPrice = formatSkuPriceRange(priceMin, priceMax);
    router.push(
      buildSearchPath({
        ...query,
        keyword,
        sort: currentSort,
        sortBtn: activeBtnId,
        skuPrice,
        pageNum: 1,
      }),
      { scroll: false },
    );
  };

  const clearPriceHref = buildSearchPath({
    ...query,
    keyword,
    sort: currentSort,
    sortBtn: activeBtnId,
    skuPrice: undefined,
    pageNum: 1,
  });

  const handlePriceKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      applyPriceRange();
    }
  };

  return (
    <div className={styles.toolbar}>
      <div className={styles.toolbarMain}>
        <div className={styles.sort}>
          {SEARCH_SORT_OPTIONS.map((opt) => {
            const isActive = opt.id === activeBtnId;
            const targetSort = nextSortOnClick(opt, isActive, currentSort);
            const arrow = sortArrowChar(isActive, currentSort, opt);
            const href = buildSearchPath({
              ...query,
              keyword,
              sort: targetSort,
              sortBtn: opt.id,
              pageNum: 1,
            });
            return (
              <SearchLink
                key={opt.id}
                href={href}
                className={`${styles.sortBtn} ${isActive ? styles.active : ''}`}
              >
                {opt.label}
                {arrow && <span className={styles.sortArrow}>{arrow}</span>}
              </SearchLink>
            );
          })}

          <span className={styles.toolbarSep} aria-hidden />

          <SearchLink
            href={toggleHasStockHref(query)}
            className={`${styles.stockToggle} ${stockOnly ? styles.stockToggleActive : ''}`}
            aria-pressed={stockOnly}
          >
            <span className={styles.stockToggleBox} aria-hidden />
            In stock only
          </SearchLink>

          <span className={styles.priceRange} role="group" aria-label="Price range">
            <input
              type="number"
              className={styles.priceInput}
              placeholder="Min"
              min={0}
              step="0.01"
              value={priceMin}
              onChange={(e) => setPriceMin(e.target.value)}
              onKeyDown={handlePriceKeyDown}
              aria-label="Minimum price"
            />
            <span className={styles.priceSep}>-</span>
            <input
              type="number"
              className={styles.priceInput}
              placeholder="Max"
              min={0}
              step="0.01"
              value={priceMax}
              onChange={(e) => setPriceMax(e.target.value)}
              onKeyDown={handlePriceKeyDown}
              aria-label="Maximum price"
            />
            <button type="button" className={styles.priceApply} onClick={applyPriceRange}>
              OK
            </button>
            {query.skuPrice && (
              <SearchLink href={clearPriceHref} className={styles.priceClear}>
                Clear
              </SearchLink>
            )}
          </span>

          {hasActiveFilters && (
            <SearchLink href={clearFiltersHref} className={styles.clearFilter}>
              Clear filters
            </SearchLink>
          )}
        </div>
      </div>

      {totalPages > 1 && (
        <div className={styles.pager}>
          {pageNum > 1 && <SearchLink href={pageHref(pageNum - 1)}>← Prev</SearchLink>}
          <span>
            Page {pageNum} / {totalPages}
          </span>
          {pageNum < totalPages && <SearchLink href={pageHref(pageNum + 1)}>Next →</SearchLink>}
        </div>
      )}
    </div>
  );
}
