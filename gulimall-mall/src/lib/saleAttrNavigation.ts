import type { SkuItemSaleAttr } from '@/types/api';

export function parseSkuIdsFromAttr(raw: string | undefined): number[] {
  if (!raw) return [];
  return raw
    .split(',')
    .map((x) => parseInt(x.trim(), 10))
    .filter((n) => Number.isFinite(n));
}

function storageSortKey(value: string): number {
  const tb = value.match(/(\d+)\s*TB/i);
  if (tb) return parseInt(tb[1], 10) * 1024;
  const gb = value.match(/(\d+)\s*GB/i);
  if (gb) return parseInt(gb[1], 10);
  return Number.MAX_SAFE_INTEGER - 1;
}

function compareSaleAttrValues(attrName: string | undefined, a: string, b: string): number {
  const n = (attrName ?? '').trim().toLowerCase();
  if (n === 'ram' || n === 'memory' || n === 'capacity' || n === 'version') {
    const ka = storageSortKey(a);
    const kb = storageSortKey(b);
    if (ka !== kb) return ka - kb;
  }
  return a.localeCompare(b, undefined, { sensitivity: 'base' });
}

export function normalizeSaleAttrValues(
  values: SkuItemSaleAttr['attrValues'],
  attrName?: string,
): { attrValue: string; skuIds: number[] }[] {
  if (!values?.length) return [];
  const out: { attrValue: string; skuIds: number[] }[] = [];
  for (const entry of values) {
    if (entry == null) continue;
    const attrValue = (entry.attrValue ?? '').trim();
    if (!attrValue) continue;
    const skuIds = Array.isArray(entry.skuIds)
      ? entry.skuIds.map((id) => Number(id)).filter((n) => Number.isFinite(n))
      : [];
    out.push({ attrValue, skuIds });
  }
  out.sort((x, y) => compareSaleAttrValues(attrName, x.attrValue, y.attrValue));
  return out;
}

/** Initial selected value per attrId for the current SKU. */
export function initialSaleAttrSelection(
  saleAttrs: SkuItemSaleAttr[],
  currentSkuId: number,
): Record<number, string> {
  const sel: Record<number, string> = {};
  for (const row of saleAttrs) {
    const attrId = row.attrId;
    if (attrId == null) continue;
    const values = normalizeSaleAttrValues(row.attrValues, row.attrName);
    let picked = values.find((v) => v.skuIds.includes(currentSkuId))?.attrValue;
    if (!picked && values.length) picked = values[0].attrValue;
    if (picked) sel[attrId] = picked;
  }
  return sel;
}

/** Intersect skuIds across all selected sale-attr options; empty if incomplete/ambiguous. */
export function resolveSkuIdFromSelection(
  saleAttrs: SkuItemSaleAttr[],
  selected: Record<number, string>,
): number | null {
  let result: number[] | null = null;
  for (const row of saleAttrs) {
    const attrId = row.attrId;
    if (attrId == null) continue;
    const chosen = selected[attrId];
    if (!chosen) return null;
    const values = normalizeSaleAttrValues(row.attrValues, row.attrName);
    const match = values.find((v) => v.attrValue === chosen);
    if (!match) return null;
    const ids = match.skuIds;
    if (!ids.length) return null;
    result = result == null ? [...ids] : result.filter((id) => ids.includes(id));
  }
  if (!result || result.length !== 1) return null;
  return result[0];
}

export function itemDetailUrl(skuId: number): string {
  return `/${skuId}.html`;
}
