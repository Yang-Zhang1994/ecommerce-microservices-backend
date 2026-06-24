package com.atguigu.gulimall.order.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.service.StripePaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin replay for failed Stripe webhooks (protect with shared secret).
 */
@RestController
@RequestMapping("order/pay/stripe")
public class StripeWebhookAdminController {

    private final StripePaymentService stripePaymentService;

    @Value("${order.stripe.webhook-replay-secret:}")
    private String webhookReplaySecret;

    public StripeWebhookAdminController(StripePaymentService stripePaymentService) {
        this.stripePaymentService = stripePaymentService;
    }

    @PostMapping("/webhook/replay/{eventId}")
    public R replay(
            @PathVariable("eventId") String eventId,
            @RequestHeader(value = "X-Webhook-Replay-Secret", required = false) String secret) {
        if (!StringUtils.hasText(webhookReplaySecret)) {
            return R.error(503, "Webhook replay is not configured");
        }
        if (!webhookReplaySecret.trim().equals(secret)) {
            return R.error(403, "Forbidden");
        }
        stripePaymentService.replayStoredWebhook(eventId);
        return R.ok().put("eventId", eventId);
    }
}
