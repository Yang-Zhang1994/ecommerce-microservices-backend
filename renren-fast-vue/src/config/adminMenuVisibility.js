/**
 * Hide admin sidebar entries that are not used in the GrainMart stack or are broken in local Docker.
 *
 * Marketing: only Points / Full Discount / Member Price stay visible (stored for future mall use).
 * All other coupon/* menus are hidden (Issue History, Subject, Seckill, Coupons, etc.).
 *
 * Orders: mall uses confirm / submit / list / Stripe pay only. Hide return, refund, settings,
 * line-item CRUD, operate history, and codegen CRUD menus until those flows exist on the storefront.
 *
 * Content: CMS menus (index / hot category / comments) have no renren-fast-vue pages or mall wiring.
 */

/** Match menu.url (no leading slash, lowercased). */
export const hiddenAdminMenuUrlPrefixes = [
  'job/schedule',
  'sys/config',
  'oss/oss',
  'member/growth',
  'member/statistics'
]

/** Content Management — no modules under renren-fast-vue/src/views/modules/content. */
export const hiddenContentMenuUrlPrefixes = [
  'content/index',
  'content/category',
  'content/comments'
]

export const hiddenContentMenuNames = new Set([
  'content management',
  'content',
  'index',
  'category hot',
  'comments',
  '内容管理',
  '首页推荐',
  '分类热门',
  '评论管理'
])

/** Member sidebar labels with no storefront / admin page wired yet. */
export const hiddenMemberMenuNames = new Set([
  'growth',
  'growth history',
  'statistics',
  'member statistics',
  '积分变化',
  '成长值',
  '成长值变化',
  '统计信息'
])

/** Order module URLs kept in the sidebar (RDS + renren codegen). */
export const visibleOrderMenuUrls = [
  'order/order',
  'order/payment',
  'order/paymentinfo'
]

/** Unused order admin pages (custom short paths + codegen entity paths). */
export const hiddenOrderMenuUrlPrefixes = [
  'order/return',
  'order/refund',
  'order/settings',
  'order/orderreturnapply',
  'order/orderreturnreason',
  'order/refundinfo',
  'order/ordersetting',
  'order/orderoperatehistory',
  'order/orderitem'
]

/** Sidebar labels (English after menuTranslation + Chinese in RDS). */
export const hiddenOrderMenuNames = new Set([
  'return',
  'returns',
  'refund',
  'refunds',
  'settings',
  'order settings',
  'return reason',
  'return reasons',
  'operate history',
  'order operate history',
  'order items',
  'order item',
  'level rules',
  '退货单处理',
  '退款流水查询',
  '订单设置',
  '退货原因',
  '操作历史',
  '订单明细',
  '等级规则'
])

/** Always hide (sidebar labels after translation or English in DB). */
export const hiddenMarketingMenuNames = new Set([
  'issue history',
  'coupon history',
  'coupons',
  'subject',
  'campaigns',
  'seckill',
  'flash sales',
  'daily flash sale',
  '发放记录',
  '专题活动',
  '秒杀活动',
  '优惠券管理',
  '每日秒杀'
])

/** Only these marketing menus stay in the sidebar (English + Chinese labels). */
export const visibleMarketingMenuNames = new Set([
  'bounds',
  'points (stored only)',
  'points',
  'full discount',
  'spend & save (stored only)',
  'spend & save',
  'member price',
  'member pricing (stored only)',
  'member pricing',
  '积分维护',
  '满减折扣',
  '会员价格'
])

/** Visible coupon module URLs (after translate + filter). */
export const marketingStorefrontStorageOnlyUrls = [
  'coupon/spubounds',
  'coupon/skufullreduction',
  'coupon/memberprice',
  'coupon/bounds',
  'coupon/full',
  'coupon/memberprice'
]

/**
 * Seckill admin pages are now wired to the storefront (gulimall-coupon warm-up + RSemaphore kill),
 * so keep their CRUD menus visible: sessions, sku relations, promotion (activity).
 */
export const visibleSeckillMenuUrls = [
  'coupon/seckillsession',
  'coupon/seckillskurelation',
  'coupon/seckillpromotion'
]

/** Extra URL hides when DB uses short paths (Issue History / Subject / Seckill). */
export const hiddenMarketingMenuUrlPrefixes = [
  'coupon/coupon',
  'coupon/couponhistory',
  'coupon/history',
  'coupon/homesubject',
  'coupon/subject',
  'coupon/seckillpromotion',
  'coupon/seckill',
  'coupon/seckillsession',
  'coupon/seckillskurelation',
  'coupon/seckillskunotice',
  'coupon/homeadv',
  'coupon/couponspurelation',
  'coupon/couponspucategoryrelation',
  'coupon/skuladder'
]

export function isHiddenExternalAdminMenuUrl (url) {
  if (!url || typeof url !== 'string') return false
  const u = url.trim().toLowerCase()
  return u.startsWith('http://') || u.startsWith('https://')
}

