/** ES search highlight uses <b style='color:red'>…</b> in skuTitle — render safely, not as literal text. */

export function plainTitle(raw: string | null | undefined): string {
  if (!raw) return '';
  return raw.replace(/<[^>]*>/g, '').trim();
}

export function hasSearchHighlight(raw: string | null | undefined): boolean {
  if (!raw) return false;
  return /<\s*b\b/i.test(raw);
}

/** Keep only <b> / </b> from Elasticsearch highlighter output. */
export function sanitizeSearchHighlight(raw: string): string {
  if (!raw) return '';
  return raw
    .replace(/<\s*script\b[^>]*>[\s\S]*?<\s*\/\s*script\s*>/gi, '')
    .replace(/<[^>]+>/gi, (tag) => {
      const t = tag.toLowerCase().trim();
      if (t === '<b>' || t.startsWith('<b ') || t === '</b>') {
        return t.startsWith('</') ? '</b>' : '<b>';
      }
      return '';
    });
}
