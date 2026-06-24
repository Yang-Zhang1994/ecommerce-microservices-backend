package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.enums.OrderStatusEnum;
import com.atguigu.gulimall.order.repository.OrderRepository;
import com.atguigu.gulimall.order.service.OrderMqOutboxService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.service.StripePaymentService;
import com.atguigu.gulimall.order.tracing.OrderTraceAttributes;
import com.atguigu.gulimall.order.vo.OrderCouponReleaseMessage;
import com.atguigu.gulimall.order.vo.OrderFinishEventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Locale;

@Service("orderService")
public class OrderServiceImpl implements OrderService {

    private static final String SECKILL_ORDER_NOTE = "Seckill order";

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderMqOutboxService orderMqOutboxService;
    @Autowired
    @Lazy
    private StripePaymentService stripePaymentService;

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<OrderEntity>().getPageable(params, Sort.by("id").ascending());
        Page<OrderEntity> page = orderRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public OrderEntity getById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public void save(OrderEntity entity) {
        orderRepository.save(entity);
    }

    @Override
    public void updateById(OrderEntity entity) {
        orderRepository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        orderRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeUnpaidOrderByOrderSn(String orderSn) {
        if (!StringUtils.hasText(orderSn)) {
            return;
        }
        OrderEntity order = orderRepository.findByOrderSn(orderSn.trim()).orElse(null);
        if (order == null) {
            return;
        }
        if (order.getPaymentTime() != null) {
            return;
        }
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.CREATE_NEW.getCode())) {
            return;
        }
        stripePaymentService.expireOpenCheckoutSessionForOrder(orderSn.trim());
        order.setStatus(OrderStatusEnum.CLOSED.getCode());
        order.setModifyTime(new Date());
        orderRepository.save(order);
        String sn = order.getOrderSn();
        orderMqOutboxService.enqueueStockUnlockForCancel(sn);
        OrderCouponReleaseMessage coupon = new OrderCouponReleaseMessage();
        coupon.setOrderSn(sn);
        coupon.setReason("timeout");
        orderMqOutboxService.enqueueCouponRelease(coupon);
        notifySeckillGrabRelease(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUnpaidByMember(String orderSn, Long memberId) {
        if (memberId == null || !StringUtils.hasText(orderSn)) {
            throw new IllegalArgumentException("orderSn and memberId required");
        }
        OrderEntity order = orderRepository.findByOrderSnAndMemberId(orderSn.trim(), memberId)
                .orElseThrow(() -> new IllegalStateException("Order not found"));
        if (order.getPaymentTime() != null) {
            throw new IllegalStateException("Order already paid");
        }
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.CREATE_NEW.getCode())) {
            throw new IllegalStateException("Order cannot be cancelled");
        }
        stripePaymentService.expireOpenCheckoutSessionForOrder(orderSn.trim());
        order.setStatus(OrderStatusEnum.CLOSED.getCode());
        order.setModifyTime(new Date());
        orderRepository.save(order);
        String sn = order.getOrderSn();
        orderMqOutboxService.enqueueStockUnlockForCancel(sn);
        OrderCouponReleaseMessage c = new OrderCouponReleaseMessage();
        c.setOrderSn(sn);
        c.setReason("cancel");
        orderMqOutboxService.enqueueCouponRelease(c);
        notifySeckillGrabRelease(order);
    }

    private void notifySeckillGrabRelease(OrderEntity order) {
        if (order == null || !StringUtils.hasText(order.getOrderSn()) || !isSeckillOrder(order)) {
            return;
        }
        orderMqOutboxService.enqueueSeckillGrabRelease(order.getOrderSn().trim());
    }

    private static boolean isSeckillOrder(OrderEntity order) {
        if (StringUtils.hasText(order.getNote()) && order.getNote().trim().startsWith(SECKILL_ORDER_NOTE)) {
            return true;
        }
        return StringUtils.hasText(order.getMemberUsername())
                && order.getMemberUsername().trim().startsWith("seckill-");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markPaidByOrderSn(String orderSn) {
        if (!StringUtils.hasText(orderSn)) {
            throw new IllegalArgumentException("orderSn required");
        }
        OrderTraceAttributes.setOrderSn(orderSn);
        OrderEntity order = orderRepository.findByOrderSn(orderSn.trim())
                .orElseThrow(() -> new IllegalStateException("Order not found"));
        if (order.getPaymentTime() != null) {
            return;
        }
        // Redirect finalize vs Stripe webhook may race; avoid duplicate MQ publish.
        if (Objects.equals(order.getStatus(), OrderStatusEnum.PAYED.getCode())) {
            return;
        }
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.CREATE_NEW.getCode())) {
            throw new IllegalStateException("Order is not pending payment");
        }
        order.setStatus(OrderStatusEnum.PAYED.getCode());
        order.setPaymentTime(new Date());
        order.setModifyTime(new Date());
        orderRepository.save(order);
        OrderFinishEventMessage m = new OrderFinishEventMessage();
        m.setOrderSn(order.getOrderSn());
        m.setMemberId(order.getMemberId());
        m.setPayAmount(order.getPayAmount());
        m.setOrderStatus(order.getStatus());
        orderMqOutboxService.enqueueOrderFinishUser(m);
        orderMqOutboxService.enqueueOrderFinishWare(m);
    }

    @Override
    public Page<OrderEntity> listMemberOrders(Long memberId, String statusTab, int pageNum, int pageSize) {
        if (memberId == null) {
            return Page.empty();
        }
        int safePage = Math.max(1, pageNum);
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        Pageable pageable = PageRequest.of(
                safePage - 1,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createTime").and(Sort.by(Sort.Direction.DESC, "id"))
        );
        List<Integer> statuses = resolveStatuses(statusTab);
        if (statuses == null) {
            return orderRepository.findByMemberIdOrderByCreateTimeDesc(memberId, pageable);
        }
        return orderRepository.findByMemberIdAndStatusInOrderByCreateTimeDesc(memberId, statuses, pageable);
    }

    private List<Integer> resolveStatuses(String statusTab) {
        if (!StringUtils.hasText(statusTab)) {
            return null;
        }
        String key = statusTab.trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "pending" -> List.of(OrderStatusEnum.CREATE_NEW.getCode());
            case "paid" -> List.of(
                    OrderStatusEnum.PAYED.getCode(),
                    OrderStatusEnum.SHIPPED.getCode(),
                    OrderStatusEnum.COMPLETED.getCode()
            );
            case "closed" -> List.of(OrderStatusEnum.CLOSED.getCode(), OrderStatusEnum.INVALID.getCode());
            default -> null;
        };
    }
}
