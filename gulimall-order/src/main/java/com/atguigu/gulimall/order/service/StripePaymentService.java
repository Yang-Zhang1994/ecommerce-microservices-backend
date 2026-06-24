package com.atguigu.gulimall.order.service;

import com.atguigu.gulimall.order.entity.OrderEntity;

public interface StripePaymentService {

    /**
     * Creates a Stripe Checkout Session for a pending order.
     */
    StripeCheckoutSession createCheckoutSession(String orderSn, Long memberId);

    /**
     * Handles Stripe webhook payload with signature validation.
     */
    void handleWebhook(String payload, String signatureHeader);

    /**
     * Re-process a stored webhook payload (admin replay; skips Stripe signature).
     */
    void replayStoredWebhook(String stripeEventId);

    /**
     * Verifies a Stripe Checkout Session is paid and matches {@code orderSn} (metadata / client_reference_id).
     * Used after redirect so the mall can show member info without calling protected {@code /order/list}.
     */
    OrderEntity verifyPaidCheckoutSession(String checkoutSessionId, String expectedOrderSn);

    /**
     * Best-effort: expire an open Stripe Checkout Session so the customer cannot pay after timeout/cancel.
     * Ignores missing session id, network errors, or Stripe errors (already paid/expired).
     */
    void expireOpenCheckoutSessionForOrder(String orderSn);

    record StripeCheckoutSession(String sessionId, String checkoutUrl) {}
}
