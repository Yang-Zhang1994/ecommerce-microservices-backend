package com.atguigu.gulimall.coupon.support;

import com.atguigu.common.client.SeckillApi;
import com.atguigu.common.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Ask gulimall-seckill to reload Redis after admin changes in coupon DB.
 */
@Component
public class SeckillWarmupNotifier {

    private static final Logger log = LoggerFactory.getLogger(SeckillWarmupNotifier.class);

    private final SeckillApi seckillApi;

    public SeckillWarmupNotifier(SeckillApi seckillApi) {
        this.seckillApi = seckillApi;
    }

    public void notifyAfterCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    triggerWarmup();
                }
            });
            return;
        }
        triggerWarmup();
    }

    private void triggerWarmup() {
        try {
            R response = seckillApi.triggerWarmup();
            if (response == null || response.getCode() == null || response.getCode() != 0) {
                log.warn("Seckill warm-up trigger returned non-OK: {}", response);
            } else if (log.isDebugEnabled()) {
                log.debug("Seckill warm-up triggered after coupon admin change");
            }
        } catch (Exception e) {
            log.warn("Seckill warm-up trigger failed (storefront may lag until cron): {}", e.toString());
        }
    }
}
