package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.to.SeckillOrderCreateTo;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enums.OrderStatusEnum;
import com.atguigu.gulimall.order.repository.OrderItemRepository;
import com.atguigu.gulimall.order.repository.OrderRepository;
import com.atguigu.gulimall.order.service.OrderMqOutboxService;
import com.atguigu.gulimall.order.service.SeckillOrderWorkflowService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillOrderWorkflowServiceImpl implements SeckillOrderWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderWorkflowServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMqOutboxService orderMqOutboxService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${order.product-service-url:http://gulimall-gateway:88/api/product}")
    private String productServiceUrl;

    @Value("${order.ware-service-url:http://gulimall-gateway:88/api/ware}")
    private String wareServiceUrl;

    public SeckillOrderWorkflowServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderMqOutboxService orderMqOutboxService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderMqOutboxService = orderMqOutboxService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFromSeckillMessage(SeckillOrderCreateTo payload) {
        if (payload == null || !StringUtils.hasText(payload.getOrderSn()) || payload.getMemberId() == null) {
            return;
        }
        String orderSn = payload.getOrderSn().trim();
        if (orderRepository.findByOrderSn(orderSn).isPresent()) {
            log.info("Seckill order already exists, skip: {}", orderSn);
            return;
        }

        int qty = payload.getNum() == null || payload.getNum() <= 0 ? 1 : payload.getNum();
        BigDecimal unitPrice = payload.getSeckillPrice() == null
                ? BigDecimal.ZERO
                : payload.getSeckillPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> skuInfo = fetchSkuInfo(payload.getSkuId());
        String title = asString(skuInfo.get("skuTitle"));
        if (!StringUtils.hasText(title)) {
            title = asString(skuInfo.get("skuName"));
        }
        String pic = asString(skuInfo.get("skuDefaultImg"));

        if (!lockStock(orderSn, payload.getSkuId(), qty)) {
            log.warn("Seckill order stock lock failed orderSn={} skuId={}", orderSn, payload.getSkuId());
            return;
        }

        OrderEntity order = new OrderEntity();
        order.setOrderSn(orderSn);
        order.setMemberId(payload.getMemberId());
        order.setMemberUsername("seckill-" + payload.getMemberId());
        order.setCreateTime(new Date());
        order.setModifyTime(new Date());
        order.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        order.setSourceType(0);
        order.setPayType(1);
        order.setTotalAmount(lineTotal);
        order.setPayAmount(lineTotal);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setPromotionAmount(BigDecimal.ZERO);
        order.setIntegrationAmount(BigDecimal.ZERO);
        order.setCouponAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setDeleteStatus(0);
        order.setConfirmStatus(0);
        order.setUseIntegration(0);
        order.setNote("Seckill order");
        orderRepository.save(order);

        OrderItemEntity item = new OrderItemEntity();
        item.setOrderId(order.getId());
        item.setOrderSn(orderSn);
        item.setSkuId(payload.getSkuId());
        item.setSkuName(title);
        item.setSkuPic(pic);
        item.setSpuId(toLong(skuInfo.get("spuId")));
        item.setSkuPrice(unitPrice);
        item.setSkuQuantity(qty);
        item.setSkuAttrsVals("");
        item.setPromotionAmount(BigDecimal.ZERO);
        item.setCouponAmount(BigDecimal.ZERO);
        item.setIntegrationAmount(BigDecimal.ZERO);
        item.setRealAmount(lineTotal);
        item.setGiftIntegration(0);
        item.setGiftGrowth(0);
        orderItemRepository.save(item);

        orderMqOutboxService.enqueueSeckillOrderWaitPayDelay(orderSn);
        log.info("Seckill order persisted orderSn={} memberId={} skuId={}", orderSn, payload.getMemberId(), payload.getSkuId());
    }

    private Map<String, Object> fetchSkuInfo(Long skuId) {
        if (skuId == null) {
            return Map.of();
        }
        String url = productServiceUrl + "/skuinfo/info/" + skuId;
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Map.of();
            }
            Map<String, Object> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object skuInfo = body.get("skuInfo");
            if (skuInfo instanceof Map<?, ?> map) {
                Map<String, Object> out = new HashMap<>();
                map.forEach((k, v) -> out.put(String.valueOf(k), v));
                return out;
            }
        } catch (Exception e) {
            log.warn("fetchSkuInfo failed skuId={}", skuId, e);
        }
        return Map.of();
    }

    private boolean lockStock(String orderSn, Long skuId, int qty) {
        if (skuId == null || qty <= 0) {
            return false;
        }
        String url = wareServiceUrl + "/waresku/lock";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> lockLine = new HashMap<>();
        lockLine.put("skuId", skuId);
        lockLine.put("count", qty);
        Map<String, Object> req = new HashMap<>();
        req.put("orderSn", orderSn);
        req.put("locks", List.of(lockLine));
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(req, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return false;
            }
            Map<String, Object> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object codeObj = body.get("code");
            int code = codeObj instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(codeObj));
            if (code != 0) {
                return false;
            }
            Object dataObj = body.get("data");
            return dataObj instanceof List<?> list && !list.isEmpty();
        } catch (Exception e) {
            log.warn("lockStock failed orderSn={} skuId={}", orderSn, skuId, e);
            return false;
        }
    }

    private static String asString(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(v).trim());
        } catch (Exception e) {
            return null;
        }
    }
}
