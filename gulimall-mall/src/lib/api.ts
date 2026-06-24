import type {
  R,
  Cart,
  CategoryEntity,
  SearchResultData,
  SeckillSku,
  SkuEsModel,
  SkuInfoDetail,
  SkuItemDetail,
  OrderConfirmData,
  SeckillOrderConfirmData,
  OrderDetail,
  OrderListOrder,
  OrderSubmitPayload,
  OrderSubmitResult,
  StripeCheckoutSessionData,
} from '@/types/api';

const API_BASE = process.env.NEXT_PUBLIC_API_BASE || 'http://localhost:88';

/** Resolve API base: use request host in SSR; in browser prefer same-origin when API_BASE points to localhost on a real host. */
async function getApiBase(): Promise<string> {
  if (typeof window !== 'undefined') {
    const host = window.location.hostname;
    const isLocalHost = host === 'localhost' || host === '127.0.0.1';
    const apiBase = (API_BASE || '').trim();
    const apiBaseIsLocal =
      apiBase.includes('localhost') || apiBase.includes('127.0.0.1');
    // When page is on ecommerce.com but NEXT_PUBLIC_API_BASE is left as localhost,
    // relative /api keeps Host=ecommerce.com so SESSION cookie is sent.
    if (!isLocalHost && apiBaseIsLocal) return '';
    // Mall on localhost + gateway on localhost:88 — same-origin /api/** (see next.config rewrites)
    // keeps SESSION aligned with the page host and avoids cross-origin cookie issues after redirects.
    if (isLocalHost && apiBaseIsLocal) return '';
    return API_BASE;
  }
  try {
    const { headers } = await import('next/headers');
    const h = headers();
    const host = h.get('host') || '';
    const hostname = host.split(':')[0].toLowerCase();
    const proto = h.get('x-forwarded-proto') || 'http';
    const apiBase = (API_BASE || '').trim();
    const apiBaseIsLocal =
      apiBase.includes('localhost') || apiBase.includes('127.0.0.1');
    const isEcommerceDevHost =
      hostname === 'www.ecommerce.com' || hostname === 'ecommerce.com';

    // Dev: hosts → 127.0.0.1:3001 with Host www.ecommerce.com (no :80 nginx) — SSR must hit gateway :88
    if (isEcommerceDevHost && apiBaseIsLocal) {
      return apiBase;
    }

    // SSR on localhost:3001 — hit gateway directly; Next rewrites do not apply to RSC fetch().
    if ((hostname === 'localhost' || hostname === '127.0.0.1') && apiBaseIsLocal) {
      return apiBase;
    }

    if (host) return `${proto}://${host}`;
  } catch {
    /* ignore */
  }
  return API_BASE;
}

/** Ware service: batch stock lookup for cart / item pages. */
export async function getSkuHasStockMapServer(
  skuIds: number[],
): Promise<Map<number, boolean>> {
  const ids = Array.from(
    new Set(skuIds.filter((id) => Number.isFinite(id) && id > 0)),
  );
  const map = new Map<number, boolean>();
  if (!ids.length) {
    return map;
  }
  const base = await getApiBase();
  const url = `${base}/api/ware/waresku/hasStock`;
  try {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(ids),
      cache: 'no-store',
    });
    let body: Partial<R<Array<{ skuId?: number; hasStock?: boolean }>>> = {};
    try {
      body = (await res.json()) as typeof body;
    } catch {
      return map;
    }
    if (!res.ok || Number(body.code) !== 0) {
      return map;
    }
    const rows = Array.isArray(body.data) ? body.data : [];
    for (const row of rows) {
      const id = Number(row.skuId);
      if (!Number.isFinite(id) || id <= 0 || row.hasStock == null) {
        continue;
      }
      map.set(id, Boolean(row.hasStock));
    }
    return map;
  } catch {
    return map;
  }
}

/** Ware service: whether SKU has available stock (sum(stock - locked) > 0). */
export async function getSkuHasStockServer(skuId: number): Promise<boolean | null> {
  const map = await getSkuHasStockMapServer([skuId]);
  if (!map.has(skuId)) {
    return null;
  }
  return map.get(skuId) ?? null;
}

