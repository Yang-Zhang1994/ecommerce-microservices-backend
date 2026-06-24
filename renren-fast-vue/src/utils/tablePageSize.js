/**
 * Persist admin table page size per route (survives browser refresh in the same tab).
 */
export const TABLE_PAGE_SIZES = [10, 20, 50, 100]
const STORAGE_PREFIX = 'admin:table:pageSize:'

export function pageSizeStorageKey (route) {
  const name = (route && (route.name || route.path)) || 'unknown'
  return STORAGE_PREFIX + name
}

export function loadSavedPageSize (route, fallback = 10) {
  try {
    const raw = sessionStorage.getItem(pageSizeStorageKey(route))
    if (!raw) return fallback
    const n = parseInt(raw, 10)
    return TABLE_PAGE_SIZES.includes(n) ? n : fallback
  } catch (e) {
    return fallback
  }
}

export function savePageSize (route, size) {
  if (!route || typeof size !== 'number' || !TABLE_PAGE_SIZES.includes(size)) {
    return
  }
  try {
    sessionStorage.setItem(pageSizeStorageKey(route), String(size))
  } catch (e) {
    // ignore quota / private mode
  }
}
