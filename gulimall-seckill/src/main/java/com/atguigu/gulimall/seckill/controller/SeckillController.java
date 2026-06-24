package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.util.SeckillUserSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 秒杀抢购（storefront）。通过网关 {@code /api/seckill/**} 暴露。
 */
@RestController
@RequestMapping("seckill")
public class SeckillController {

    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    /** 当前正在进行的秒杀场次商品（首页秒杀楼层）。 */
    @GetMapping("/currentSeckillSkus")
    public R currentSeckillSkus() {
        List<SeckillSkuRedisTo> skus = seckillService.getCurrentSeckillSkus();
        return R.ok().put("data", skus);
    }

    /** 未来 warm-up 窗口内即将开始的秒杀商品（首页预告楼层）。 */
    @GetMapping("/upcomingSeckillSkus")
    public R upcomingSeckillSkus() {
        List<SeckillSkuRedisTo> skus = seckillService.getUpcomingSeckillSkus();
        return R.ok().put("data", skus);
    }

    /** 单个商品的秒杀信息（商品详情页）。 */
    @GetMapping("/sku/{skuId}")
    public R getSeckillSkuInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo to = seckillService.getSeckillSkuInfo(skuId);
        return R.ok().put("data", to);
    }

    /**
     * 抢购：内存信号量扣减 + 异步落库。
     * 成功返回订单号；售罄返回业务错误；未登录返回 401。
     */
    @PostMapping("/kill")
    public R kill(@RequestParam("killId") String killId,
                  @RequestParam("key") String key,
                  @RequestParam(value = "num", defaultValue = "1") Integer num,
                  HttpServletRequest request) {
        Long memberId = SeckillUserSupport.currentMemberId(request);
        if (memberId == null) {
            return R.error(401, "Please login first");
        }
        try {
            String orderSn = seckillService.kill(killId, key, num == null ? 1 : num, memberId);
            if (orderSn == null) {
                return R.error(60001, "Sold out");
            }
            return R.ok().put("data", orderSn).put("orderSn", orderSn);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return R.error(60002, e.getMessage());
        }
    }
}