/**
 * Current user's cart (SSR): forwards browser Cookie so Redis cart key matches cart interceptor.
 */
export async function getCurrentCartServer(): Promise<{ ok: boolean; cart?: Cart; httpStatus: number }> {
  const base = await getApiBase();
  let cookie = '';
  try {
    const { headers } = await import('next/headers');
    cookie = headers().get('cookie') || '';
  } catch {
    /* ignore */
  }
  const url = `${base}/api/cart/current`;
  const res = await fetch(url, {
    headers: cookie ? { Cookie: cookie } : {},
    cache: 'no-store',
  });
  const httpStatus = res.status;
  let body: unknown = null;
  try {
    body = await res.json();
  } catch {
    return { ok: false, httpStatus };
  }
  const r = body as R<Cart>;
  if (!res.ok || !r || Number(r.code) !== 0) {
    return { ok: false, httpStatus };
  }
  return { ok: true, cart: r.data ?? { items: [] }, httpStatus };
}

/** Browser cart fetch (always uses same-origin /api + cookies). */
export async function getCurrentCartClient(): Promise<Cart> {
  const res = await fetch('/api/cart/current', {
    credentials: 'include',
    cache: 'no-store',
  });
  let body: Partial<R<Cart>> = {};
  try {
    body = (await res.json()) as Partial<R<Cart>>;
  } catch {
    return { items: [] };
  }
  if (!res.ok || Number(body.code) !== 0) {
    return { items: [] };
  }
  return body.data ?? { items: [] };
}

/** Browser batch stock lookup for cart lines. */
export async function getSkuHasStockMapClient(skuIds: number[]): Promise<Map<number, boolean>> {
  const ids = Array.from(new Set(skuIds.filter((id) => Number.isFinite(id) && id > 0)));
  const map = new Map<number, boolean>();
  if (!ids.length) {
    return map;
  }
  try {
    const res = await fetch('/api/ware/waresku/hasStock', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(ids),
      cache: 'no-store',
    });
    const body = (await res.json()) as Partial<R<Array<{ skuId?: number; hasStock?: boolean }>>>;
    if (!res.ok || Number(body.code) !== 0) {
      return map;
    }
    const rows = Array.isArray(body.data) ? body.data : [];
    for (const row of rows) {
      const id = Number(row.skuId);
      if (!Number.isFinite(id) || id <= 0 || row.hasStock == null) {
        continue;
      }
      map.set(id, Boolean(row.hasStock));
    }
  } catch {
    /* ignore */
  }
  return map;
}

export async function getOrderConfirmServer(): Promise<{
  ok: boolean;
  data?: OrderConfirmData;
  httpStatus: number;
  unauthorized?: boolean;
}> {
  const base = await getApiBase();
  let cookie = '';
  try {
    const { headers } = await import('next/headers');
    cookie = headers().get('cookie') || '';
  } catch {
    /* ignore */
  }

  const url = `${base}/api/order/confirm`;
  const res = await fetch(url, {
    headers: cookie ? { Cookie: cookie } : {},
    cache: 'no-store',
  });
  const httpStatus = res.status;
  let body: unknown = null;
  try {
    body = await res.json();
  } catch {
    return { ok: false, httpStatus };
  }
  const r = body as R<OrderConfirmData>;
  if (!res.ok || !r || Number(r.code) !== 0) {
    return { ok: false, httpStatus, unauthorized: httpStatus === 401 };
  }
  return { ok: true, data: r.data, httpStatus };
}

