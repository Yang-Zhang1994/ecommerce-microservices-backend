package com.atguigu.gulimall.seckill.constant;

/**
 * Redis key prefixes for the seckill warm-up cache and stock semaphore.
 */
public final class SeckillConstant {

    private SeckillConstant() {}

    /** List per session: key = prefix + {sessionId}:{startMs}_{endMs}, value = list of "{sessionId}_{skuId}". */
    public static final String SESSION_CACHE_PREFIX = "seckill:sessions:";

    /** Hash of all warmed seckill SKUs: field = "{sessionId}_{skuId}", value = SeckillSkuRedisTo JSON. */
    public static final String SKUKILL_CACHE_PREFIX = "seckill:skus";

    /** Redisson semaphore per SKU: key = prefix + randomCode, permits = seckillCount. */
    public static final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    /** Per-user grab count in this session: key = prefix + "{userId}_{sessionId}_{skuId}". */
    public static final String USER_BUY_FLAG = "seckill:user:";

    /** Grab metadata for rollback on unpaid close: key = prefix + orderSn. */
    public static final String ORDER_GRAB_META_PREFIX = "seckill:order:";

    /** Distributed lock guarding the warm-up job. */
    public static final String UPLOAD_LOCK = "seckill:upload:lock";
}
