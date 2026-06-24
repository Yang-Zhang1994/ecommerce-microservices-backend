package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal hooks (admin save, coupon service) to refresh Redis without restarting seckill.
 */
@RestController
@RequestMapping("seckill/internal")
public class SeckillInternalController {

    private final SeckillService seckillService;

    public SeckillInternalController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    @PostMapping("/warmup")
    public R warmup() {
        seckillService.uploadSeckillSkuLatest3Days(true);
        return R.ok();
    }
}
