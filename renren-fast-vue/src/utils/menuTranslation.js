import { ADMIN_BRAND } from '@/constants/brand'

/**
 * Exact Chinese menu labels from sys_menu (RDS) → English sidebar text.
 */
export const menuTranslationMap = {
  // Brand / storefront wording (if present in DB)
  '电商系统': `${ADMIN_BRAND} Admin`,
  '电子商务': `${ADMIN_BRAND} Admin`,
  '商城系统': `${ADMIN_BRAND} Store`,
  '商城管理': 'Store Management',
  '谷粒商城': ADMIN_BRAND,
  '谷粒商城后台': `${ADMIN_BRAND} Admin`,

  // System Management
  '一级菜单': 'Top-Level Menu',
  '系统管理': 'System',
  '管理员列表': 'Admin Users',
  '角色管理': 'Roles',
  '菜单管理': 'Menus',
  'SQL监控': 'SQL Monitor',
  '定时任务': 'Scheduled Jobs',
  '参数管理': 'Settings',
  '系统日志': 'System Logs',
  '文件上传': 'File Upload',
  '日志列表': 'Job Logs',

  // Common actions (permission buttons shown as menus in tree)
  '查看': 'View',
  '新增': 'Add',
  '修改': 'Edit',
  '删除': 'Delete',
  '暂停': 'Pause',
  '恢复': 'Resume',
  '立即执行': 'Run Now',

  // System log operations (legacy Chinese in sys_log.operation)
  '保存角色': 'Save role',
  '修改角色': 'Update role',
  '删除角色': 'Delete role',
  '修改密码': 'Change password',
  '保存用户': 'Save user',
  '修改用户': 'Update user',
  '删除用户': 'Delete user',
  '保存菜单': 'Save menu',
  '修改菜单': 'Update menu',
  '删除菜单': 'Delete menu',
  '保存配置': 'Save config',
  '修改配置': 'Update config',
  '删除配置': 'Delete config',
  '保存定时任务': 'Save scheduled job',
  '修改定时任务': 'Update scheduled job',
  '删除定时任务': 'Delete scheduled job',
  '立即执行任务': 'Run scheduled job',
  '暂停定时任务': 'Pause scheduled job',
  '恢复定时任务': 'Resume scheduled job',

  // Product
  '商品系统': 'Catalog',
  '分类维护': 'Categories',
  '品牌管理': 'Brands',
  '平台属性': 'Attributes',
  '属性分组': 'Attribute Groups',
  '规格参数': 'Base Attributes',
  '销售属性': 'Sale Attributes',
  '商品维护': 'Products',
  'spu管理': 'SPU List',
  'SPU管理': 'SPU List',
  '发布商品': 'Publish Product',
  '商品管理': 'Product List',
  '规格维护': 'Specifications',

  // Marketing
  '优惠营销': 'Promotions',
  '优惠券管理': 'Coupons',
  '发放记录': 'Issue History',
  '专题活动': 'Subject',
  '秒杀活动': 'Seckill',
  '积分维护': 'Points (stored only)',
  '满减折扣': 'Spend & Save (stored only)',
  '会员价格': 'Member Pricing (stored only)',
  '每日秒杀': 'Daily Flash Sale',

  // Inventory
  '库存系统': 'Inventory',
  '仓库维护': 'Warehouses',
  '库存工作单': 'Stock Tasks',
  '商品库存': 'Stock Levels',
  '采购单维护': 'Purchasing',
  '采购需求': 'Purchase Requests',
  '采购单': 'Purchase Orders',

  // Orders
  '订单系统': 'Orders',
  '订单管理': 'Orders',
  '订单查询': 'Order Search',
  '退货单处理': 'Returns',
  '等级规则': 'Level Rules',
  '支付流水查询': 'Payments',
  '退款流水查询': 'Refunds',

  // Members
  '会员系统': 'Members',
  '会员管理': 'Members',
  '用户系统': 'Members',
  '会员列表': 'Member List',
  '会员等级': 'Member Levels',
  '积分变化': 'Points History',
  '统计信息': 'Statistics',

  // Content
  '内容管理': 'Content',
  '首页推荐': 'Home Featured',
  '分类热门': 'Hot Categories',
  '评论管理': 'Reviews'
}

/** Replace ecommerce-style phrases before exact map lookup. */
const phraseReplacements = [
  [/谷粒商城/g, ADMIN_BRAND],
  [/电子商务/g, ADMIN_BRAND],
  [/电商后台/g, `${ADMIN_BRAND} Admin`],
  [/电商系统/g, `${ADMIN_BRAND} Admin`],
  [/电商平台/g, `${ADMIN_BRAND} Store`],
  [/商城后台/g, `${ADMIN_BRAND} Admin`],
  [/商城系统/g, `${ADMIN_BRAND} Store`],
  [/商城管理/g, 'Store Management'],
  [/商城/g, 'Store']
]

function normalizeMenuLabel (name) {
  let s = String(name).trim()
  for (const [pattern, replacement] of phraseReplacements) {
    s = s.replace(pattern, replacement)
  }
  return s
}

/** System log @SysLog operation labels (Chinese legacy + English). */
export function translateSysLogOperation (operation) {
  return translateMenuName(operation)
}

/**
 * Translate menu name from Chinese to English (sidebar / tabs).
 */
export function translateMenuName (name) {
  if (!name) return name
  const trimmed = String(name).trim()

  const exact = menuTranslationMap[trimmed]
  if (exact) return exact

  const normalized = normalizeMenuLabel(trimmed)
  if (menuTranslationMap[normalized]) {
    return menuTranslationMap[normalized]
  }

  // Already English or post-replacement label
  if (!/[\u4e00-\u9fa5]/.test(normalized)) {
    return normalized
  }

  if (process.env.NODE_ENV !== 'production') {
    console.warn(`[Menu Translation] Untranslated menu name: "${trimmed}"`)
  }
  return normalized
}

export function translateMenuList (menuList) {
  if (!menuList || !Array.isArray(menuList)) {
    return menuList
  }

  return menuList.map(menu => {
    const translatedMenu = {
      ...menu,
      name: translateMenuName(menu.name)
    }

    if (menu.list && menu.list.length > 0) {
      translatedMenu.list = translateMenuList(menu.list)
    }

    return translatedMenu
  })
}