export async function getSeckillOrderConfirmServer(orderSn: string): Promise<{
  ok: boolean;
  data?: SeckillOrderConfirmData;
  httpStatus: number;
  unauthorized?: boolean;
  msg?: string;
}> {
  const sn = orderSn.trim();
  if (!sn) {
    return { ok: false, httpStatus: 400, msg: 'orderSn required' };
  }
  const base = await getApiBase();
  let cookie = '';
  try {
    const { headers } = await import('next/headers');
    cookie = headers().get('cookie') || '';
  } catch {
    /* ignore */
  }
  const url = `${base}/api/order/seckill/confirm?orderSn=${encodeURIComponent(sn)}`;
  const res = await fetch(url, {
    headers: cookie ? { Cookie: cookie } : {},
    cache: 'no-store',
  });
  const httpStatus = res.status;
  let body: unknown = null;
  try {
    body = await res.json();
  } catch {
    return { ok: false, httpStatus };
  }
  const r = body as R<SeckillOrderConfirmData>;
  if (!res.ok || !r || Number(r.code) !== 0) {
    return {
      ok: false,
      httpStatus,
      unauthorized: httpStatus === 401 || Number(r?.code) === 401,
      msg: r?.msg,
    };
  }
  return { ok: true, data: r.data, httpStatus };
}

/** Browser: load flash-sale confirm payload (uses same-origin /api + SESSION cookie). */
export async function fetchSeckillOrderConfirm(orderSn: string): Promise<{
  ok: boolean;
  data?: SeckillOrderConfirmData;
  unauthorized?: boolean;
  msg?: string;
}> {
  const sn = orderSn.trim();
  if (!sn) {
    return { ok: false, msg: 'orderSn required' };
  }
  try {
    const res = await fetch(
      `/api/order/seckill/confirm?orderSn=${encodeURIComponent(sn)}`,
      { credentials: 'include', cache: 'no-store' },
    );
    const body = (await res.json()) as R<SeckillOrderConfirmData>;
    if (res.status === 401 || Number(body.code) === 401) {
      return { ok: false, unauthorized: true, msg: body.msg };
    }
    if (!res.ok || Number(body.code) !== 0 || !body.data) {
      return { ok: false, msg: body.msg || `HTTP ${res.status}` };
    }
    return { ok: true, data: body.data };
  } catch (e) {
    return { ok: false, msg: e instanceof Error ? e.message : 'network error' };
  }
}

/** MQ creates the order asynchronously after /seckill/kill; poll until it appears. */
export async function waitForSeckillOrderConfirm(
  orderSn: string,
  opts?: { maxAttempts?: number; intervalMs?: number },
): Promise<Awaited<ReturnType<typeof fetchSeckillOrderConfirm>>> {
  const maxAttempts = opts?.maxAttempts ?? 25;
  const intervalMs = opts?.intervalMs ?? 400;
  for (let i = 0; i < maxAttempts; i++) {
    const result = await fetchSeckillOrderConfirm(orderSn);
    if (result.ok || result.unauthorized) {
      return result;
    }
    const msg = (result.msg || '').toLowerCase();
    const pending = msg.includes('order not found');
    if (!pending) {
      return result;
    }
    if (i + 1 < maxAttempts) {
      await new Promise((r) => setTimeout(r, intervalMs));
    }
  }
  return fetchSeckillOrderConfirm(orderSn);
}

export async function bindSeckillOrderAddress(payload: {
  orderSn: string;
  addressId: number;
  note?: string;
}): Promise<{ code: number; msg?: string }> {
  const r = await fetchApi<unknown>('/order/seckill/confirm', {
    method: 'POST',
    body: JSON.stringify(payload),
    credentials: 'include',
  });
  return { code: Number(r.code ?? -1), msg: r.msg };
}

/** Member cancels an unpaid order (releases stock; seckill quota restored via MQ). */
export async function cancelOrder(orderSn: string): Promise<{ code: number; msg?: string }> {
  const r = await fetchApi<unknown>('/order/cancel', {
    method: 'POST',
    body: JSON.stringify({ orderSn: orderSn.trim() }),
    credentials: 'include',
  });
  return { code: Number(r.code ?? -1), msg: r.msg };
}

