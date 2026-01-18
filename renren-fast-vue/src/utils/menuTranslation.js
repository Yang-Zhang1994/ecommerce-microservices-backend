/**
 * Menu name translation mapping
 * Maps Chinese menu names to English
 */
export const menuTranslationMap = {
  // System Management
  '系统管理': 'System Management',
  '管理员列表': 'Admin List',
  '角色管理': 'Role Management',
  '菜单管理': 'Menu Management',
  'SQL监控': 'SQL Monitor',
  '定时任务': 'Scheduled Tasks',
  '参数管理': 'Parameter Management',
  '系统日志': 'System Log',
  '文件上传': 'File Upload',

  // Product System
  '商品系统': 'Product System',
  '分类维护': 'Category Maintenance',
  '品牌管理': 'Brand Management',
  '平台属性': 'Platform Attributes',
  '属性分组': 'Attribute Group',
  '规格参数': 'Base Attributes',
  '销售属性': 'Sale Attributes',
  '商品维护': 'Product Maintenance',
  'spu管理': 'SPU Management',
  '发布商品': 'Publish Product',
  '商品管理': 'Product Management',

  // Promotional Marketing
  '优惠营销': 'Promotional Marketing',
  '优惠券管理': 'Coupon Management',
  '发放记录': 'Issuance Record',
  '专题活动': 'Special Activities',
  '秒杀活动': 'Flash Sale Activities',
  '积分维护': 'Points Maintenance',
  '满减折扣': 'Full Reduction Discount',
  '会员价格': 'Member Price',
  '每日秒杀': 'Daily Flash Sale',

  // Inventory System
  '库存系统': 'Inventory System',
  '仓库维护': 'Warehouse Maintenance',
  '库存工作单': 'Inventory Work Order',
  '商品库存': 'Product Inventory',
  '采购单维护': 'Purchase Order Maintenance',
  '采购需求': 'Purchase Requirements',
  '采购单': 'Purchase Order',

  // Order Management
  '订单系统': 'Order System',
  '订单管理': 'Order Management',
  '订单查询': 'Order Inquiry',
  '退货单处理': 'Return Order Processing',
  '等级规则': 'Level Rules',
  '支付流水查询': 'Payment Flow Inquiry',
  '退款流水查询': 'Refund Flow Inquiry',

  // Member Management
  '会员系统': 'Member System',
  '会员管理': 'Member Management',
  '用户系统': 'User System',
  '会员列表': 'Member List',
  '会员等级': 'Member Level',
  '积分变化': 'Points Change',
  '统计信息': 'Statistics',

  // Content Management
  '内容管理': 'Content Management',
  '首页推荐': 'Homepage Recommendation',
  '分类热门': 'Popular Categories',
  '评论管理': 'Comment Management',

  // Other
  '规格维护': 'Specification Maintenance'
}

/**
 * Translate menu name from Chinese to English
 * @param {string} name - Chinese menu name
 * @returns {string} - English menu name
 */
export function translateMenuName (name) {
  if (!name) return name
  const translated = menuTranslationMap[name]
  if (!translated && /[\u4e00-\u9fa5]/.test(name)) {
    // Log untranslated Chinese menu names for debugging
    console.warn(`[Menu Translation] Untranslated menu name: "${name}"`)
  }
  return translated || name
}

/**
 * Translate menu list recursively
 * @param {Array} menuList - Menu list from backend
 * @returns {Array} - Translated menu list
 */
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
