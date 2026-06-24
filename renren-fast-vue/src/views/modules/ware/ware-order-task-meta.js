/** wms_ware_order_task display maps — keep in sync with WareOrderTaskStatusEnum */

export const PAYMENT_WAY_OPTIONS = [
  { value: 1, label: 'Online Payment' },
  { value: 2, label: 'Cash on Delivery' }
]

export const TASK_STATUS_OPTIONS = [
  { value: 1, label: 'Stock Locked', tagType: 'primary' },
  { value: 2, label: 'Stock Released', tagType: 'info' },
  { value: 3, label: 'Stock Deducted', tagType: 'success' }
]

const paymentWayMap = Object.fromEntries(PAYMENT_WAY_OPTIONS.map(o => [o.value, o.label]))
const taskStatusMap = Object.fromEntries(TASK_STATUS_OPTIONS.map(o => [o.value, o]))

export function formatPaymentWay (value) {
  const code = Number(value)
  return paymentWayMap[code] || (value == null || value === '' ? '' : `Unknown (${value})`)
}

export function formatTaskStatus (value) {
  const code = Number(value)
  const meta = taskStatusMap[code]
  return meta ? meta.label : (value == null || value === '' ? '' : `Unknown (${value})`)
}

export function taskStatusTagType (value) {
  const code = Number(value)
  const meta = taskStatusMap[code]
  return meta ? meta.tagType : 'warning'
}
