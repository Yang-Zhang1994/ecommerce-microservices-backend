package com.atguigu.common.client;

import com.atguigu.common.utils.R;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * HTTP Interface for gulimall-seckill internal/admin hooks.
 */
@HttpExchange
public interface SeckillApi {

    /** Reload enabled seckill sessions + SKUs from coupon into Redis (storefront cache). */
    @PostExchange("/seckill/internal/warmup")
    R triggerWarmup();
}
