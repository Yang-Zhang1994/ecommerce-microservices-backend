package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.enums.OrderStatusEnum;
import com.atguigu.gulimall.order.repository.OrderRepository;
import com.atguigu.gulimall.order.repository.PaymentInfoRepository;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.service.StripePaymentService;
import com.atguigu.gulimall.order.service.StripeWebhookStoreService;
import com.atguigu.gulimall.order.tracing.OrderTraceAttributes;
import com.atguigu.gulimall.order.entity.StripeWebhookEventEntity;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StripePaymentServiceImpl implements StripePaymentService {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentServiceImpl.class);

    private static final String STRIPE_CHECKOUT_URL = "https://api.stripe.com/v1/checkout/sessions";

    /** Stripe Checkout Session ids returned by {@code /v1/checkout/sessions}. */
    private static final Pattern STRIPE_CHECKOUT_SESSION_ID =
            Pattern.compile("cs_(?:test|live)_[A-Za-z0-9]+");

    /**
     * Stripe Checkout Session {@code expires_at} must be between 30 minutes and 24 hours from creation.
     *
     * @see <a href="https://docs.stripe.com/api/checkout/sessions/create#create_checkout_session-expires_at">Stripe API</a>
     */
    private static final int STRIPE_CHECKOUT_MIN_EXPIRY_MINUTES = 30;

    private static final int STRIPE_CHECKOUT_MAX_EXPIRY_MINUTES = 24 * 60;

    private static final Duration STRIPE_HTTP_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration STRIPE_HTTP_REQUEST_TIMEOUT = Duration.ofSeconds(45);

    private final OrderRepository orderRepository;
    private final PaymentInfoRepository paymentInfoRepository;
    private final OrderService orderService;
    private final StripeWebhookStoreService webhookStoreService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient =
            HttpClient.newBuilder().connectTimeout(STRIPE_HTTP_CONNECT_TIMEOUT).build();

    @Value("${order.stripe.secret-key:}")
    private String stripeSecretKey;
    @Value("${order.stripe.webhook-secret:}")
    private String stripeWebhookSecret;
    @Value("${order.stripe.currency:cad}")
    private String stripeCurrency;
    /** Dev default localhost:3001; prod set {@code GULIMALL_MALL_PUBLIC_ORIGIN=https://www.example.com} */
    @Value("${order.mall-public-origin:http://localhost:3001}")
    private String mallPublicOrigin;
    /** If empty, success URL is derived from mall-public-origin. */
    @Value("${order.stripe.success-url:}")
    private String stripeSuccessUrlOverride;
    /** If empty, cancel URL is derived from mall-public-origin. */
    @Value("${order.stripe.cancel-url:}")
    private String stripeCancelUrlOverride;

    /** Aligns Checkout Session expiry with unpaid-order auto-close ({@code order.pay-timeout-minutes}). */
    @Value("${order.pay-timeout-minutes:30}")
    private int payTimeoutMinutes;

    public StripePaymentServiceImpl(
            OrderRepository orderRepository,
            PaymentInfoRepository paymentInfoRepository,
            OrderService orderService,
            StripeWebhookStoreService webhookStoreService,
            ObjectMapper objectMapper
    ) {
        this.orderRepository = orderRepository;
        this.paymentInfoRepository = paymentInfoRepository;
        this.orderService = orderService;
        this.webhookStoreService = webhookStoreService;
        this.objectMapper = objectMapper;
    }

    @Override
    public StripeCheckoutSession createCheckoutSession(String orderSn, Long memberId) {
        if (!StringUtils.hasText(orderSn)) {
            throw new IllegalArgumentException("orderSn required");
        }
        requireStripeSecretKey();

        OrderEntity order = orderRepository.findByOrderSn(orderSn.trim())
                .orElseThrow(() -> new IllegalStateException("Order not found"));
        if (memberId != null && order.getMemberId() != null && !memberId.equals(order.getMemberId())) {
            throw new IllegalStateException("Order not found");
        }
        if (order.getPaymentTime() != null) {
            throw new IllegalStateException("Order already paid");
        }
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.CREATE_NEW.getCode())) {
            throw new IllegalStateException("Order is not awaiting payment (closed or completed)");
        }

        long amountCents = toCents(order.getPayAmount());
        String currency = StringUtils.hasText(stripeCurrency) ? stripeCurrency.trim().toLowerCase(Locale.ROOT) : "cad";
        String cleanSn = order.getOrderSn().trim();

        String form = buildFormBody(cleanSn, amountCents, currency);
        String respJson = createStripeSession(form);

        try {
            JsonNode root = objectMapper.readTree(respJson);
            String sessionId = root.path("id").asText(null);
            String checkoutUrl = root.path("url").asText(null);
            if (!StringUtils.hasText(sessionId) || !StringUtils.hasText(checkoutUrl)) {
                throw new IllegalStateException("Stripe checkout session create failed");
            }
            OrderTraceAttributes.setStripeCheckoutContext(cleanSn, sessionId, memberId);
            upsertPaymentInit(order, sessionId);
            return new StripeCheckoutSession(sessionId, checkoutUrl);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Parse stripe checkout response failed", e);
        }
    }

    @Override
    public OrderEntity verifyPaidCheckoutSession(String checkoutSessionId, String expectedOrderSn) {
        if (!StringUtils.hasText(checkoutSessionId) || !StringUtils.hasText(expectedOrderSn)) {
            return null;
        }
        requireStripeSecretKey();
        try {
            String json = retrieveStripeCheckoutSessionJson(checkoutSessionId.trim());
            JsonNode root = objectMapper.readTree(json);
            if (root.has("error")) {
                return null;
            }
            String paymentStatus = root.path("payment_status").asText("");
            if (!"paid".equalsIgnoreCase(paymentStatus)) {
                return null;
            }
            String orderSn = firstText(
                    root.path("metadata").path("orderSn").asText(null),
                    root.path("client_reference_id").asText(null)
            );
            if (!expectedOrderSn.trim().equals(orderSn)) {
                return null;
            }
            OrderEntity order = orderRepository.findByOrderSn(orderSn.trim()).orElse(null);
            if (order == null) {
                return null;
            }
            // Same truth as webhook: paid in Stripe → persist paid order even if webhook is delayed/missing.
            if (order.getPaymentTime() != null
                    || Objects.equals(order.getStatus(), OrderStatusEnum.PAYED.getCode())) {
                return order;
            }
            if (!Objects.equals(order.getStatus(), OrderStatusEnum.CREATE_NEW.getCode())) {
                return null;
            }
            long amountTotal = root.path("amount_total").asLong(0L);
            if (amountTotal > 0) {
                long expected = toCents(order.getPayAmount());
                if (expected != amountTotal) {
                    return null;
                }
            }
            String paymentIntentId = root.path("payment_intent").asText(null);
            String sessionIdFromStripe = root.path("id").asText(checkoutSessionId.trim());
            String currency = root.path("currency").asText(null);
            upsertPaymentSuccess(order, paymentIntentId, sessionIdFromStripe, amountTotal, currency, json);
            OrderTraceAttributes.setPostPayVerifyContext(order.getOrderSn(), sessionIdFromStripe);
            orderService.markPaidByOrderSn(order.getOrderSn());
            return orderRepository.findByOrderSn(orderSn.trim()).orElse(order);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void expireOpenCheckoutSessionForOrder(String orderSn) {
        if (!StringUtils.hasText(orderSn) || !StringUtils.hasText(stripeSecretKey)) {
            return;
        }
        Optional<PaymentInfoEntity> row = paymentInfoRepository.findByOrderSn(orderSn.trim());
        if (row.isEmpty()) {
            return;
        }
        String sessionId = extractStripeCheckoutSessionId(row.get());
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        String sid = sessionId.trim();
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(STRIPE_CHECKOUT_URL + "/" + sid + "/expire"))
                    .header("Authorization", "Bearer " + stripeSecretKey.trim())
                    .timeout(STRIPE_HTTP_REQUEST_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int code = resp.statusCode();
            if (code >= 200 && code < 300) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug(
                        "stripe expire session non-success orderSn={} sessionId={} http={} body={}",
                        orderSn,
                        sid,
                        code,
                        truncate(resp.body(), 500));
            }
        } catch (Exception e) {
            log.warn("stripe expire session failed orderSn={} sessionId={}", orderSn, sid, e);
        }
    }

    /**
     * Prefer {@code stripe_session_id=} from {@link PaymentInfoEntity#getCallbackContent()} (INIT row),
     * else {@code cs_} embedded in callback JSON, else {@link PaymentInfoEntity#getAlipayTradeNo()}.
     */
    private static String extractStripeCheckoutSessionId(PaymentInfoEntity p) {
        if (p == null) {
            return null;
        }
        String cb = p.getCallbackContent();
        if (StringUtils.hasText(cb)) {
            String marker = "stripe_session_id=";
            int i = cb.indexOf(marker);
            if (i >= 0) {
                String rest = cb.substring(i + marker.length());
                String token = rest.split("[&\\s\"<]", 2)[0];
                if (StringUtils.hasText(token) && token.startsWith("cs_")) {
                    return token.trim();
                }
            }
            Matcher m = STRIPE_CHECKOUT_SESSION_ID.matcher(cb);
            String last = null;
            while (m.find()) {
                last = m.group();
            }
            if (StringUtils.hasText(last)) {
                return last;
            }
        }
        String alipay = p.getAlipayTradeNo();
        if (StringUtils.hasText(alipay) && alipay.startsWith("cs_")) {
            return alipay.trim();
        }
        return null;
    }

    private String retrieveStripeCheckoutSessionJson(String sessionId) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(STRIPE_CHECKOUT_URL + "/" + sessionId.trim()))
                .header("Authorization", "Bearer " + stripeSecretKey.trim())
                .timeout(STRIPE_HTTP_REQUEST_TIMEOUT)
                .GET()
                .build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IllegalStateException("stripe retrieve session failed status=" + resp.statusCode());
        }
        return resp.body();
    }

    @Override
    public void handleWebhook(String payload, String signatureHeader) {
        if (!StringUtils.hasText(payload)) {
            return;
        }
        requireStripeWebhookSecret();
        try {
            Event event = Webhook.constructEvent(payload, signatureHeader, stripeWebhookSecret.trim());
            OrderTraceAttributes.setStripeWebhookReceived(event.getId());
            if (webhookStoreService.isAlreadyProcessed(event.getId())) {
                OrderTraceAttributes.markStripeWebhookDuplicate();
                log.info("Stripe webhook already processed eventId={}", event.getId());
                return;
            }
            StripeWebhookEventEntity row = webhookStoreService.saveReceived(payload);
            processCheckoutCompleted(payload, row);
        } catch (SignatureVerificationException e) {
            throw new IllegalStateException("Stripe signature verification failed", e);
        }
    }

    @Override
    public void replayStoredWebhook(String stripeEventId) {
        if (!StringUtils.hasText(stripeEventId)) {
            throw new IllegalArgumentException("eventId required");
        }
        StripeWebhookEventEntity row = webhookStoreService.findByEventId(stripeEventId.trim())
                .orElseThrow(() -> new IllegalStateException("Webhook event not found"));
        processCheckoutCompleted(row.getPayload(), row);
    }

    private void processCheckoutCompleted(String payload, StripeWebhookEventEntity row) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventType = root.path("type").asText("");
            if (!"checkout.session.completed".equals(eventType)) {
                webhookStoreService.markSkipped(row, "ignored event type: " + eventType);
                return;
            }
            JsonNode session = root.path("data").path("object");
            String paymentStatus = session.path("payment_status").asText("");
            if (!"paid".equalsIgnoreCase(paymentStatus)) {
                webhookStoreService.markSkipped(row, "payment_status=" + paymentStatus);
                return;
            }
            String orderSn = firstText(
                    session.path("metadata").path("orderSn").asText(null),
                    session.path("client_reference_id").asText(null)
            );
            if (!StringUtils.hasText(orderSn)) {
                webhookStoreService.markSkipped(row, "missing orderSn");
                return;
            }
            String paymentIntentId = session.path("payment_intent").asText(null);
            String sessionId = session.path("id").asText(null);
            long amountTotal = session.path("amount_total").asLong(0L);
            String currency = session.path("currency").asText(null);

            OrderEntity order = orderRepository.findByOrderSn(orderSn.trim()).orElse(null);
            if (order == null) {
                webhookStoreService.markSkipped(row, "order not found");
                return;
            }
            if (amountTotal > 0) {
                long expected = toCents(order.getPayAmount());
                if (expected != amountTotal) {
                    throw new IllegalStateException("stripe amount mismatch");
                }
            }
            upsertPaymentSuccess(order, paymentIntentId, sessionId, amountTotal, currency, payload);
            OrderTraceAttributes.setStripeWebhookPayContext(
                    order.getOrderSn(), sessionId, root.path("id").asText(null));
            orderService.markPaidByOrderSn(order.getOrderSn());
            webhookStoreService.markProcessed(row);
        } catch (IllegalStateException e) {
            webhookStoreService.markFailed(row, e.getMessage());
            throw e;
        } catch (Exception e) {
            webhookStoreService.markFailed(row, e.getMessage());
            throw new IllegalStateException("Stripe webhook handle failed", e);
        }
    }

    private void requireStripeSecretKey() {
        if (!StringUtils.hasText(stripeSecretKey)) {
            throw new IllegalStateException("Stripe secret key is not configured");
        }
    }

    private void requireStripeWebhookSecret() {
        if (!StringUtils.hasText(stripeWebhookSecret)) {
            throw new IllegalStateException("Stripe webhook secret is not configured");
        }
    }

    private String createStripeSession(String formBody) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(STRIPE_CHECKOUT_URL))
                    .header("Authorization", "Bearer " + stripeSecretKey.trim())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(STRIPE_HTTP_REQUEST_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return resp.body();
            }
            throw new IllegalStateException("Stripe create session failed status=" + resp.statusCode() + " body=" + resp.body());
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Call stripe create session failed", e);
        }
    }

    private String buildFormBody(String orderSn, long amountCents, String currency) {
        List<String> params = new ArrayList<>();
        params.add(enc("mode") + "=" + enc("payment"));
        params.add(enc("client_reference_id") + "=" + enc(orderSn));
        params.add(enc("success_url") + "=" + encodeStripeRedirectUrl(appendOrderSn(stripeSuccessUrl(), orderSn)));
        params.add(enc("cancel_url") + "=" + encodeStripeRedirectUrl(appendOrderSn(stripeCancelUrl(), orderSn)));
        params.add(enc("payment_method_types[0]") + "=" + enc("card"));
        params.add(enc("line_items[0][quantity]") + "=" + enc("1"));
        params.add(enc("line_items[0][price_data][currency]") + "=" + enc(currency));
        params.add(enc("line_items[0][price_data][unit_amount]") + "=" + enc(String.valueOf(amountCents)));
        params.add(enc("line_items[0][price_data][product_data][name]") + "=" + enc("Order " + orderSn));
        params.add(enc("metadata[orderSn]") + "=" + enc(orderSn));
        long expiresAtEpoch = checkoutSessionExpiresAtEpochSeconds();
        params.add(enc("expires_at") + "=" + enc(String.valueOf(expiresAtEpoch)));
        return String.join("&", params);
    }

    /**
     * Minutes until session expiry: {@link #payTimeoutMinutes}, clamped to Stripe's 30m–24h window.
     */
    private int checkoutSessionExpiryMinutes() {
        int configured = Math.max(1, payTimeoutMinutes);
        return Math.min(
                STRIPE_CHECKOUT_MAX_EXPIRY_MINUTES,
                Math.max(STRIPE_CHECKOUT_MIN_EXPIRY_MINUTES, configured));
    }

    private long checkoutSessionExpiresAtEpochSeconds() {
        return Instant.now().plus(Duration.ofMinutes(checkoutSessionExpiryMinutes())).getEpochSecond();
    }

    private String stripeSuccessUrl() {
        if (StringUtils.hasText(stripeSuccessUrlOverride)) {
            return stripeSuccessUrlOverride.trim();
        }
        return normalizeOrigin(mallPublicOrigin) + "/order/success?paid=1";
    }

    private String stripeCancelUrl() {
        if (StringUtils.hasText(stripeCancelUrlOverride)) {
            return stripeCancelUrlOverride.trim();
        }
        return normalizeOrigin(mallPublicOrigin) + "/order/pay?cancelled=1";
    }

    private static String normalizeOrigin(String origin) {
        if (!StringUtils.hasText(origin)) {
            return "http://localhost:3001";
        }
        String o = origin.trim();
        while (o.endsWith("/")) {
            o = o.substring(0, o.length() - 1);
        }
        return o;
    }

    private String appendOrderSn(String baseUrl, String orderSn) {
        String withOrderSn;
        if (!StringUtils.hasText(baseUrl)) {
            String s = stripeSuccessUrl();
            withOrderSn = s.contains("?") ? s + "&orderSn=" + enc(orderSn) : s + "?orderSn=" + enc(orderSn);
        } else {
            withOrderSn = baseUrl.contains("?")
                    ? baseUrl + "&orderSn=" + enc(orderSn)
                    : baseUrl + "?orderSn=" + enc(orderSn);
        }
        if (withOrderSn.contains("{CHECKOUT_SESSION_ID}")) {
            return withOrderSn;
        }
        return withOrderSn + "&session_id={CHECKOUT_SESSION_ID}";
    }

    /** Stripe replaces {@code {CHECKOUT_SESSION_ID}} on redirect; keep literal after URL-encoding the rest. */
    private String encodeStripeRedirectUrl(String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            return "";
        }
        try {
            String encFull = URLEncoder.encode(rawUrl, StandardCharsets.UTF_8);
            return encFull.replace("%7BCHECKOUT_SESSION_ID%7D", "{CHECKOUT_SESSION_ID}");
        } catch (Exception e) {
            return enc(rawUrl);
        }
    }

    private String enc(String val) {
        return UriUtils.encodeQueryParam(val == null ? "" : val, StandardCharsets.UTF_8);
    }

    private long toCents(BigDecimal amount) {
        BigDecimal val = amount == null ? BigDecimal.ZERO : amount;
        return val.max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();
    }

    private void upsertPaymentInit(OrderEntity order, String sessionId) {
        PaymentInfoEntity row = paymentInfoRepository.findByOrderSn(order.getOrderSn())
                .orElseGet(PaymentInfoEntity::new);
        Date now = new Date();
        if (row.getId() == null) {
            row.setCreateTime(now);
        }
        row.setOrderSn(order.getOrderSn());
        row.setOrderId(order.getId());
        row.setTotalAmount(order.getPayAmount());
        row.setSubject("Order " + order.getOrderSn());
        row.setPaymentStatus("INIT");
        row.setCallbackTime(now);
        row.setCallbackContent("stripe_session_id=" + sessionId);
        paymentInfoRepository.save(row);
    }

    private void upsertPaymentSuccess(
            OrderEntity order,
            String paymentIntentId,
            String sessionId,
            long amountTotal,
            String currency,
            String payload
    ) {
        PaymentInfoEntity row = paymentInfoRepository.findByOrderSn(order.getOrderSn())
                .orElseGet(PaymentInfoEntity::new);
        Date now = new Date();
        if (row.getId() == null) {
            row.setCreateTime(now);
        }
        row.setOrderSn(order.getOrderSn());
        row.setOrderId(order.getId());
        row.setTotalAmount(amountTotal > 0 ? BigDecimal.valueOf(amountTotal).movePointLeft(2) : order.getPayAmount());
        row.setSubject("Order " + order.getOrderSn() + " (" + firstText(currency, "cad") + ")");
        row.setPaymentStatus("SUCCEEDED");
        row.setAlipayTradeNo(firstText(paymentIntentId, sessionId));
        row.setConfirmTime(now);
        row.setCallbackTime(now);
        row.setCallbackContent(truncate(payload, 4000));
        paymentInfoRepository.save(row);
    }

    private String firstText(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (StringUtils.hasText(v)) return v.trim();
        }
        return null;
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
