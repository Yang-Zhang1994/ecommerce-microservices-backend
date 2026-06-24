/** CategoryEntity model matching gulimall-product service */
export interface CategoryEntity {
  catId: number;
  name: string;
  parentCid: number;
  catLevel: number;
  showStatus?: number;
  sort?: number;
  icon?: string;
  productUnit?: string;
  productCount?: number;
  children?: CategoryEntity[];
  catelogPath?: number[];
}

/** Search result ES SKU model (aligned with backend SkuEsModel, trimmed for frontend) */
export interface SkuEsModel {
  skuId: number;
  spuId: number;
  skuTitle: string;
  skuPrice: number;
  skuImg: string;
  saleCount?: number;
  hasStock?: boolean;
  hotScore?: number;
  brandId?: number;
  catalogId?: number;
  brandName?: string;
  brandImg?: string;
  catalogName?: string;
}

/** Brand in search filter */
export interface SearchBrandVo {
  brandId: number;
  brandName: string;
  brandImg: string;
}

/** Catalog in search filter */
export interface SearchCatalogVo {
  catalogId: number;
  catalogName: string;
}

/** Attr in search filter */
export interface SearchAttrVo {
  attrId: number;
  attrName: string;
  attrValue: string[];
}

/** Breadcrumb nav item */
export interface SearchNavVo {
  navName: string;
  navValue: string;
  link: string;
}

/** Full search result from /api/search/product/list */
export interface SearchResultData {
  list: SkuEsModel[];
  total: number;
  pageNum: number;
  totalPages: number;
  brands?: SearchBrandVo[];
  catalogs?: SearchCatalogVo[];
  attrs?: SearchAttrVo[];
  pageNavs?: number[];
  navs?: SearchNavVo[];
}

/** Minimal SKU info embedded in a seckill row (from gulimall-coupon warm-up). */
export interface SeckillSkuInfo {
  skuId?: number;
  spuId?: number;
  skuName?: string;
  skuTitle?: string;
  skuSubtitle?: string;
  skuDefaultImg?: string;
  price?: number;
}

/** One seckill SKU (from /api/seckill/currentSeckillSkus). */
export interface SeckillSku {
  id?: number;
  promotionId?: number;
  promotionSessionId?: number;
  skuId?: number;
  seckillPrice?: number;
  seckillCount?: number;
  seckillLimit?: number;
  seckillSort?: number;
  skuInfo?: SeckillSkuInfo;
  startTime?: number;
  endTime?: number;
  /** Buy token; only present while the session is live. */
  randomCode?: string;
}

/** pms_sku_info detail (product microservice /product/skuinfo/info/{id}) */
export interface SkuInfoDetail {
  skuId: number;
  spuId?: number;
  skuName?: string;
  skuTitle?: string;
  skuSubtitle?: string;
  skuDesc?: string;
  skuDefaultImg?: string;
  price?: number;
  saleCount?: number;
  brandId?: number;
  catalogId?: number;
}

export interface SkuImageItem {
  id?: number;
  skuId?: number;
  imgUrl?: string;
  defaultImg?: number;
}

export interface SpuImageItem {
  id?: number;
  spuId?: number;
  imgUrl?: string;
  imgSort?: number;
  defaultImg?: number;
}

export interface AttrValueWithSkuIds {
  attrValue?: string;
  skuIds?: number[];
}

export interface SkuItemSaleAttr {
  attrId: number;
  attrName?: string;
  /** Each option lists which SKUs use this value (for switching SKU on the detail page). */
  attrValues?: AttrValueWithSkuIds[];
}

export interface SpuItemAttr {
  attrId: number;
  attrName?: string;
  attrValue?: string;
}

export interface SpuItemAttrGroup {
  groupId: number;
  groupName?: string;
  attrs?: SpuItemAttr[];
}

/** Aggregated product detail payload from /api/product/item/{skuId} */
export interface SkuItemDetail {
  info?: SkuInfoDetail;
  /** SKU spec images — main gallery */
  images?: SkuImageItem[];
  /** SPU shared promo gallery — below main gallery */
  spuImages?: SpuImageItem[];
  saleAttr?: SkuItemSaleAttr[];
  desc?: {
    spuId?: number;
    decript?: string;
  };
  groupAttrs?: SpuItemAttrGroup[];
}

