package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.enums.OrderStatusEnum;
import com.atguigu.gulimall.order.repository.OrderItemRepository;
import com.atguigu.gulimall.order.repository.OrderRepository;
import com.atguigu.gulimall.order.repository.PaymentInfoRepository;
import com.atguigu.gulimall.order.service.OrderMemberQueryService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderDetailItemVo;
import com.atguigu.gulimall.order.vo.OrderDetailVo;
import com.atguigu.gulimall.order.vo.OrderListItemVo;
import com.atguigu.gulimall.order.vo.OrderPaymentSummaryVo;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderMemberQueryServiceImpl implements OrderMemberQueryService {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentInfoRepository paymentInfoRepository;

    public OrderMemberQueryServiceImpl(
            OrderService orderService,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            PaymentInfoRepository paymentInfoRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentInfoRepository = paymentInfoRepository;
    }

    @Override
    public Page<OrderListItemVo> listMemberOrders(Long memberId, String statusTab, int pageNum, int pageSize) {
        Page<OrderEntity> page = orderService.listMemberOrders(memberId, statusTab, pageNum, pageSize);
        List<OrderEntity> orders = page.getContent();
        if (orders.isEmpty()) {
            return page.map(OrderListItemVo::from);
        }
        List<String> orderSns = orders.stream()
                .map(OrderEntity::getOrderSn)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        Map<String, ItemRollup> rollupBySn = rollupItems(orderItemRepository.findByOrderSnInOrderByIdAsc(orderSns));
        return page.map(order -> {
            OrderListItemVo vo = OrderListItemVo.from(order);
            ItemRollup rollup = rollupBySn.get(order.getOrderSn());
            if (rollup != null) {
                vo.setFirstItemTitle(rollup.firstTitle);
                vo.setFirstItemPic(rollup.firstPic);
                vo.setItemCount(rollup.itemCount);
            }
            return vo;
        });
    }

    @Override
    public OrderDetailVo getMemberOrderDetail(Long memberId, String orderSn) {
        if (memberId == null || !StringUtils.hasText(orderSn)) {
            return null;
        }
        OrderEntity order = orderRepository.findByOrderSnAndMemberId(orderSn.trim(), memberId).orElse(null);
        if (order == null) {
            return null;
        }
        OrderDetailVo vo = new OrderDetailVo();
        vo.setId(order.getId());
        vo.setOrderSn(StringUtils.hasText(order.getOrderSn()) ? order.getOrderSn().trim() : order.getOrderSn());
        vo.setStatus(order.getStatus());
        OrderStatusEnum statusEnum = OrderStatusEnum.fromCode(order.getStatus());
        vo.setStatusText(statusEnum == null ? "Unknown" : statusEnum.getDesc());
        vo.setCreateTime(order.getCreateTime());
        vo.setPaymentTime(order.getPaymentTime());
        vo.setDeliveryTime(order.getDeliveryTime());
        vo.setReceiveTime(order.getReceiveTime());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setPromotionAmount(order.getPromotionAmount());
        vo.setCouponAmount(order.getCouponAmount());
        vo.setIntegrationAmount(order.getIntegrationAmount());
        vo.setNote(order.getNote());
        vo.setDeliveryCompany(order.getDeliveryCompany());
        vo.setDeliverySn(order.getDeliverySn());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverPostCode(order.getReceiverPostCode());
        vo.setReceiverProvince(order.getReceiverProvince());
        vo.setReceiverCity(order.getReceiverCity());
        vo.setReceiverRegion(order.getReceiverRegion());
        vo.setReceiverDetailAddress(order.getReceiverDetailAddress());
        vo.setItems(orderItemRepository.findByOrderSnOrderByIdAsc(order.getOrderSn()).stream()
                .map(this::toDetailItem)
                .collect(Collectors.toList()));
        vo.setPayment(buildPaymentSummary(order, paymentInfoRepository.findByOrderSn(order.getOrderSn()).orElse(null)));
        return vo;
    }

    private OrderDetailItemVo toDetailItem(OrderItemEntity item) {
        OrderDetailItemVo vo = new OrderDetailItemVo();
        vo.setSkuId(item.getSkuId());
        vo.setSkuName(firstText(item.getSkuName(), item.getSpuName(), "Product"));
        vo.setSkuPic(firstText(item.getSkuPic(), item.getSpuPic()));
        vo.setSkuPrice(item.getSkuPrice());
        vo.setSkuQuantity(item.getSkuQuantity());
        vo.setSkuAttrs(parseSkuAttrs(item.getSkuAttrsVals()));
        vo.setRealAmount(item.getRealAmount());
        vo.setSpuName(item.getSpuName());
        return vo;
    }

    private static OrderPaymentSummaryVo buildPaymentSummary(OrderEntity order, PaymentInfoEntity payment) {
        OrderPaymentSummaryVo vo = new OrderPaymentSummaryVo();
        boolean paidByStatus = order.getStatus() != null
                && order.getStatus() >= OrderStatusEnum.PAYED.getCode()
                && order.getStatus() <= OrderStatusEnum.COMPLETED.getCode();
        boolean paidByPayment = payment != null && "SUCCEEDED".equalsIgnoreCase(payment.getPaymentStatus());
        vo.setPaid(paidByStatus || paidByPayment || order.getPaymentTime() != null);
        vo.setPaymentTime(order.getPaymentTime() != null ? order.getPaymentTime()
                : payment == null ? null : payment.getConfirmTime());
        vo.setPaidAmount(order.getPayAmount());
        if (payment != null) {
            vo.setPaymentStatus(payment.getPaymentStatus());
            vo.setTradeNo(payment.getAlipayTradeNo());
            if (payment.getCallbackContent() != null && payment.getCallbackContent().contains("stripe")) {
                vo.setPayMethodLabel("Stripe card");
            } else if ("SUCCEEDED".equalsIgnoreCase(payment.getPaymentStatus())) {
                vo.setPayMethodLabel("Online payment");
            } else {
                vo.setPayMethodLabel("Card checkout");
            }
        } else if (vo.isPaid()) {
            vo.setPayMethodLabel(order.getPayType() == null ? "Paid" : payTypeLabel(order.getPayType()));
        } else {
            vo.setPayMethodLabel(null);
        }
        return vo;
    }

    private static String payTypeLabel(Integer payType) {
        return switch (payType) {
            case 1 -> "Alipay";
            case 2 -> "WeChat Pay";
            case 3 -> "UnionPay";
            case 4 -> "Cash on delivery";
            default -> "Paid";
        };
    }

    private static Map<String, ItemRollup> rollupItems(List<OrderItemEntity> items) {
        Map<String, List<OrderItemEntity>> bySn = new HashMap<>();
        for (OrderItemEntity item : items) {
            if (!StringUtils.hasText(item.getOrderSn())) {
                continue;
            }
            bySn.computeIfAbsent(item.getOrderSn(), k -> new ArrayList<>()).add(item);
        }
        Map<String, ItemRollup> out = new HashMap<>();
        for (Map.Entry<String, List<OrderItemEntity>> e : bySn.entrySet()) {
            List<OrderItemEntity> lines = e.getValue();
            lines.sort(Comparator.comparing(OrderItemEntity::getId, Comparator.nullsLast(Long::compareTo)));
            OrderItemEntity first = lines.get(0);
            ItemRollup rollup = new ItemRollup();
            rollup.firstTitle = lineTitle(first);
            rollup.firstPic = linePic(first);
            rollup.itemCount = lines.stream()
                    .map(OrderItemEntity::getSkuQuantity)
                    .filter(q -> q != null && q > 0)
                    .mapToInt(Integer::intValue)
                    .sum();
            if (rollup.itemCount <= 0) {
                rollup.itemCount = lines.size();
            }
            out.put(e.getKey(), rollup);
        }
        return out;
    }

    private static String lineTitle(OrderItemEntity item) {
        return firstText(item.getSkuName(), item.getSpuName(), "Product");
    }

    private static String linePic(OrderItemEntity item) {
        return firstText(item.getSkuPic(), item.getSpuPic());
    }

    private static List<String> parseSkuAttrs(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return Arrays.stream(trimmed.split("[;,]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private static String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return null;
    }

    private static final class ItemRollup {
        private String firstTitle;
        private String firstPic;
        private int itemCount;
    }
}