function normalizeMenuUrl (url) {
  if (!url || typeof url !== 'string') return ''
  return url.replace(/^\//, '').toLowerCase()
}

function normalizeMenuName (name) {
  return (name || '').trim().toLowerCase()
}

function matchesAnyPrefix (url, prefixes) {
  const u = normalizeMenuUrl(url)
  if (!u) return false
  return prefixes.some((prefix) => u === prefix || u.startsWith(`${prefix}/`))
}

function isCouponModuleMenu (menu) {
  const u = normalizeMenuUrl(menu && menu.url)
  return u.startsWith('coupon/') || u.startsWith('modules/coupon/')
}

/**
 * Hide marketing entries unless explicitly whitelisted (name or storage URL).
 * Runs after menuTranslation — match English sidebar labels.
 */
export function shouldHideMarketingMenu (menu) {
  if (!menu) return false
  const name = normalizeMenuName(menu.name)
  const url = menu.url

  if (matchesAnyPrefix(url, visibleSeckillMenuUrls)) return false
  if (visibleMarketingMenuNames.has(name)) return false
  if (isMarketingStorefrontStorageOnlyUrl(url)) return false
  if (hiddenMarketingMenuNames.has(name)) return true

  if (isCouponModuleMenu(menu)) return true
  if (matchesAnyPrefix(url, hiddenMarketingMenuUrlPrefixes)) return true

  return false
}

export function isMarketingStorefrontStorageOnlyUrl (url) {
  return matchesAnyPrefix(url, marketingStorefrontStorageOnlyUrls)
}

function isOrderModuleMenu (menu) {
  const u = normalizeMenuUrl(menu && menu.url)
  return u.startsWith('order/') || u.startsWith('modules/order/')
}

/**
 * Hide order admin CRUD not wired to gulimall-mall; keep order list + payment records.
 */
export function shouldHideOrderMenu (menu) {
  if (!menu) return false
  const name = normalizeMenuName(menu.name)
  const url = menu.url

  if (hiddenOrderMenuNames.has(name)) return true
  if (matchesAnyPrefix(url, hiddenOrderMenuUrlPrefixes)) return true

  if (!isOrderModuleMenu(menu)) return false

  const u = normalizeMenuUrl(url)
  if (!u) {
    // Parent folder only (e.g. Order System) — keep; children are filtered separately.
    return false
  }

  return !visibleOrderMenuUrls.includes(u)
}

export function shouldHideMemberMenu (menu) {
  if (!menu) return false
  const name = normalizeMenuName(menu.name)
  if (hiddenMemberMenuNames.has(name)) return true
  return matchesAnyPrefix(menu.url, ['member/growth', 'member/statistics'])
}

export function shouldHideContentMenu (menu) {
  if (!menu) return false
  const name = normalizeMenuName(menu.name)
  if (hiddenContentMenuNames.has(name)) return true
  return matchesAnyPrefix(menu.url, hiddenContentMenuUrlPrefixes)
}

/** @param {{ url?: string, name?: string }} menu Menu row (pass full menu for marketing rules). */
export function isHiddenAdminMenuUrl (menu) {
  const url = typeof menu === 'string' ? menu : menu && menu.url
  const row = typeof menu === 'string' ? { url: menu } : menu

  if (isHiddenExternalAdminMenuUrl(url)) return true
  if (row && shouldHideMarketingMenu(row)) return true
  if (row && shouldHideOrderMenu(row)) return true
  if (row && shouldHideMemberMenu(row)) return true
  if (row && shouldHideContentMenu(row)) return true
  return matchesAnyPrefix(url, hiddenAdminMenuUrlPrefixes)
}

/**
 * Filter nested menu trees (sidebar nav uses `list`, role permissions use `children`).
 */
export function filterHiddenAdminMenuTree (menuList, childKey = 'list') {
  if (!menuList || !menuList.length) return []

  function filterNode (menu) {
    if (!menu || isHiddenAdminMenuUrl(menu)) return null
    const node = { ...menu }
    const kids = menu[childKey]
    if (Array.isArray(kids) && kids.length > 0) {
      node[childKey] = kids.map(filterNode).filter(Boolean)
      if (node[childKey].length === 0 && !(node.url && String(node.url).trim())) {
        return null
      }
    }
    return node
  }

  return menuList.map(filterNode).filter(Boolean)
}

/**
 * Filter flat `/sys/menu/list` rows before treeDataTranslate (role permissions, menu admin).
 */
export function filterHiddenAdminMenuRows (rows, idKey = 'menuId', pidKey = 'parentId') {
  if (!Array.isArray(rows) || rows.length === 0) return []

  const byId = new Map(rows.map(r => [r[idKey], r]))
  const hiddenIds = new Set()

  rows.forEach(row => {
    if (isHiddenAdminMenuUrl(row)) hiddenIds.add(row[idKey])
  })

  let changed = true
  while (changed) {
    changed = false
    for (const row of rows) {
      if (hiddenIds.has(row[idKey])) continue
      let pid = row[pidKey]
      while (pid != null && pid !== 0) {
        if (hiddenIds.has(pid)) {
          hiddenIds.add(row[idKey])
          changed = true
          break
        }
        const parent = byId.get(pid)
        pid = parent ? parent[pidKey] : null
      }
    }
  }

  const visibleIds = () => rows.filter(r => !hiddenIds.has(r[idKey])).map(r => r[idKey])

  const pruneEmptyFolders = () => {
    let pruned = false
    for (const row of rows) {
      if (hiddenIds.has(row[idKey])) continue
      const hasUrl = row.url && String(row.url).trim()
      if (hasUrl) continue
      const hasVisibleChild = rows.some(
        r => !hiddenIds.has(r[idKey]) && r[pidKey] === row[idKey]
      )
      if (!hasVisibleChild) {
        hiddenIds.add(row[idKey])
        pruned = true
      }
    }
    return pruned
  }

  while (pruneEmptyFolders()) { /* drop parents with no visible children */ }

  return rows.filter(r => !hiddenIds.has(r[idKey]))
}
