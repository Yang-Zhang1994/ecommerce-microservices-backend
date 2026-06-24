package com.atguigu.gulimall.seckill.job;

import com.atguigu.gulimall.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically warms upcoming seckill sessions into Redis. A Redisson lock inside the service
 * keeps this safe across multiple seckill instances.
 */
@Component
public class SeckillSkuScheduled {

    private static final Logger log = LoggerFactory.getLogger(SeckillSkuScheduled.class);

    private final SeckillService seckillService;

    public SeckillSkuScheduled(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    /** Warm once on startup so the homepage has data without waiting for the first cron tick. */
    @EventListener(ApplicationReadyEvent.class)
    public void warmOnStartup() {
        safeUpload();
    }

    @Scheduled(cron = "${seckill.warmup-cron:0 0/30 * * * ?}")
    public void uploadSeckillSkuLatest3Days() {
        safeUpload();
    }

    private void safeUpload() {
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } catch (Exception e) {
            log.error("Seckill warm-up failed", e);
        }
    }
}
