/** @type {import('next').NextConfig} */
const publicApiBase = process.env.NEXT_PUBLIC_API_BASE || 'http://localhost:88';
// Server-side rewrites in cluster: use internal gateway DNS (see gulimall-mall Dockerfile / k8s).
const rewriteApiBase = process.env.INTERNAL_API_BASE || publicApiBase;

const nextConfig = {
  output: 'standalone',
  env: {
    NEXT_PUBLIC_API_BASE: publicApiBase,
  },
  // Wrong-path requests to /next/static/... rewrite to /_next/static/...
  // /search serves static search-page/index.html
  async redirects() {
    return [
      { source: '/cart/cartList.html', destination: '/cart/list', permanent: false },
      { source: '/cartList.html', destination: '/cart/list', permanent: false },
      { source: '/auth/login', destination: '/login', permanent: false },
      { source: '/auth/login/index.html', destination: '/login', permanent: false },
      { source: '/auth/register', destination: '/register', permanent: false },
      { source: '/auth/register/index.html', destination: '/register', permanent: false },
    ];
  },
  async rewrites() {
    return [
      // Keep SEO URL /{skuId}.html in the browser; render Next item page internally.
      { source: '/:skuId(\\d+).html', destination: '/item/:skuId' },
      // Cart, product, search, etc. — same origin in dev (prod often Nginx → gateway)
      { source: '/api/cart/:path*', destination: `${rewriteApiBase}/api/cart/:path*` },
      { source: '/api/order/:path*', destination: `${rewriteApiBase}/api/order/:path*` },
      { source: '/api/product/:path*', destination: `${rewriteApiBase}/api/product/:path*` },
      { source: '/api/ware/:path*', destination: `${rewriteApiBase}/api/ware/:path*` },
      { source: '/api/search/:path*', destination: `${rewriteApiBase}/api/search/:path*` },
      { source: '/api/seckill/:path*', destination: `${rewriteApiBase}/api/seckill/:path*` },
      { source: '/api/coupon/:path*', destination: `${rewriteApiBase}/api/coupon/:path*` },
      { source: '/api/member/:path*', destination: `${rewriteApiBase}/api/member/:path*` },
      { source: '/api/auth/:path*', destination: `${rewriteApiBase}/api/auth/:path*` },
      { source: '/next/:path*', destination: '/_next/:path*' },
    ];
  },
};

module.exports = nextConfig;
