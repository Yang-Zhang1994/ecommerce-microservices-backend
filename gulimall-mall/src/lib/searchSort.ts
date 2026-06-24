/** Sort keys supported by gulimall-search (SearchServiceImpl.buildSearchRequest). */

export type SortOption = {
  id: string;
  label: string;
  desc: string;
  asc: string;
};

export const SEARCH_SORT_OPTIONS: SortOption[] = [
  { id: 'relevance', label: 'Relevance', desc: 'hotScore_desc', asc: 'hotScore_asc' },
  { id: 'sales', label: 'Sales', desc: 'saleCount_desc', asc: 'saleCount_asc' },
  { id: 'price', label: 'Price', desc: 'skuPrice_desc', asc: 'skuPrice_asc' },
];

const LEGACY_SORT: Record<string, string> = {
  price_desc: 'skuPrice_desc',
  price_asc: 'skuPrice_asc',
  salecount_desc: 'saleCount_desc',
  salecount_asc: 'saleCount_asc',
};

/** Normalize URL sort param to backend field_sort form. */
export function normalizeSortParam(sort: string | undefined | null): string {
  const raw = (sort || '').trim();
  if (!raw || raw === 'default') {
    return 'hotScore_desc';
  }
  const lower = raw.toLowerCase();
  return LEGACY_SORT[lower] ?? raw;
}

/** Map sort value → active toolbar button id. */
export function sortToButtonId(sort: string | undefined | null, sortBtn?: string | null): string {
  const btn = (sortBtn || '').trim();
  if (btn && SEARCH_SORT_OPTIONS.some((o) => o.id === btn)) {
    return btn;
  }
  const normalized = normalizeSortParam(sort);
  for (const opt of SEARCH_SORT_OPTIONS) {
    if (normalized === opt.desc || normalized === opt.asc) {
      return opt.id;
    }
  }
  return 'relevance';
}

/** Value for search API: omit default hotScore_desc. */
export function sortForSearchApi(sort: string | undefined | null): string | undefined {
  const normalized = normalizeSortParam(sort);
  if (normalized === 'hotScore_desc') {
    return undefined;
  }
  return normalized;
}

export function nextSortOnClick(
  option: SortOption,
  isActive: boolean,
  currentNormalized: string,
): string {
  if (isActive) {
    return currentNormalized === option.desc ? option.asc : option.desc;
  }
  return option.desc;
}

export function sortArrowChar(isActive: boolean, currentNormalized: string, option: SortOption): string {
  if (!isActive) return '';
  return currentNormalized === option.desc ? '▼' : '▲';
}
