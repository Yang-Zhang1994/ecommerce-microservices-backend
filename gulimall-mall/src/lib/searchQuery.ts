/** Parse / build mall search URL query (keyword, catalog, brand, attrs, sort, page). */

export type SearchQueryState = {
  keyword: string;
  catalog3Ids: number[];
  brandIds: number[];
  attrs: string[];
  sort: string;
  sortBtn: string;
  pageNum: number;
  pageSize?: number;
  hasStock?: number;
  skuPrice?: string;
};

function paramOne(v: string | string[] | undefined): string {
  if (Array.isArray(v)) return v[0] ?? '';
  return v ?? '';
}

function paramAll(v: string | string[] | undefined): string[] {
  if (Array.isArray(v)) return v.filter(Boolean);
  return v ? [v] : [];
}

function parseIds(v: string | string[] | undefined): number[] {
  return paramAll(v)
    .map((x) => parseInt(x, 10))
    .filter((n) => Number.isFinite(n) && n > 0);
}

export function parseSearchParams(
  sp: Record<string, string | string[] | undefined>
): SearchQueryState {
  const hasStockRaw = paramOne(sp.hasStock);
  const hasStock =
    hasStockRaw === '1' ? 1 : hasStockRaw === '0' ? 0 : undefined;
  return {
    keyword: paramOne(sp.keyword).trim(),
    catalog3Ids: parseIds(sp.catalog3Id),
    brandIds: parseIds(sp.brandId),
    attrs: paramAll(sp.attrs).filter(Boolean),
    sort: paramOne(sp.sort),
    sortBtn: paramOne(sp.sortBtn),
    pageNum: Math.max(1, parseInt(paramOne(sp.pageNum) || '1', 10) || 1),
    pageSize: parseInt(paramOne(sp.pageSize) || '20', 10) || 20,
    hasStock,
    skuPrice: paramOne(sp.skuPrice).trim() || undefined,
  };
}

export function hasSearchCriteria(state: SearchQueryState): boolean {
  return (
    Boolean(state.keyword) ||
    state.catalog3Ids.length > 0 ||
    state.brandIds.length > 0 ||
    state.attrs.length > 0 ||
    state.hasStock === 1 ||
    Boolean(state.skuPrice)
  );
}

/** Parse backend skuPrice param (min_max) into form fields. */
export function parseSkuPriceRange(skuPrice?: string): { min: string; max: string } {
  if (!skuPrice?.includes('_')) {
    return { min: '', max: '' };
  }
  const idx = skuPrice.indexOf('_');
  return {
    min: skuPrice.slice(0, idx),
    max: skuPrice.slice(idx + 1),
  };
}

/** Build skuPrice query value from min/max inputs (legacy list.js format). */
export function formatSkuPriceRange(min: string, max: string): string | undefined {
  const minVal = min.trim();
  const maxVal = max.trim();
  if (!minVal && !maxVal) {
    return undefined;
  }
  return `${minVal}_${maxVal}`;
}

export function toggleHasStockHref(state: SearchQueryState): string {
  return buildSearchPath(state, {
    hasStock: state.hasStock === 1 ? undefined : 1,
    pageNum: 1,
  });
}

/** Toggle catalog3Id in list (same behavior as legacy list.js). */
function toggleId(ids: number[], id: number): number[] {
  const next = ids.slice();
  const idx = next.indexOf(id);
  if (idx >= 0) next.splice(idx, 1);
  else next.push(id);
  return next;
}

function toggleString(list: string[], value: string): string[] {
  const next = list.slice();
  const idx = next.indexOf(value);
  if (idx >= 0) next.splice(idx, 1);
  else next.push(value);
  return next;
}

export function buildSearchPath(
  state: SearchQueryState,
  override?: Partial<SearchQueryState>
): string {
  const merged: SearchQueryState = { ...state, ...override };
  const q = new URLSearchParams();

  if (merged.keyword) q.set('keyword', merged.keyword);
  merged.catalog3Ids.forEach((id) => q.append('catalog3Id', String(id)));
  merged.brandIds.forEach((id) => q.append('brandId', String(id)));
  merged.attrs.forEach((a) => q.append('attrs', a));

  const sort = (merged.sort || '').trim();
  const sortBtn = (merged.sortBtn || '').trim();
  const isDefaultRelevance = sort === 'hotScore_desc' && sortBtn === 'relevance';
  if (sort && sort !== 'hotScore_desc') q.set('sort', sort);
  else if (sort && !isDefaultRelevance) q.set('sort', sort);
  if (sortBtn && !isDefaultRelevance) q.set('sortBtn', sortBtn);

  if (merged.hasStock === 1) q.set('hasStock', '1');
  if (merged.skuPrice) q.set('skuPrice', merged.skuPrice);

  q.set('pageNum', String(Math.max(1, merged.pageNum)));
  if (merged.pageSize && merged.pageSize !== 20) {
    q.set('pageSize', String(merged.pageSize));
  }

  const qs = q.toString();
  return qs ? `/search?${qs}` : '/search';
}

export function toggleCatalogHref(state: SearchQueryState, catalogId: number): string {
  return buildSearchPath(state, {
    catalog3Ids: toggleId(state.catalog3Ids, catalogId),
    pageNum: 1,
  });
}

export function toggleBrandHref(state: SearchQueryState, brandId: number): string {
  return buildSearchPath(state, {
    brandIds: toggleId(state.brandIds, brandId),
    pageNum: 1,
  });
}

export function toggleAttrHref(state: SearchQueryState, attrId: number, value: string): string {
  const token = `${attrId}_${(value || '').trim()}`;
  if (!token || token === '_') return buildSearchPath(state, { pageNum: 1 });
  return buildSearchPath(state, {
    attrs: toggleString(state.attrs, token),
    pageNum: 1,
  });
}

/** Strip legacy static-page hash from backend nav links. */
export function normalizeNavLink(link: string): string {
  const raw = (link || '').trim();
  if (!raw) return '/search';
  return raw.split('#')[0] || '/search';
}
