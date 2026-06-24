package com.atguigu.gulimall.order.controller;

import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
public class OrderHealthController {

    /**
     * Lightweight health endpoint for gateway/domain routing checks.
     * Keep this endpoint free of DB/service dependencies.
     */
    @GetMapping("/ping")
    public R ping() {
        return R.ok()
                .put("msg", "order ok")
                .put("service", "gulimall-order");
    }
}
