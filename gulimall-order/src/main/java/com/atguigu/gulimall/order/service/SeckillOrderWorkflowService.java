package com.atguigu.gulimall.order.service;

import com.atguigu.common.to.SeckillOrderCreateTo;

public interface SeckillOrderWorkflowService {

    /** Idempotent: persist oms_order + line item, lock ware stock, enqueue flash pay timeout. */
    void createFromSeckillMessage(SeckillOrderCreateTo payload);
}
