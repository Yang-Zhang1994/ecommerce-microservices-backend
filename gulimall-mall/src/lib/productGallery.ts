/** Comma-separated intro image URLs in pms_spu_info_desc.decript (admin: Product Introduction). */
export function parseIntroImageUrls(decript: string | undefined): string[] {
  if (!decript?.trim()) return [];
  const parts = decript.split(',').map((s) => s.trim()).filter(Boolean);
  if (!parts.length || !parts.every((p) => /^https?:\/\//i.test(p))) return [];
  return parts;
}

export function uniqueUrls(urls: (string | undefined)[]): string[] {
  const out: string[] = [];
  const seen = new Set<string>();
  for (const u of urls) {
    const t = u?.trim();
    if (!t || seen.has(t)) continue;
    seen.add(t);
    out.push(t);
  }
  return out;
}

/** Left thumbnail strip: SKU images only (falls back to skuDefaultImg). */
export function buildSkuThumbUrls(
  skuImageUrls: (string | undefined)[],
  skuDefaultImg?: string,
): string[] {
  const list = uniqueUrls(skuImageUrls);
  if (list.length > 0) return list;
  const fallback = skuDefaultImg?.trim();
  return fallback ? [fallback] : [];
}

/**
 * Product gallery on the detail page:
 * 1) Admin「Product Images」→ pms_spu_images (spuImages)
 * 2) Admin「Product Introduction」→ pms_spu_info_desc.decript (intro long images)
 */
export function buildProductGalleryUrls(
  spuImageUrls: (string | undefined)[],
  decript?: string,
): string[] {
  return uniqueUrls([...uniqueUrls(spuImageUrls), ...parseIntroImageUrls(decript)]);
}
