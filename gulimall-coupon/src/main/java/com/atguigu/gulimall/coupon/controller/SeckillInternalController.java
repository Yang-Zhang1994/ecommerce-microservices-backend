package com.atguigu.gulimall.coupon.controller;

import com.atguigu.common.to.SeckillWarmupSessionVo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.coupon.service.SeckillInternalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal read APIs for gulimall-seckill (no direct DB access in seckill service).
 */
@RestController
@RequestMapping("/coupon/internal/seckill")
public class SeckillInternalController {

    private final SeckillInternalService seckillInternalService;

    public SeckillInternalController(SeckillInternalService seckillInternalService) {
        this.seckillInternalService = seckillInternalService;
    }

    @GetMapping("/warmup")
    public R warmup(@RequestParam(value = "days", defaultValue = "3") int days) {
        List<SeckillWarmupSessionVo> data = seckillInternalService.warmupData(days);
        return R.ok().put("data", data);
    }
}
