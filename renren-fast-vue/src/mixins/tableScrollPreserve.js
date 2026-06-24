/**
 * Preserves el-table body scroll position across dataList reloads (after CRUD).
 * Works on any list page that uses `dataListLoading`.
 */
function captureTableScroll (vm) {
  const root = vm.$el
  if (!root) return null
  const body = root.querySelector('.el-table__body-wrapper')
  return body ? body.scrollTop : null
}

function restoreTableScroll (vm, scrollTop) {
  if (scrollTop == null) return
  vm.$nextTick(() => {
    const body = vm.$el && vm.$el.querySelector('.el-table__body-wrapper')
    if (body) body.scrollTop = scrollTop
  })
}

export default {
  watch: {
    dataListLoading (val, oldVal) {
      if (typeof val !== 'boolean' || typeof oldVal !== 'boolean') return
      if (val === true && oldVal === false) {
        this._tableScrollTop = captureTableScroll(this)
      } else if (val === false && oldVal === true) {
        restoreTableScroll(this, this._tableScrollTop)
        this._tableScrollTop = null
      }
    }
  }
}
