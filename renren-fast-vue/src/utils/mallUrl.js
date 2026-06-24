/**
 * Mall storefront origin for admin "Preview" links.
 * Set at build time: MALL_PUBLIC_ORIGIN=https://www.example.com
 */
export function getMallPublicOrigin() {
  const fromEnv = process.env.MALL_PUBLIC_ORIGIN;
  if (fromEnv && String(fromEnv).trim()) {
    return String(fromEnv).trim().replace(/\/$/, "");
  }
  if (typeof window !== "undefined" && window.GULIMALL_MALL_PUBLIC_ORIGIN) {
    return String(window.GULIMALL_MALL_PUBLIC_ORIGIN).trim().replace(/\/$/, "");
  }
  return "http://localhost:3001";
}

export function mallItemDetailUrl(skuId) {
  const id = encodeURIComponent(String(skuId));
  return `${getMallPublicOrigin()}/item/${id}`;
}
