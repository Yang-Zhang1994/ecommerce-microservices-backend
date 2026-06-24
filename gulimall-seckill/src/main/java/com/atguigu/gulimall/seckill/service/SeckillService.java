package com.atguigu.gulimall.seckill.service;

import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * Storefront seckill flow: warm sessions into Redis, expose current SKUs, and run the
 * high-concurrency kill (Redisson semaphore in memory, async persist to DB).
 */
public interface SeckillService {

    /** Warm the next N days of enabled sessions + their SKUs into Redis (idempotent). */
    void uploadSeckillSkuLatest3Days();

    /**
     * @param refreshStock when true (admin-triggered warm-up), reset in-memory stock semaphores
     *                     to configured {@code seckillCount}; when false (cron), only init new
     *                     SKUs or adjust when the configured count changed.
     */
    void uploadSeckillSkuLatest3Days(boolean refreshStock);

    /** SKUs of the session that is live right now (for the homepage flash-sale block). */
    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    /** SKUs scheduled to start within the warm-up window (not live yet; no random buy token). */
    List<SeckillSkuRedisTo> getUpcomingSeckillSkus();

    /** Lookup a single warmed seckill SKU (random code hidden unless the session is live). */
    SeckillSkuRedisTo getSeckillSkuInfo(Long skuId);

    /**
     * Attempt to buy {@code num} units. Returns the generated order number on success,
     * or {@code null} when sold out. Throws for invalid/expired requests and limit breaches.
     */
    String kill(String killId, String key, int num, Long memberId);

    /**
     * Idempotent rollback when an unpaid seckill order is closed/cancelled: restore user quota
     * and in-memory semaphore permits.
     */
    void rollbackUnpaidGrab(String orderSn);
}
