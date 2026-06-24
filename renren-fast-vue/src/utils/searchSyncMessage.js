/**
 * Build admin toast after save when API returns searchSynced (Plan B).
 * @param {object} data response body with code and optional searchSynced
 * @param {string} savedLabel e.g. "Saved"
 * @returns {string|null} message or null if not a success response
 */
export function formatSaveWithSearchSync(data, savedLabel = "Saved") {
  if (!data || data.code !== 0) {
    return null;
  }
  if (data.searchSynced) {
    return `${savedLabel}; search index synced`;
  }
  return `${savedLabel}; product is not on sale — search was not updated`;
}
