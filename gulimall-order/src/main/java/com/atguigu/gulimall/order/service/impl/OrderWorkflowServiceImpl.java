package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enums.OrderStatusEnum;
import com.atguigu.gulimall.order.interceptor.OrderLoginInterceptor;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.repository.OrderItemRepository;
import com.atguigu.gulimall.order.repository.OrderRepository;
import com.atguigu.gulimall.order.service.OrderMqOutboxService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.service.OrderWorkflowService;
import com.atguigu.gulimall.order.tracing.OrderTraceAttributes;
import com.atguigu.gulimall.order.to.OrderLoginUserTo;
import com.atguigu.gulimall.order.vo.MemberAddressVo;
import com.atguigu.gulimall.order.vo.OrderConfirmItemVo;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderDetailItemVo;
import com.atguigu.gulimall.order.vo.OrderSubmitResponseVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SeckillOrderBindVo;
import com.atguigu.gulimall.order.vo.SeckillOrderConfirmVo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderWorkflowServiceImpl implements OrderWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(OrderWorkflowServiceImpl.class);
    private static final String ORDER_TOKEN_PREFIX = "order:token:";
    private static final String TOKEN_REFRESH_MSG = "Please refresh confirm page";
    private static final BigDecimal PAY_AMOUNT_TOLERANCE = new BigDecimal("0.01");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;
    private final OrderMqOutboxService orderMqOutboxService;

    @Value("${order.cart-service-url:http://gulimall-cart:13000}")
    private String cartServiceUrl;

    @Value("${order.member-service-url:http://gulimall-gateway:88/api/member}")
    private String memberServiceUrl;

    @Value("${order.product-service-url:http://gulimall-gateway:88/api/product}")
    private String productServiceUrl;

    @Value("${order.ware-service-url:http://gulimall-gateway:88/api/ware}")
    private String wareServiceUrl;

    @Value("${order.token-ttl-seconds:1800}")
    private long orderTokenTtlSeconds;

    @Value("${order.default-freight:0}")
    private BigDecimal defaultFreight;

    @Value("${order.free-shipping-threshold:0}")
    private BigDecimal freeShippingThreshold;

    @Value("${order.flash-pay-timeout-minutes:15}")
    private int flashPayTimeoutMinutes;

    private static final String SECKILL_ORDER_NOTE = "Seckill order";

    public OrderWorkflowServiceImpl(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            StringRedisTemplate stringRedisTemplate,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderService orderService,
            OrderMqOutboxService orderMqOutboxService
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderService = orderService;
        this.orderMqOutboxService = orderMqOutboxService;
    }

    @Override
    public OrderConfirmVo confirmOrder(String cookieHeader) {
        OrderLoginUserTo loginUser = getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            return new OrderConfirmVo();
        }
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        List<OrderConfirmItemVo> checkedItems = loadConfirmCartItems(cookieHeader);
        confirmVo.setItems(checkedItems);
        applyConfirmAmounts(confirmVo, loginUser, checkedItems);
        applyConfirmAddresses(confirmVo, cookieHeader, loginUser);
        issueConfirmToken(loginUser, confirmVo);
        return confirmVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitResponseVo submitOrder(OrderSubmitVo submitVo, String cookieHeader) {
        OrderSubmitResponseVo response = new OrderSubmitResponseVo();
        OrderLoginUserTo loginUser = getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            return fail(response, 401, "Please login first");
        }

        String confirmPayPrice = consumeSubmitToken(submitVo, loginUser.getUserId());
        if (confirmPayPrice == null) {
            return fail(response, 1, TOKEN_REFRESH_MSG);
        }

        List<OrderConfirmItemVo> checkedItems = loadCheckedSubmitItems(cookieHeader);
        if (checkedItems.isEmpty()) {
            return fail(response, 2, "No checked cart items");
        }

        AmountSnapshot amounts = calcAndValidateSubmitAmount(submitVo, confirmPayPrice, checkedItems, response);
        if (amounts == null) {
            return response;
        }

        String orderSn = buildOrderSn();
        StockLockOutcome lockOutcome = lockOrderStock(orderSn, checkedItems);
        if (lockOutcome.lines.isEmpty()) {
            if (lockOutcome.lockApiError) {
                return fail(response, 5, "Stock service unavailable, please retry");
            }
            return fail(response, 4, "Insufficient stock, please refresh confirm page");
        }

        try {
            OrderEntity order = buildOrderEntity(submitVo, cookieHeader, loginUser, orderSn, amounts);
            orderRepository.save(order);

            List<OrderItemEntity> orderItems = buildOrderItems(order, orderSn, checkedItems);
            orderItemRepository.saveAll(orderItems);

            clearSubmittedCartItems(cookieHeader, checkedItems);

            orderMqOutboxService.enqueueOrderWaitPayDelay(orderSn);

            OrderTraceAttributes.setSubmitOrderContext(orderSn, loginUser.getUserId(), amounts.payAmount);

            response.setCode(0);
            response.setMsg("success");
            response.setOrderSn(orderSn);
            response.setPayAmount(amounts.payAmount);
            return response;
        } catch (Exception e) {
            unlockOrderStock(lockOutcome.taskId, lockOutcome.lines);
            throw e;
        }
    }

    @Override
    public SeckillOrderConfirmVo confirmSeckillOrder(String orderSn, String cookieHeader) {
        OrderLoginUserTo loginUser = getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new IllegalStateException("Please login first");
        }
        if (!StringUtils.hasText(orderSn)) {
            throw new IllegalArgumentException("orderSn required");
        }
        OrderEntity order = orderRepository.findByOrderSnAndMemberId(orderSn.trim(), loginUser.getUserId())
                .orElseThrow(() -> new IllegalStateException("Order not found"));
        if (!isSeckillOrder(order)) {
            throw new IllegalStateException("Not a flash-sale order");
        }
        if (!java.util.Objects.equals(order.getStatus(), OrderStatusEnum.CREATE_NEW.getCode())) {
            throw new IllegalStateException("Order is not awaiting payment");
        }

        SeckillOrderConfirmVo vo = new SeckillOrderConfirmVo();
        vo.setOrderSn(order.getOrderSn() == null ? orderSn.trim() : order.getOrderSn().trim());
        vo.setPayAmount(order.getPayAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setCreateTime(order.getCreateTime());
        vo.setFlashPayTimeoutMinutes(Math.max(1, flashPayTimeoutMinutes));
        vo.setNeedsAddress(needsShippingAddress(order));
        vo.setReceiverName(trimToNull(order.getReceiverName()));
        vo.setReceiverPhone(trimToNull(order.getReceiverPhone()));
        vo.setReceiverProvince(trimToNull(order.getReceiverProvince()));
        vo.setReceiverCity(trimToNull(order.getReceiverCity()));
        vo.setReceiverRegion(trimToNull(order.getReceiverRegion()));
        vo.setReceiverDetailAddress(trimToNull(order.getReceiverDetailAddress()));

        List<MemberAddressVo> addresses = fetchMemberAddresses(cookieHeader, loginUser.getUserId());
        vo.setAddresses(addresses);

        List<OrderItemEntity> lines = orderItemRepository.findByOrderSnOrderByIdAsc(order.getOrderSn());
        vo.setItems(lines.stream().map(this::toSeckillConfirmItem).toList());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindSeckillOrderAddress(SeckillOrderBindVo bindVo, String cookieHeader) {
        OrderLoginUserTo loginUser = getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new IllegalStateException("Please login first");
        }
        if (bindVo == null || !StringUtils.hasText(bindVo.getOrderSn())) {
            throw new IllegalArgumentException("orderSn required");
        }
        if (bindVo.getAddressId() == null) {
            throw new IllegalArgumentException("addressId required");
        }
        String orderSn = bindVo.getOrderSn().trim();
        OrderEntity order = orderRepository.findByOrderSnAndMemberId(orderSn, loginUser.getUserId())
                .orElseThrow(() -> new IllegalStateException("Order not found"));
        if (!isSeckillOrder(order)) {
            throw new IllegalStateException("Not a flash-sale order");
        }
        if (!java.util.Objects.equals(order.getStatus(), OrderStatusEnum.CREATE_NEW.getCode())) {
            throw new IllegalStateException("Order is not awaiting payment");
        }

        List<MemberAddressVo> addresses = fetchMemberAddresses(cookieHeader, loginUser.getUserId());
        MemberAddressVo address = addresses.stream()
                .filter(a -> bindVo.getAddressId().equals(a.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Address not found"));

        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        if (StringUtils.hasText(bindVo.getNote())) {
            order.setNote(SECKILL_ORDER_NOTE + " · " + bindVo.getNote().trim());
        }
        order.setModifyTime(new Date());
        orderRepository.save(order);
    }

    @Override
    public R cancelOrder(String orderSn, String cookieHeader) {
        OrderLoginUserTo loginUser = getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            return R.error(401, "Please login first");
        }
        if (!StringUtils.hasText(orderSn)) {
            return R.error(400, "orderSn required");
        }
        try {
            orderService.cancelUnpaidByMember(orderSn.trim(), loginUser.getUserId());
        } catch (IllegalArgumentException e) {
            return R.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            return R.error(400, e.getMessage());
        }
        return R.ok();
    }

    @Override
    public R notifyPaySuccess(String orderSn) {
        if (!StringUtils.hasText(orderSn)) {
            return R.error(400, "orderSn required");
        }
        try {
            orderService.markPaidByOrderSn(orderSn.trim());
        } catch (IllegalArgumentException e) {
            return R.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            return R.error(400, e.getMessage());
        }
        return R.ok();
    }

    private OrderLoginUserTo getLoginUser() {
        return OrderLoginInterceptor.THREAD_LOCAL.get();
    }

    private List<OrderConfirmItemVo> loadConfirmCartItems(String cookieHeader) {
        List<OrderConfirmItemVo> checkedItems = fetchCheckedCartItems(cookieHeader);
        refreshRealtimePrice(checkedItems);
        enrichStockStatus(checkedItems);
        return checkedItems;
    }

    private void applyConfirmAmounts(OrderConfirmVo confirmVo, OrderLoginUserTo loginUser, List<OrderConfirmItemVo> checkedItems) {
        BigDecimal total = checkedItems.stream()
                .map(OrderConfirmItemVo::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal freight = calculateFreight(total);
        int availableIntegration = Math.max(0, loginUser.getIntegration() == null ? 0 : loginUser.getIntegration());
        BigDecimal integrationAmount = BigDecimal.ZERO;
        confirmVo.setTotalAmount(total);
        confirmVo.setFreightAmount(freight);
        confirmVo.setIntegration(availableIntegration);
        confirmVo.setIntegrationAmount(integrationAmount);
        confirmVo.setPayPrice(total.add(freight).subtract(integrationAmount));
    }

    private void applyConfirmAddresses(OrderConfirmVo confirmVo, String cookieHeader, OrderLoginUserTo loginUser) {
        List<MemberAddressVo> addresses = fetchMemberAddresses(cookieHeader, loginUser.getUserId());
        confirmVo.setAddresses(addresses.isEmpty() ? buildFallbackAddressList() : addresses);
    }

    private void issueConfirmToken(OrderLoginUserTo loginUser, OrderConfirmVo confirmVo) {
        String token = UUID.randomUUID().toString().replace("-", "");
        confirmVo.setOrderToken(token);
        stringRedisTemplate.opsForValue().set(
                buildTokenKey(loginUser.getUserId(), token),
                confirmVo.getPayPrice().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                Duration.ofSeconds(orderTokenTtlSeconds)
        );
    }

    private OrderSubmitResponseVo fail(OrderSubmitResponseVo response, int code, String msg) {
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }

    private String consumeSubmitToken(OrderSubmitVo submitVo, Long userId) {
        if (submitVo == null || submitVo.getOrderToken() == null || submitVo.getOrderToken().isBlank()) {
            return null;
        }
        String tokenKey = buildTokenKey(userId, submitVo.getOrderToken().trim());
        return stringRedisTemplate.opsForValue().getAndDelete(tokenKey);
    }

    private List<OrderConfirmItemVo> loadCheckedSubmitItems(String cookieHeader) {
        List<OrderConfirmItemVo> checkedItems = fetchCheckedCartItems(cookieHeader);
        refreshRealtimePrice(checkedItems);
        return checkedItems;
    }

    private AmountSnapshot calcAndValidateSubmitAmount(
            OrderSubmitVo submitVo,
            String confirmPayPrice,
            List<OrderConfirmItemVo> checkedItems,
            OrderSubmitResponseVo response
    ) {
        BigDecimal total = checkedItems.stream()
                .map(OrderConfirmItemVo::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal freight = calculateFreight(total);
        BigDecimal payAmount = total.add(freight).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedPay = toBigDecimal(confirmPayPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal payDiff = payAmount.subtract(expectedPay).abs();
        if (payDiff.compareTo(PAY_AMOUNT_TOLERANCE) > 0) {
            if (log.isWarnEnabled()) {
                log.warn("order-pay-check failed token={} actualPay={} expectedPay={} diff={} tolerance={}",
                        submitVo == null ? null : submitVo.getOrderToken(), payAmount, expectedPay, payDiff, PAY_AMOUNT_TOLERANCE);
            }
            response.setCode(3);
            response.setMsg("Order price changed, please refresh confirm page");
            response.setPayAmount(payAmount);
            return null;
        }
        AmountSnapshot snapshot = new AmountSnapshot();
        snapshot.total = total;
        snapshot.freight = freight;
        snapshot.payAmount = payAmount;
        return snapshot;
    }

    private String buildOrderSn() {
        return "GL" + System.currentTimeMillis();
    }

    private OrderEntity buildOrderEntity(
            OrderSubmitVo submitVo,
            String cookieHeader,
            OrderLoginUserTo loginUser,
            String orderSn,
            AmountSnapshot amounts
    ) {
        OrderEntity order = new OrderEntity();
        order.setOrderSn(orderSn);
        order.setMemberId(loginUser.getUserId());
        order.setMemberUsername(loginUser.getUsername());
        order.setCreateTime(new Date());
        order.setModifyTime(new Date());
        order.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        order.setSourceType(0);
        order.setPayType(submitVo.getPayType() == null ? 1 : submitVo.getPayType());
        order.setTotalAmount(amounts.total);
        order.setPayAmount(amounts.payAmount);
        order.setFreightAmount(amounts.freight);
        order.setPromotionAmount(BigDecimal.ZERO);
        order.setIntegrationAmount(BigDecimal.ZERO);
        order.setCouponAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setDeleteStatus(0);
        order.setConfirmStatus(0);
        order.setUseIntegration(0);
        order.setNote(submitVo.getNote());
        applyAddress(order, submitVo.getAddressId(), cookieHeader, loginUser.getUserId());
        return order;
    }

    private List<OrderItemEntity> buildOrderItems(OrderEntity order, String orderSn, List<OrderConfirmItemVo> checkedItems) {
        List<OrderItemEntity> orderItems = new ArrayList<>();
        Map<Long, SkuOrderMeta> skuMetaMap = loadSkuOrderMeta(checkedItems);
        int orderGiftPoints = 0;
        for (OrderConfirmItemVo cartItem : checkedItems) {
            OrderItemEntity item = new OrderItemEntity();
            item.setOrderId(order.getId());
            item.setOrderSn(orderSn);
            item.setSkuId(cartItem.getSkuId());
            item.setSkuName(cartItem.getTitle());
            item.setSkuPic(cartItem.getImage());
            SkuOrderMeta meta = cartItem.getSkuId() == null ? null : skuMetaMap.get(cartItem.getSkuId());
            if (meta != null) {
                item.setSpuId(meta.spuId);
                item.setSpuName(meta.spuName);
                item.setSpuPic(meta.spuPic);
                item.setSpuBrand(meta.spuBrand);
                item.setCategoryId(meta.categoryId);
                if (item.getSkuPic() == null || item.getSkuPic().isBlank()) {
                    item.setSkuPic(meta.spuPic);
                }
            }
            item.setSkuPrice(cartItem.getPrice());
            item.setSkuQuantity(cartItem.getCount());
            item.setSkuAttrsVals(cartItem.getSkuAttr() == null ? "" : String.join(";", cartItem.getSkuAttr()));
            item.setPromotionAmount(BigDecimal.ZERO);
            item.setCouponAmount(BigDecimal.ZERO);
            item.setIntegrationAmount(BigDecimal.ZERO);
            item.setRealAmount(cartItem.getTotalPrice());
            int giftPoints = toPoints(cartItem.getTotalPrice());
            item.setGiftIntegration(giftPoints);
            item.setGiftGrowth(giftPoints);
            orderGiftPoints += giftPoints;
            orderItems.add(item);
        }
        order.setIntegration(orderGiftPoints);
        order.setGrowth(orderGiftPoints);
        return orderItems;
    }

    private List<OrderConfirmItemVo> fetchCheckedCartItems(String cookieHeader) {
        String url = cartServiceUrl + "/cart/current";
        HttpHeaders headers = new HttpHeaders();
        if (cookieHeader != null && !cookieHeader.isBlank()) {
            headers.set(HttpHeaders.COOKIE, cookieHeader);
        }
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return List.of();
        }

        try {
            Map<String, Object> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object dataObj = body.get("data");
            if (!(dataObj instanceof Map<?, ?> dataMap)) return List.of();
            Object itemsObj = dataMap.get("items");
            if (!(itemsObj instanceof List<?> rawItems)) return List.of();

            List<OrderConfirmItemVo> result = new ArrayList<>();
            for (Object raw : rawItems) {
                if (!(raw instanceof Map<?, ?> m)) continue;
                boolean checked = !Boolean.FALSE.equals(m.get("check"));
                if (!checked) continue;

                OrderConfirmItemVo item = new OrderConfirmItemVo();
                item.setSkuId(toLong(m.get("skuId")));
                item.setTitle(asString(m.get("title")));
                item.setImage(asString(m.get("image")));
                item.setCount(toInt(m.get("count"), 1));
                BigDecimal price = toBigDecimal(m.get("price"));
                BigDecimal lineTotal = m.get("totalPrice") == null
                        ? price.multiply(BigDecimal.valueOf(item.getCount()))
                        : toBigDecimal(m.get("totalPrice"));
                item.setPrice(price);
                item.setTotalPrice(lineTotal.setScale(2, RoundingMode.HALF_UP));
                item.setSkuAttr(toStringList(m.get("skuAttr")));
                result.add(item);
            }
            return result;
        } catch (Exception ignore) {
            return List.of();
        }
    }

    private void refreshRealtimePrice(List<OrderConfirmItemVo> items) {
        if (items == null || items.isEmpty()) return;
        for (OrderConfirmItemVo item : items) {
            if (item.getSkuId() == null) continue;
            BigDecimal latest = fetchLatestSkuPrice(item.getSkuId());
            if (latest == null || latest.compareTo(BigDecimal.ZERO) < 0) continue;
            item.setPrice(latest);
            int count = item.getCount() == null ? 1 : Math.max(1, item.getCount());
            item.setTotalPrice(latest.multiply(BigDecimal.valueOf(count)).setScale(2, RoundingMode.HALF_UP));
        }
    }

    /**
     * Real-time availability from ware ({@code wms_ware_sku} via {@code WareSkuRepository#getSkuStock}).
     */
    private void enrichStockStatus(List<OrderConfirmItemVo> items) {
        if (items == null || items.isEmpty()) return;
        Set<Long> skuIds = new LinkedHashSet<>();
        for (OrderConfirmItemVo item : items) {
            if (item.getSkuId() != null) {
                skuIds.add(item.getSkuId());
            }
        }
        if (skuIds.isEmpty()) return;

        Map<Long, Boolean> stockBySku = fetchHasStockBySkuIds(new ArrayList<>(skuIds));
        for (OrderConfirmItemVo item : items) {
            if (item.getSkuId() == null) {
                item.setHasStock(Boolean.FALSE);
                continue;
            }
            item.setHasStock(Boolean.TRUE.equals(stockBySku.get(item.getSkuId())));
        }
    }

    private Map<Long, Boolean> fetchHasStockBySkuIds(List<Long> skuIds) {
        String url = wareServiceUrl + "/waresku/hasStock";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(skuIds, headers),
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Map.of();
            }
            Map<String, Object> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object codeObj = body.get("code");
            int code = codeObj instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(codeObj));
            if (code != 0) {
                return Map.of();
            }
            Object dataObj = body.get("data");
            if (!(dataObj instanceof List<?> list)) {
                return Map.of();
            }
            Map<Long, Boolean> out = new HashMap<>();
            for (Object raw : list) {
                if (!(raw instanceof Map<?, ?> m)) continue;
                Long skuId = toLong(m.get("skuId"));
                if (skuId == null) continue;
                Object hs = m.get("hasStock");
                boolean ok = Boolean.TRUE.equals(hs) || "true".equalsIgnoreCase(String.valueOf(hs));
                out.put(skuId, ok);
            }
            return out;
        } catch (Exception ignore) {
            return Map.of();
        }
    }

    private StockLockOutcome lockOrderStock(String orderSn, List<OrderConfirmItemVo> items) {
        StockLockOutcome outcome = new StockLockOutcome();
        if (items == null || items.isEmpty()) return outcome;
        String url = wareServiceUrl + "/waresku/lock";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        List<Map<String, Object>> locks = new ArrayList<>();
        for (OrderConfirmItemVo item : items) {
            if (item.getSkuId() == null || item.getCount() == null || item.getCount() <= 0) continue;
            Map<String, Object> one = new HashMap<>();
            one.put("skuId", item.getSkuId());
            one.put("count", item.getCount());
            locks.add(one);
        }
        if (locks.isEmpty()) return outcome;
        Map<String, Object> req = new HashMap<>();
        req.put("orderSn", orderSn);
        req.put("locks", locks);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(req, headers),
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                outcome.lockApiError = true;
                if (log.isWarnEnabled()) {
                    log.warn("order-stock-lock failed: non-2xx or empty body. orderSn={} req={} status={}",
                            orderSn, req, response.getStatusCode());
                }
                return outcome;
            }
            Map<String, Object> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object codeObj = body.get("code");
            int code = codeObj instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(codeObj));
            if (code != 0) {
                if (log.isInfoEnabled()) {
                    log.info("order-stock-lock rejected by ware. orderSn={} req={} code={} msg={}",
                            orderSn, req, code, body.get("msg"));
                }
                return outcome;
            }
            outcome.taskId = toLong(body.get("taskId"));
            Object dataObj = body.get("data");
            if (!(dataObj instanceof List<?> list)) return outcome;
            List<LockedStockLine> result = new ArrayList<>();
            for (Object raw : list) {
                if (!(raw instanceof Map<?, ?> m)) continue;
                LockedStockLine line = new LockedStockLine();
                line.skuId = toLong(m.get("skuId"));
                line.wareId = toLong(m.get("wareId"));
                line.count = toInt(m.get("count"), 0);
                if (line.skuId != null && line.wareId != null && line.count > 0) {
                    result.add(line);
                }
            }
            outcome.lines = result;
            return outcome;
        } catch (Exception e) {
            outcome.lockApiError = true;
            if (log.isWarnEnabled()) {
                log.warn("order-stock-lock exception. orderSn={} req={}", orderSn, req, e);
            }
            return outcome;
        }
    }

    private void unlockOrderStock(Long taskId, List<LockedStockLine> locked) {
        String url = wareServiceUrl + "/waresku/unlock";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        List<Map<String, Object>> lockedPayload = new ArrayList<>();
        if (locked != null) {
            for (LockedStockLine line : locked) {
                if (line == null || line.skuId == null || line.wareId == null || line.count <= 0) continue;
                Map<String, Object> one = new HashMap<>();
                one.put("skuId", line.skuId);
                one.put("wareId", line.wareId);
                one.put("count", line.count);
                lockedPayload.add(one);
            }
        }
        Map<String, Object> req = new HashMap<>();
        if (taskId != null) {
            req.put("taskId", taskId);
        }
        req.put("locked", lockedPayload);
        try {
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(req, headers), String.class);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("order-stock-unlock failed, will rely on later compensation. msg={}", e.getMessage());
            }
        }
    }

    private BigDecimal fetchLatestSkuPrice(Long skuId) {
        String url = productServiceUrl + "/skuinfo/info/" + skuId;
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }
            Map<String, Object> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object skuInfoObj = body.get("skuInfo");
            if (!(skuInfoObj instanceof Map<?, ?> skuMap)) return null;
            return toBigDecimal(skuMap.get("price"));
        } catch (Exception ignore) {
            return null;
        }
    }

    private void clearSubmittedCartItems(String cookieHeader, List<OrderConfirmItemVo> items) {
        if (items.isEmpty()) return;
        for (OrderConfirmItemVo item : items) {
            if (item.getSkuId() == null) continue;
            String url = cartServiceUrl + "/cart/item/delete?skuId=" + item.getSkuId();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            if (cookieHeader != null && !cookieHeader.isBlank()) {
                headers.set(HttpHeaders.COOKIE, cookieHeader);
            }
            try {
                restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), String.class);
            } catch (Exception ignore) {
                // Cart cleanup failure should not rollback placed order.
            }
        }
    }

    private List<MemberAddressVo> buildFallbackAddressList() {
        MemberAddressVo address = new MemberAddressVo();
        address.setId(1L);
        address.setName("Default Receiver");
        address.setPhone("13800000000");
        address.setPostCode("100000");
        address.setProvince("Beijing");
        address.setCity("Beijing");
        address.setRegion("Chaoyang");
        address.setDetailAddress("No.1 Gulimall Road");
        address.setDefaultStatus(1);
        return List.of(address);
    }

    private String buildTokenKey(Long userId, String token) {
        long uid = userId == null ? 0L : userId;
        return ORDER_TOKEN_PREFIX + uid + ":" + token;
    }

    /**
     * Phase-1 freight rule:
     * - itemsTotal >= freeShippingThreshold => freight = 0
     * - otherwise freight = defaultFreight
     */
    private BigDecimal calculateFreight(BigDecimal itemsTotal) {
        BigDecimal total = itemsTotal == null ? BigDecimal.ZERO : itemsTotal;
        BigDecimal threshold = freeShippingThreshold == null ? BigDecimal.ZERO : freeShippingThreshold;
        BigDecimal baseFreight = defaultFreight == null ? BigDecimal.ZERO : defaultFreight;
        if (threshold.compareTo(BigDecimal.ZERO) > 0 && total.compareTo(threshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return baseFreight.max(BigDecimal.ZERO);
    }

    private List<MemberAddressVo> fetchMemberAddresses(String cookieHeader, Long loginUserId) {
        if (loginUserId == null) return List.of();
        String url = memberServiceUrl + "/memberreceiveaddress/list?page=1&limit=50";
        HttpHeaders headers = new HttpHeaders();
        if (cookieHeader != null && !cookieHeader.isBlank()) {
            headers.set(HttpHeaders.COOKIE, cookieHeader);
        }
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return List.of();
            }
            Map<String, Object> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object pageObj = body.get("page");
            if (!(pageObj instanceof Map<?, ?> pageMap)) return List.of();
            Object listObj = pageMap.get("list");
            if (!(listObj instanceof List<?> list)) return List.of();

            List<MemberAddressVo> result = new ArrayList<>();
            for (Object raw : list) {
                if (!(raw instanceof Map<?, ?> m)) continue;
                MemberAddressVo vo = new MemberAddressVo();
                vo.setId(toLong(m.get("id")));
                vo.setMemberId(toLong(m.get("memberId")));
                vo.setName(asString(m.get("name")));
                vo.setPhone(asString(m.get("phone")));
                vo.setPostCode(asString(m.get("postCode")));
                vo.setProvince(asString(m.get("province")));
                vo.setCity(asString(m.get("city")));
                vo.setRegion(asString(m.get("region")));
                vo.setDetailAddress(asString(m.get("detailAddress")));
                vo.setDefaultStatus(toInt(m.get("defaultStatus"), 0));
                if (loginUserId.equals(vo.getMemberId())) {
                    result.add(vo);
                }
            }
            return result;
        } catch (Exception ignore) {
            return List.of();
        }
    }

    private void applyAddress(OrderEntity order, Long addressId, String cookieHeader, Long loginUserId) {
        List<MemberAddressVo> addresses = fetchMemberAddresses(cookieHeader, loginUserId);
        if (addresses.isEmpty()) {
            addresses = buildFallbackAddressList();
        }
        MemberAddressVo address = addresses.stream()
                .filter(a -> addressId == null || addressId.equals(a.getId()))
                .findFirst()
                .orElse(addresses.get(0));
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
    }

    private static boolean isSeckillOrder(OrderEntity order) {
        if (order == null) {
            return false;
        }
        if (StringUtils.hasText(order.getNote()) && order.getNote().trim().startsWith(SECKILL_ORDER_NOTE)) {
            return true;
        }
        return StringUtils.hasText(order.getMemberUsername())
                && order.getMemberUsername().trim().startsWith("seckill-");
    }

    private static boolean needsShippingAddress(OrderEntity order) {
        return !StringUtils.hasText(trimToNull(order.getReceiverName()))
                || !StringUtils.hasText(trimToNull(order.getReceiverPhone()))
                || !StringUtils.hasText(trimToNull(order.getReceiverDetailAddress()));
    }

    private OrderDetailItemVo toSeckillConfirmItem(OrderItemEntity item) {
        OrderDetailItemVo vo = new OrderDetailItemVo();
        vo.setSkuId(item.getSkuId());
        vo.setSkuName(firstNonBlank(item.getSkuName(), item.getSpuName(), "Product"));
        vo.setSkuPic(firstNonBlank(item.getSkuPic(), item.getSpuPic()));
        vo.setSkuPrice(item.getSkuPrice());
        vo.setSkuQuantity(item.getSkuQuantity());
        vo.setRealAmount(item.getRealAmount());
        vo.setSpuName(item.getSpuName());
        return vo;
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private static String firstNonBlank(String... values) {
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

    private Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        if (val == null) return null;
        try {
            return Long.parseLong(String.valueOf(val));
        } catch (Exception ignore) {
            return null;
        }
    }

    private Integer toInt(Object val, int fallback) {
        if (val instanceof Number n) return n.intValue();
        if (val == null) return fallback;
        try {
            return Integer.parseInt(String.valueOf(val));
        } catch (Exception ignore) {
            return fallback;
        }
    }

    private String asString(Object val) {
        return val == null ? "" : String.valueOf(val);
    }

    /**
     * Enrich order-item SPU fields by skuId:
     * - skuinfo/info/{skuId} -> spuId, catalogId, brandId, skuDefaultImg
     * - spuinfo/info/{spuId} -> spuName
     * - brand/info/{brandId} -> brand name
     */
    private Map<Long, SkuOrderMeta> loadSkuOrderMeta(List<OrderConfirmItemVo> items) {
        if (items == null || items.isEmpty()) return Map.of();
        Set<Long> skuIds = new LinkedHashSet<>();
        for (OrderConfirmItemVo item : items) {
            if (item.getSkuId() != null) skuIds.add(item.getSkuId());
        }
        if (skuIds.isEmpty()) return Map.of();
        return fetchSkuMetaBatch(new ArrayList<>(skuIds));
    }

    private Map<Long, SkuOrderMeta> fetchSkuMetaBatch(List<Long> skuIds) {
        String url = productServiceUrl + "/skuinfo/order/meta";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(skuIds, headers),
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Map.of();
            }
            Map<String, Object> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object codeObj = body.get("code");
            int code = codeObj instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(codeObj));
            if (code != 0) {
                return Map.of();
            }
            Object dataObj = body.get("data");
            if (!(dataObj instanceof List<?> list)) {
                return Map.of();
            }
            Map<Long, SkuOrderMeta> out = new HashMap<>();
            for (Object raw : list) {
                if (!(raw instanceof Map<?, ?> m)) continue;
                Long skuId = toLong(m.get("skuId"));
                if (skuId == null) continue;
                SkuOrderMeta meta = new SkuOrderMeta();
                meta.spuId = toLong(m.get("spuId"));
                meta.spuName = asString(m.get("spuName"));
                meta.spuPic = asString(m.get("spuPic"));
                meta.spuBrand = asString(m.get("spuBrand"));
                meta.categoryId = toLong(m.get("categoryId"));
                out.put(skuId, meta);
            }
            return out;
        } catch (Exception ignore) {
            return Map.of();
        }
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(String.valueOf(val));
        } catch (Exception ignore) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Rule: earned points equals item amount (floor, non-negative integer).
     */
    private int toPoints(BigDecimal amount) {
        if (amount == null) return 0;
        return amount.max(BigDecimal.ZERO).setScale(0, RoundingMode.DOWN).intValue();
    }

    private List<String> toStringList(Object val) {
        if (!(val instanceof List<?> list)) return List.of();
        List<String> result = new ArrayList<>();
        for (Object one : list) {
            if (one != null) result.add(String.valueOf(one));
        }
        return result;
    }

    private static final class SkuOrderMeta {
        private Long spuId;
        private String spuName;
        private String spuPic;
        private String spuBrand;
        private Long categoryId;
    }

    private static final class StockLockOutcome {
        private Long taskId;
        private List<LockedStockLine> lines = List.of();
        private boolean lockApiError;
    }

    private static final class LockedStockLine {
        private Long skuId;
        private Long wareId;
        private int count;
    }

    private static final class AmountSnapshot {
        private BigDecimal total;
        private BigDecimal freight;
        private BigDecimal payAmount;
    }
}