export async function getMyOrdersServer(status = 'all'): Promise<{
  ok: boolean;
  data: OrderListOrder[];
  total: number;
  httpStatus: number;
  unauthorized?: boolean;
}> {
  const base = await getApiBase();
  let cookie = '';
  try {
    const { headers } = await import('next/headers');
    cookie = headers().get('cookie') || '';
  } catch {
    /* ignore */
  }
  const q = new URLSearchParams();
  if (status && status !== 'all') q.set('status', status);
  q.set('page', '1');
  q.set('limit', '20');
  const url = `${base}/api/order/list?${q.toString()}`;
  const res = await fetch(url, {
    headers: cookie ? { Cookie: cookie } : {},
    cache: 'no-store',
  });
  const httpStatus = res.status;
  let body: unknown = null;
  try {
    body = await res.json();
  } catch {
    return { ok: false, data: [], total: 0, httpStatus };
  }
  const r = body as R<OrderListOrder[]>;
  if (!res.ok || !r || Number(r.code) !== 0) {
    return { ok: false, data: [], total: 0, httpStatus, unauthorized: httpStatus === 401 };
  }
  const rows = Array.isArray(r.data) ? r.data : [];
  const total = Number((r as Record<string, unknown>).total ?? rows.length) || rows.length;
  return { ok: true, data: rows, total, httpStatus };
}

export async function getOrderDetailServer(orderSn: string): Promise<{
  ok: boolean;
  data?: OrderDetail;
  httpStatus: number;
  unauthorized?: boolean;
  notFound?: boolean;
}> {
  const base = await getApiBase();
  let cookie = '';
  try {
    const { headers } = await import('next/headers');
    cookie = headers().get('cookie') || '';
  } catch {
    /* ignore */
  }
  const sn = (orderSn || '').trim();
  if (!sn) {
    return { ok: false, httpStatus: 400, notFound: true };
  }
  const url = `${base}/api/order/detail/${encodeURIComponent(sn)}`;
  const res = await fetch(url, {
    headers: cookie ? { Cookie: cookie } : {},
    cache: 'no-store',
  });
  const httpStatus = res.status;
  let body: unknown = null;
  try {
    body = await res.json();
  } catch {
    return { ok: false, httpStatus };
  }
  const r = body as R<OrderDetail>;
  if (httpStatus === 401 || Number(r?.code) === 401) {
    return { ok: false, httpStatus, unauthorized: true };
  }
  if (httpStatus === 404 || Number(r?.code) === 404) {
    return { ok: false, httpStatus, notFound: true };
  }
  if (!res.ok || !r || Number(r.code) !== 0) {
    return { ok: false, httpStatus };
  }
  return { ok: true, data: r.data, httpStatus };
}

export async function submitOrder(payload: OrderSubmitPayload): Promise<OrderSubmitResult> {
  try {
    const r = await fetchApi<OrderSubmitResult>('/order/submit', {
      method: 'POST',
      body: JSON.stringify(payload),
      credentials: 'include',
    });
    return {
      code: Number(r.code),
      msg: r.msg,
      ...(r.data ?? {}),
    };
  } catch (e) {
    const msg = e instanceof Error ? e.message : 'Submit failed';
    if (msg.includes('401') || msg.toLowerCase().includes('login')) {
      return { code: 401, msg: 'Please login first' };
    }
    throw e;
  }
}

export type MemberAddressPayload = {
  id?: number;
  memberId: number;
  name: string;
  phone: string;
  postCode?: string;
  province?: string;
  city?: string;
  region?: string;
  detailAddress?: string;
  defaultStatus?: number;
};

export async function saveMemberAddress(payload: MemberAddressPayload): Promise<void> {
  const path = payload.id
    ? '/member/memberreceiveaddress/update'
    : '/member/memberreceiveaddress/save';
  const r = await fetchApi<unknown>(path, {
    method: 'POST',
    body: JSON.stringify(payload),
    credentials: 'include',
  });
  if (Number(r.code) !== 0) {
    throw new Error(r.msg || 'Could not save address');
  }
}

export async function createStripeCheckoutSession(orderSn: string): Promise<StripeCheckoutSessionData> {
  const r = await fetchApi<StripeCheckoutSessionData>('/order/pay/stripe/checkout-session', {
    method: 'POST',
    body: JSON.stringify({ orderSn }),
    credentials: 'include',
  });
  const data = r.data;
  if (!data?.checkoutUrl || !data?.sessionId) {
    throw new Error('Failed to create stripe checkout session');
  }
  return data;
}

