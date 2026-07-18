/**
 * Homepage promo carousel — dedicated wide banners (not SKU main images).
 * Each slide links to a representative on-sale SKU detail page.
 */
export type PromoBannerSlide = {
  id: string;
  skuId: number;
  title: string;
  bannerSrc: string;
};

export const HOME_PROMO_BANNERS: PromoBannerSlide[] = [
  {
    id: 'iphone-15-pro',
    skuId: 4,
    title: 'Apple iPhone 15 Pro',
    bannerSrc: '/static/index/promo/promo-iphone-15-pro.png',
  },
  {
    id: 'xiaomi-14',
    skuId: 5,
    title: 'Xiaomi 14',
    bannerSrc: '/static/index/promo/promo-xiaomi-14.png',
  },
  {
    id: 'oppo-reno15',
    skuId: 14,
    title: 'OPPO Reno15',
    bannerSrc: '/static/index/promo/promo-oppo-reno15.png',
  },
  {
    id: 'galaxy-a36',
    skuId: 17,
    title: 'Samsung Galaxy A36 5G',
    bannerSrc: '/static/index/promo/promo-galaxy-a36.png',
  },
];