/** Shopping cart line (gulimall-cart Redis JSON) */
export interface CartItem {
  skuId?: number;
  title?: string;
  image?: string;
  skuAttr?: string[];
  price?: number | string;
  count?: number;
  check?: boolean;
  totalPrice?: number | string;
}

/** Cart aggregate from GET /api/cart/current */
export interface Cart {
  items?: CartItem[];
  countNum?: number;
  countType?: number;
  totalAmount?: number | string;
  reduce?: number | string;
}

export interface OrderConfirmAddress {
  id: number;
  name: string;
  phone: string;
  postCode?: string;
  province?: string;
  city?: string;
  region?: string;
  detailAddress?: string;
  defaultStatus?: number;
}

export interface OrderConfirmItem {
  skuId?: number;
  title?: string;
  image?: string;
  skuAttr?: string[];
  count?: number;
  price?: number | string;
  totalPrice?: number | string;
  /** From ware service: available stock = sum(stock - stock_locked) > 0 */
  hasStock?: boolean;
}

export interface OrderConfirmData {
  addresses?: OrderConfirmAddress[];
  items?: OrderConfirmItem[];
  integration?: number;
  integrationAmount?: number | string;
  totalAmount?: number | string;
  freightAmount?: number | string;
  payPrice?: number | string;
  orderToken?: string;
}

export interface OrderSubmitPayload {
  orderToken: string;
  addressId?: number;
  note?: string;
  payType?: number;
}

export interface OrderSubmitResult {
  code: number;
  msg?: string;
  orderSn?: string;
  payAmount?: number | string;
}

export interface StripeCheckoutSessionData {
  sessionId: string;
  checkoutUrl: string;
}

export interface OrderListOrder {
  id?: number;
  orderSn?: string;
  createTime?: string;
  totalAmount?: number | string;
  payAmount?: number | string;
  status?: number;
  memberId?: number;
  memberUsername?: string;
  firstItemTitle?: string;
  itemCount?: number;
  firstItemPic?: string;
}

export interface OrderDetailItem {
  skuId?: number;
  skuName?: string;
  skuPic?: string;
  skuPrice?: number | string;
  skuQuantity?: number;
  skuAttrs?: string[];
  realAmount?: number | string;
  spuName?: string;
}

export interface OrderPaymentSummary {
  paid?: boolean;
  payMethodLabel?: string;
  paymentStatus?: string;
  paymentTime?: string;
  paidAmount?: number | string;
  tradeNo?: string;
}

export interface OrderDetail {
  id?: number;
  orderSn?: string;
  status?: number;
  statusText?: string;
  createTime?: string;
  paymentTime?: string;
  deliveryTime?: string;
  receiveTime?: string;
  totalAmount?: number | string;
  payAmount?: number | string;
  freightAmount?: number | string;
  promotionAmount?: number | string;
  couponAmount?: number | string;
  integrationAmount?: number | string;
  note?: string;
  deliveryCompany?: string;
  deliverySn?: string;
  receiverName?: string;
  receiverPhone?: string;
  receiverPostCode?: string;
  receiverProvince?: string;
  receiverCity?: string;
  receiverRegion?: string;
  receiverDetailAddress?: string;
  items?: OrderDetailItem[];
  payment?: OrderPaymentSummary;
}

export interface SeckillOrderConfirmData {
  orderSn?: string;
  payAmount?: number | string;
  freightAmount?: number | string;
  createTime?: string;
  needsAddress?: boolean;
  flashPayTimeoutMinutes?: number;
  addresses?: OrderConfirmAddress[];
  items?: OrderDetailItem[];
  receiverName?: string;
  receiverPhone?: string;
  receiverProvince?: string;
  receiverCity?: string;
  receiverRegion?: string;
  receiverDetailAddress?: string;
}

/** Standard R wrapper returned by gateway */
export interface R<T = unknown> {
  code: number;
  msg: string;
  data?: T;
  total?: number;
  [key: string]: unknown;
}