/** Call backend /api endpoints via gateway */
export async function fetchApi<T = unknown>(path: string, init?: RequestInit): Promise<R<T>> {
  const base = await getApiBase();
  const url = path.startsWith('http') ? path : `${base}/api${path.startsWith('/') ? path : `/${path}`}`;
  let res: Response;
  try {
    res = await fetch(url, {
      ...init,
      credentials: init?.credentials ?? 'include',
      headers: { 'Content-Type': 'application/json', ...init?.headers },
      cache: 'no-store',
    });
  } catch (e) {
    const msg = e instanceof Error ? e.message : 'network error';
    throw new Error(
      `Cannot reach API at ${url}. Is the gateway running (default :88)? ${msg}`
    );
  }

  const text = await res.text();
  let data: R<T>;
  try {
    data = text ? (JSON.parse(text) as R<T>) : ({} as R<T>);
  } catch {
    const snippet = text.replace(/\s+/g, ' ').trim().slice(0, 160);
    throw new Error(
      res.ok
        ? 'API returned invalid JSON'
        : `API error HTTP ${res.status}${snippet ? `: ${snippet}` : ''}`
    );
  }

  if (res.ok) return data;
  throw new Error(data.msg || `HTTP ${res.status}`);
}

/** Single SKU detail for item detail page */
export async function getSkuInfo(skuId: string): Promise<SkuInfoDetail | null> {
  try {
    const r = await fetchApi<unknown>(`/product/skuinfo/info/${encodeURIComponent(skuId)}`);
    const skuInfo = (r as Record<string, unknown>).skuInfo as SkuInfoDetail | undefined;
    return skuInfo ?? null;
  } catch {
    return null;
  }
}

/** Aggregated SKU detail for item page */
export async function getSkuItem(skuId: string): Promise<SkuItemDetail | null> {
  try {
    const r = await fetchApi<unknown>(`/product/item/${encodeURIComponent(skuId)}`);
    const item = (r as Record<string, unknown>).item as SkuItemDetail | undefined;
    return item ?? null;
  } catch {
    return null;
  }
}

/** Current live seckill SKUs for the homepage flash-sale block (seckill service). Soft-fails to []. */
export async function getCurrentSeckillSkus(): Promise<SeckillSku[]> {
  try {
    const r = await fetchApi<SeckillSku[]>('/seckill/currentSeckillSkus');
    const data = r.data;
    return Array.isArray(data) ? data : [];
  } catch {
    return [];
  }
}

/** Upcoming seckill SKUs within the warm-up window (not live yet). Soft-fails to []. */
export async function getUpcomingSeckillSkus(): Promise<SeckillSku[]> {
  try {
    const r = await fetchApi<SeckillSku[]>('/seckill/upcomingSeckillSkus');
    const data = r.data;
    return Array.isArray(data) ? data : [];
  } catch {
    return [];
  }
}

/** Seckill info for one SKU (live or nearest upcoming within 3 days). Soft-fails to null. */
export async function getSeckillSkuInfo(skuId: number): Promise<SeckillSku | null> {
  try {
    const r = await fetchApi<SeckillSku>(`/seckill/sku/${skuId}`);
    return r.data ?? null;
  } catch {
    return null;
  }
}

export interface SeckillKillResult {
  code: number;
  msg?: string;
  orderSn?: string;
}

/** Grab a seckill item (browser). killId = "{sessionId}_{skuId}", key = randomCode. */
export async function seckillKill(
  killId: string,
  key: string,
  num = 1,
): Promise<SeckillKillResult> {
  const params = new URLSearchParams({ killId, key, num: String(num) });
  try {
    const res = await fetch(`/api/seckill/kill?${params.toString()}`, {
      method: 'POST',
      credentials: 'include',
      cache: 'no-store',
    });
    let body: Partial<R<string>> = {};
    try {
      body = (await res.json()) as Partial<R<string>>;
    } catch {
      return { code: -1, msg: `HTTP ${res.status}` };
    }
    const orderSn =
      (typeof body.data === 'string' ? body.data : undefined) ??
      (typeof body.orderSn === 'string' ? body.orderSn : undefined);
    return { code: Number(body.code ?? -1), msg: body.msg, orderSn };
  } catch (e) {
    return { code: -1, msg: e instanceof Error ? e.message : 'network error' };
  }
}

