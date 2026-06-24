package com.atguigu.gulimall.order.tracing;

import io.opentelemetry.api.trace.Span;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

public final class OrderTraceAttributes {

    private OrderTraceAttributes() {
    }

    public static void setOrderSn(String orderSn) {
        if (!StringUtils.hasText(orderSn)) {
            return;
        }
        Span.current().setAttribute("order.sn", orderSn.trim());
    }

    public static void setSubmitOrderContext(String orderSn, Long memberId, BigDecimal payAmount) {
        setOrderSn(orderSn);
        if (memberId != null) {
            Span.current().setAttribute("order.member_id", memberId);
        }
        if (payAmount != null) {
            Span.current().setAttribute("order.pay_amount", payAmount.doubleValue());
        }
    }

    /** Trace #2: Stripe Checkout Session creation. */
    public static void setStripeCheckoutContext(String orderSn, String sessionId, Long memberId) {
        setOrderSn(orderSn);
        setStripeSessionId(sessionId);
        if (memberId != null) {
            Span.current().setAttribute("order.member_id", memberId);
        }
    }

    /** Webhook HTTP entry: tag event id before idempotency / business logic. */
    public static void setStripeWebhookReceived(String eventId) {
        setStripeEventId(eventId);
        Span.current().setAttribute("order.pay_path", "webhook");
    }

    /** Stripe at-least-once retry hit processed event_id; span stays short, no markPaid. */
    public static void markStripeWebhookDuplicate() {
        Span.current().setAttribute("stripe.webhook.duplicate", true);
    }

    /** Trace #3a: Stripe webhook confirms payment (enriches entry span on success path). */
    public static void setStripeWebhookPayContext(String orderSn, String sessionId, String eventId) {
        setOrderSn(orderSn);
        setStripeSessionId(sessionId);
        setStripeEventId(eventId);
        Span.current().setAttribute("order.pay_path", "webhook");
    }

    /** Trace #3b: Mall success page verifies Checkout Session after redirect. */
    public static void setPostPayVerifyContext(String orderSn, String sessionId) {
        setOrderSn(orderSn);
        setStripeSessionId(sessionId);
        Span.current().setAttribute("order.pay_path", "post_pay_verify");
    }

    private static void setStripeSessionId(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        Span.current().setAttribute("stripe.session_id", sessionId.trim());
    }

    private static void setStripeEventId(String eventId) {
        if (!StringUtils.hasText(eventId)) {
            return;
        }
        Span.current().setAttribute("stripe.event_id", eventId.trim());
    }
}
