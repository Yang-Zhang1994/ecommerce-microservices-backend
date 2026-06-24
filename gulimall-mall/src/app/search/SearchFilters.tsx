import SearchLink from './SearchLink';
import type { SearchAttrVo, SearchBrandVo, SearchCatalogVo } from '@/types/api';
import {
  buildSearchPath,
  toggleAttrHref,
  toggleBrandHref,
  toggleCatalogHref,
  type SearchQueryState,
} from '@/lib/searchQuery';
import styles from './page.module.css';

type Props = {
  query: SearchQueryState;
  catalogs: SearchCatalogVo[];
  brands: SearchBrandVo[];
  attrs: SearchAttrVo[];
};

export default function SearchFilters({ query, catalogs, brands, attrs }: Props) {
  const hasFilters = catalogs.length > 0 || brands.length > 0 || attrs.length > 0;

  if (!hasFilters) {
    return (
      <div className={styles.filters}>
        <p className={styles.filtersEmpty}>
          No filters for this result set. Try another keyword or category.
        </p>
      </div>
    );
  }

  const uniqueBrands = brands.filter((b, i, arr) => {
    const id = b.brandId;
    return id != null && arr.findIndex((x) => x.brandId === id) === i;
  });

  return (
    <div className={styles.filters} id="search-filters">
      {catalogs.length > 0 && (
        <section className={styles.filterRow}>
          <span className={styles.filterKey}>Category</span>
          <ul className={styles.filterValues}>
            {catalogs.map((c) => (
              <li key={c.catalogId}>
                <SearchLink href={toggleCatalogHref(query, c.catalogId)} className={styles.filterLink}>
                  {c.catalogName || c.catalogId}
                </SearchLink>
              </li>
            ))}
          </ul>
        </section>
      )}

      {uniqueBrands.length > 0 && (
        <section className={styles.filterRow}>
          <span className={styles.filterKey}>Brand</span>
          <ul className={`${styles.filterValues} ${styles.brandLogos}`}>
            {uniqueBrands.map((b) => (
              <li key={b.brandId}>
                <SearchLink
                  href={toggleBrandHref(query, b.brandId)}
                  className={styles.brandLink}
                  title={b.brandName || String(b.brandId)}
                >
                  {b.brandImg ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={b.brandImg} alt="" className={styles.brandImg} />
                  ) : (
                    <span className={styles.brandFallback}>{b.brandName || b.brandId}</span>
                  )}
                </SearchLink>
              </li>
            ))}
          </ul>
        </section>
      )}

      {attrs.map((attr) => {
        const values = attr.attrValue?.filter(Boolean) ?? [];
        if (values.length === 0) return null;
        return (
          <section className={styles.filterRow} key={attr.attrId ?? attr.attrName}>
            <span className={styles.filterKey}>{attr.attrName || 'Attribute'}</span>
            <ul className={styles.filterValues}>
              {values.map((v) => (
                <li key={`${attr.attrId}-${v}`}>
                  <SearchLink
                    href={toggleAttrHref(query, attr.attrId, v)}
                    className={styles.filterLink}
                  >
                    {v}
                  </SearchLink>
                </li>
              ))}
            </ul>
          </section>
        );
      })}

      {(query.catalog3Ids.length > 0 ||
        query.brandIds.length > 0 ||
        query.attrs.length > 0 ||
        query.hasStock === 1 ||
        query.skuPrice) && (
        <p className={styles.clearAll}>
          <SearchLink
            href={buildSearchPath({
              ...query,
              catalog3Ids: [],
              brandIds: [],
              attrs: [],
              hasStock: undefined,
              skuPrice: undefined,
              pageNum: 1,
            })}
          >
            Clear all filters
          </SearchLink>
        </p>
      )}
    </div>
  );
}