/** Product category tree (product microservice) */
export async function getCategoryTree(): Promise<CategoryEntity[]> {
  const r = await fetchApi<CategoryEntity[]>('/product/category/list/tree');
  const data = r.data;
  return Array.isArray(data) ? data : [];
}

export interface SearchParams {
  keyword?: string;
  catalog3Id?: number | number[];
  pageNum?: number;
  pageSize?: number;
  sort?: string;
  hasStock?: number;
  skuPrice?: string;
  brandId?: number[];
  attrs?: string[];
}

/** Backend SearchResult (data.products) mapped to SearchResultData (list) */
interface SearchResultResponse {
  products?: SkuEsModel[];
  pageNum?: number;
  total?: number;
  totalPages?: number;
  degraded?: boolean;
  brands?: SearchResultData['brands'];
  catalogs?: SearchResultData['catalogs'];
  attrs?: SearchResultData['attrs'];
  pageNavs?: number[];
  navs?: SearchResultData['navs'];
}

const EMPTY_SEARCH_RESULT: SearchResultData = {
  list: [],
  total: 0,
  pageNum: 1,
  totalPages: 0,
  brands: [],
  catalogs: [],
  attrs: [],
  pageNavs: [1],
  navs: [],
};

function mapSearchResponse(d: SearchResultResponse | undefined): SearchResultData {
  const list = Array.isArray(d?.products) ? d.products : [];
  return {
    list,
    total: typeof d?.total === 'number' ? d.total : 0,
    pageNum: typeof d?.pageNum === 'number' ? d.pageNum : 1,
    totalPages: typeof d?.totalPages === 'number' ? d.totalPages : 0,
    brands: d?.brands ?? [],
    catalogs: d?.catalogs ?? [],
    attrs: d?.attrs ?? [],
    pageNavs: Array.isArray(d?.pageNavs) ? d.pageNavs : [1],
    navs: d?.navs ?? [],
  };
}

/** Search product list (search microservice); returns full result with filters and pagination */
export async function searchProducts(
  params: SearchParams,
  options?: { softFail?: boolean }
): Promise<SearchResultData> {
  const search = new URLSearchParams();
  if (params.keyword) search.set('keyword', params.keyword);
  if (params.catalog3Id != null) {
    const ids = Array.isArray(params.catalog3Id) ? params.catalog3Id : [params.catalog3Id];
    ids.forEach((id) => search.append('catalog3Id', String(id)));
  }
  if (params.pageNum != null) search.set('pageNum', String(params.pageNum));
  if (params.pageSize != null) search.set('pageSize', String(params.pageSize));
  if (params.sort) search.set('sort', params.sort);
  if (params.hasStock != null) search.set('hasStock', String(params.hasStock));
  if (params.skuPrice) search.set('skuPrice', params.skuPrice);
  if (params.brandId?.length) params.brandId.forEach((id) => search.append('brandId', String(id)));
  if (params.attrs?.length) params.attrs.forEach((a) => search.append('attrs', a));

  const path = `/search/product/list?${search.toString()}`;
  try {
    const r = await fetchApi<SearchResultResponse>(path);
    if (r.code != null && Number(r.code) !== 0) {
      throw new Error(r.msg || 'Search failed');
    }
    const payload = r.data as SearchResultResponse | undefined;
    if (payload?.degraded) {
      throw new Error('Search service temporarily unavailable (gateway degraded). Retry in a moment.');
    }
    return mapSearchResponse(payload);
  } catch (e) {
    if (options?.softFail) {
      return { ...EMPTY_SEARCH_RESULT, pageNum: params.pageNum ?? 1 };
    }
    throw e;
  }
}
