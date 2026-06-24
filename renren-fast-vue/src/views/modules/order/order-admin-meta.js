/**
 * Admin display maps for oms_order / oms_payment_info.
 * Order status codes: com.atguigu.gulimall.order.enums.OrderStatusEnum
 */

export const ORDER_STATUS_OPTIONS = [
  { value: 0, label: 'Pending Payment', tagType: 'warning' },
  { value: 1, label: 'Paid', tagType: 'success' },
  { value: 2, label: 'Shipped', tagType: 'primary' },
  { value: 3, label: 'Completed', tagType: 'success' },
  { value: 4, label: 'Closed', tagType: 'info' },
  { value: 5, label: 'Invalid', tagType: 'danger' }
]

export const PAY_TYPE_OPTIONS = [
  { value: 1, label: 'Online (Stripe)', tagType: 'success' },
  { value: 2, label: 'WeChat Pay', tagType: 'success' },
  { value: 3, label: 'UnionPay', tagType: 'primary' },
  { value: 4, label: 'Cash on Delivery', tagType: 'warning' }
]

export const SOURCE_TYPE_OPTIONS = [
  { value: 0, label: 'PC', tagType: 'info' },
  { value: 1, label: 'App', tagType: 'primary' }
]

export const BILL_TYPE_OPTIONS = [
  { value: 0, label: 'No Invoice', tagType: 'info' },
  { value: 1, label: 'Electronic', tagType: 'primary' },
  { value: 2, label: 'Paper', tagType: 'warning' }
]

export const CONFIRM_STATUS_OPTIONS = [
  { value: 0, label: 'Unconfirmed', tagType: 'warning' },
  { value: 1, label: 'Confirmed', tagType: 'success' }
]

export const DELETE_STATUS_OPTIONS = [
  { value: 0, label: 'Active', tagType: 'success' },
  { value: 1, label: 'Deleted', tagType: 'danger' }
]

/** oms_payment_info.payment_status (Stripe + legacy) */
export const PAYMENT_STATUS_LABELS = {
  INIT: { label: 'Checkout Started', tagType: 'info' },
  SUCCEEDED: { label: 'Paid', tagType: 'success' },
  PAID: { label: 'Paid', tagType: 'success' },
  paid: { label: 'Paid', tagType: 'success' },
  UNPAID: { label: 'Unpaid', tagType: 'warning' },
  unpaid: { label: 'Unpaid', tagType: 'warning' },
  FAILED: { label: 'Failed', tagType: 'danger' },
  failed: { label: 'Failed', tagType: 'danger' },
  CANCELED: { label: 'Canceled', tagType: 'info' },
  canceled: { label: 'Canceled', tagType: 'info' },
  'no_payment_required': { label: 'No Payment Required', tagType: 'info' }
}

function lookupByCode (options, value) {
  if (value == null || value === '') return null
  const code = Number(value)
  if (Number.isNaN(code)) return null
  return options.find(o => o.value === code) || null
}

export function formatFromOptions (options, value) {
  const meta = lookupByCode(options, value)
  return meta ? meta.label : (value == null || value === '' ? '' : `Unknown (${value})`)
}

export function tagTypeFromOptions (options, value) {
  const meta = lookupByCode(options, value)
  return meta ? meta.tagType : 'warning'
}

export function formatOrderStatus (value) {
  return formatFromOptions(ORDER_STATUS_OPTIONS, value)
}

export function orderStatusTagType (value) {
  return tagTypeFromOptions(ORDER_STATUS_OPTIONS, value)
}

export function formatPayType (value) {
  return formatFromOptions(PAY_TYPE_OPTIONS, value)
}

export function payTypeTagType (value) {
  return tagTypeFromOptions(PAY_TYPE_OPTIONS, value)
}

export function formatSourceType (value) {
  return formatFromOptions(SOURCE_TYPE_OPTIONS, value)
}

export function sourceTypeTagType (value) {
  return tagTypeFromOptions(SOURCE_TYPE_OPTIONS, value)
}

export function formatBillType (value) {
  return formatFromOptions(BILL_TYPE_OPTIONS, value)
}

export function billTypeTagType (value) {
  return tagTypeFromOptions(BILL_TYPE_OPTIONS, value)
}

export function formatConfirmStatus (value) {
  return formatFromOptions(CONFIRM_STATUS_OPTIONS, value)
}

export function confirmStatusTagType (value) {
  return tagTypeFromOptions(CONFIRM_STATUS_OPTIONS, value)
}

export function formatDeleteStatus (value) {
  return formatFromOptions(DELETE_STATUS_OPTIONS, value)
}

export function deleteStatusTagType (value) {
  return tagTypeFromOptions(DELETE_STATUS_OPTIONS, value)
}

export function formatPaymentStatus (value) {
  if (value == null || value === '') return ''
  const key = String(value).trim()
  const exact = PAYMENT_STATUS_LABELS[key]
  if (exact) return exact.label
  const upper = PAYMENT_STATUS_LABELS[key.toUpperCase()]
  if (upper) return upper.label
  const lower = PAYMENT_STATUS_LABELS[key.toLowerCase()]
  if (lower) return lower.label
  return key.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase())
}

export function paymentStatusTagType (value) {
  if (value == null || value === '') return 'info'
  const key = String(value).trim()
  const meta = PAYMENT_STATUS_LABELS[key] ||
    PAYMENT_STATUS_LABELS[key.toUpperCase()] ||
    PAYMENT_STATUS_LABELS[key.toLowerCase()]
  return meta ? meta.tagType : 'warning'
}
