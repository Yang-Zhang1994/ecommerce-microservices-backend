import MallShell from '@/components/layout/MallShell';
import ProductCard from '@/components/ProductCard';
import { searchProducts } from '@/lib/api';
import { hasSearchCriteria, parseSearchParams } from '@/lib/searchQuery';
import { sortForSearchApi } from '@/lib/searchSort';
import type { SearchResultData } from '@/types/api';
import SearchBreadcrumb from './SearchBreadcrumb';
import SearchFilters from './SearchFilters';
import SearchToolbar from './SearchToolbar';
import styles from './page.module.css';

export const dynamic = 'force-dynamic';

type Props = {
  searchParams: Record<string, string | string[] | undefined>;
};

export default async function SearchPage({ searchParams }: Props) {
  const query = parseSearchParams(searchParams);
  const sortForApi = sortForSearchApi(query.sort);

  let error = '';
  let result: SearchResultData = {
    list: [],
    total: 0,
    pageNum: 1,
    totalPages: 0,
    brands: [],
    catalogs: [],
    attrs: [],
    navs: [],
  };

  try {
    result = await searchProducts({
      keyword: query.keyword || undefined,
      catalog3Id: query.catalog3Ids.length ? query.catalog3Ids : undefined,
      brandId: query.brandIds.length ? query.brandIds : undefined,
      attrs: query.attrs.length ? query.attrs : undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize ?? 20,
      sort: sortForApi,
      hasStock: query.hasStock,
      skuPrice: query.skuPrice,
    });
  } catch (e) {
    error = e instanceof Error ? e.message : 'Search failed';
  }

  const hasFilters = hasSearchCriteria(query);

  return (
    <MallShell>
      <div className={styles.head}>
        <h1>Search results</h1>
        {query.keyword && (
          <p className={styles.kw}>
            Keyword: &quot;{query.keyword}&quot;
          </p>
        )}
        {!query.keyword && !hasFilters && (
          <p className={styles.browseAll}>Browsing all products</p>
        )}
        <p className={styles.count}>{result.total} product(s)</p>
      </div>

      {!error && (
        <>
          <SearchBreadcrumb query={query} navs={result.navs ?? []} />

          <SearchFilters
            query={query}
            catalogs={result.catalogs ?? []}
            brands={result.brands ?? []}
            attrs={result.attrs ?? []}
          />

          <SearchToolbar
            keyword={query.keyword}
            sort={query.sort}
            sortBtn={query.sortBtn}
            pageNum={result.pageNum || query.pageNum}
            totalPages={result.totalPages}
            query={query}
          />
        </>
      )}

      {error && <p className={styles.error}>{error}</p>}

      {!error &&
        (result.list.length === 0 ? (
          <p className={styles.empty}>No products found. Try another keyword or filter.</p>
        ) : (
          <ul className={styles.grid} id="search-results">
            {result.list.map((sku) => (
              <li key={sku.skuId}>
                <ProductCard sku={sku} />
              </li>
            ))}
          </ul>
        ))}
    </MallShell>
  );
}
