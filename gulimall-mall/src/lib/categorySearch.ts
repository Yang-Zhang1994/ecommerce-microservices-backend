import type { CategoryEntity } from '@/types/api';

/** Collect level-3 category ids under a node (for ES catalogId filter). */
export function collectCatalog3Ids(cat: CategoryEntity): number[] {
  if (cat.catLevel === 3) {
    return [cat.catId];
  }
  if (!cat.children?.length) {
    return [];
  }
  return cat.children.flatMap(collectCatalog3Ids);
}

/** Build /search link with catalog3Id query params for a category at any level. */
export function categorySearchHref(cat: CategoryEntity): string {
  const ids = collectCatalog3Ids(cat);
  if (ids.length === 0) {
    return '/search';
  }
  const q = new URLSearchParams();
  ids.forEach((id) => q.append('catalog3Id', String(id)));
  return `/search?${q.toString()}`;
}
