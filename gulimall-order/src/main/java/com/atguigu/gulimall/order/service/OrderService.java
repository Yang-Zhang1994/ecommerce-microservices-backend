package com.atguigu.gulimall.order.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.Map;

public interface OrderService {

    PageUtils queryPage(Map<String, Object> params);
    OrderEntity getById(Long id);
    void save(OrderEntity entity);
    void updateById(OrderEntity entity);
    void removeByIds(Collection<?> ids);

    /**
     * Idempotent close for unpaid orders (timeout). No-op if already paid or not {@code CREATE_NEW}.
     */
    void closeUnpaidOrderByOrderSn(String orderSn);

    /** Member cancels pending order; after commit notifies stock + coupon via MQ. */
    void cancelUnpaidByMember(String orderSn, Long memberId);

    /** Marks order paid and publishes {@code order.finish.user} / {@code order.finish.ware} after commit (idempotent if already paid). */
    void markPaidByOrderSn(String orderSn);

    /**
     * Member-facing order list filtered by status tab ({@code all/pending/paid/closed}).
     */
    Page<OrderEntity> listMemberOrders(Long memberId, String statusTab, int pageNum, int pageSize);
}
