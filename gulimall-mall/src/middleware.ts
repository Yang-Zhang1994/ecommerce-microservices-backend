import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

/**
 * item 子域根路径 / 无 SKU，重定向到默认商品详情（与搜索页 item.ecommerce.com/{skuId}.html 一致）。
 * 默认 SKU 可通过环境变量 NEXT_PUBLIC_ITEM_ROOT_SKU_ID 配置（本地开发未配 item 域名时仍访问首页）。
 */
export function middleware(request: NextRequest) {
  const host = request.headers.get('host') || '';
  if (request.nextUrl.pathname !== '/') {
    return NextResponse.next();
  }
  if (!host.startsWith('item.')) {
    return NextResponse.next();
  }
  const defaultSku =
    process.env.NEXT_PUBLIC_ITEM_ROOT_SKU_ID || process.env.ITEM_ROOT_SKU_ID || '1';
  const url = request.nextUrl.clone();
  url.pathname = `/${defaultSku}.html`;
  return NextResponse.redirect(url);
}

export const config = {
  matcher: ['/'],
};
