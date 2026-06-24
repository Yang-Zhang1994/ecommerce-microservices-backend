import { loadSavedPageSize, savePageSize } from '@/utils/tablePageSize'

/**
 * Restores `pageSize` from sessionStorage for list pages (keyed by route name/path).
 * Saves whenever pageSize changes (e.g. user picks 100 per page).
 */
export default {
  created () {
    this.restoreTablePageSize()
  },
  activated () {
    this.restoreTablePageSize()
  },
  watch: {
    pageSize (val) {
      if (typeof val === 'number' && this.$route) {
        savePageSize(this.$route, val)
      }
    }
  },
  methods: {
    restoreTablePageSize () {
      if (typeof this.pageSize !== 'number' || !this.$route) {
        return
      }
      const saved = loadSavedPageSize(this.$route, this.pageSize)
      if (saved !== this.pageSize) {
        this.pageSize = saved
      }
    }
  }
}
