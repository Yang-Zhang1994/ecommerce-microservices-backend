package com.atguigu.gulimall.order.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.enums.OrderStatusEnum;
import com.atguigu.gulimall.order.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * Internal APIs for other services (e.g. product cascade delete guards).
 */
@RestController
@RequestMapping("/order/internal")
public class OrderInternalController {

    private static final List<Integer> ACTIVE_ORDER_STATUSES = List.of(
            OrderStatusEnum.CREATE_NEW.getCode(),
            OrderStatusEnum.PAYED.getCode(),
            OrderStatusEnum.SHIPPED.getCode()
    );

    @Autowired
    private OrderItemRepository orderItemRepository;

    @PostMapping("/sku/active-order-skus")
    public R activeOrderSkus(@RequestBody List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return R.ok().put("data", Collections.emptyList());
        }
        List<Long> blocked = orderItemRepository.findSkuIdsInActiveOrders(skuIds, ACTIVE_ORDER_STATUSES);
        return R.ok().put("data", blocked);
    }
}
