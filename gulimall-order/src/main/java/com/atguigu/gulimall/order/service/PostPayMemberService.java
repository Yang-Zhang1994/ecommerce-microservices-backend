package com.atguigu.gulimall.order.service;

import java.util.Map;
import java.util.Optional;

/**
 * Builds mall-facing member profile after Stripe redirects back (verified Checkout Session).
 */
public interface PostPayMemberService {

    /**
     * Verifies the Stripe session is paid and matches {@code orderSn}, then resolves username/nickname
     * (preferring member-service when {@code memberId} is present).
     */
    Optional<Map<String, Object>> memberProfileAfterStripeCheckout(String orderSn, String sessionId);
}
