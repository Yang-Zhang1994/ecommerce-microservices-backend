package com.atguigu.gulimall.order.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.interceptor.OrderLoginInterceptor;
import com.atguigu.gulimall.order.service.IdempotencyService;
import com.atguigu.gulimall.order.service.StripePaymentService;
import com.atguigu.gulimall.order.to.OrderLoginUserTo;
import com.atguigu.gulimall.order.vo.StripeCheckoutSessionRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import java.util.Map;

@RestController
@RequestMapping("order/pay/stripe")
public class StripePaymentController {

    private final StripePaymentService stripePaymentService;
    private final IdempotencyService idempotencyService;

    public StripePaymentController(
            StripePaymentService stripePaymentService,
            IdempotencyService idempotencyService) {
        this.stripePaymentService = stripePaymentService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping("/checkout-session")
    public R createCheckoutSession(
            @RequestBody StripeCheckoutSessionRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (req == null || !StringUtils.hasText(req.getOrderSn())) {
            return R.error(400, "orderSn required");
        }
        OrderLoginUserTo loginUser = OrderLoginInterceptor.THREAD_LOCAL.get();
        Long memberId = loginUser == null ? null : loginUser.getUserId();
        String orderSn = req.getOrderSn().trim();
        Map<String, Object> data = idempotencyService.execute(
                "stripe-checkout:" + orderSn,
                idempotencyKey,
                Map.class,
                () -> {
                    StripePaymentService.StripeCheckoutSession session =
                            stripePaymentService.createCheckoutSession(orderSn, memberId);
                    return Map.of(
                            "sessionId", session.sessionId(),
                            "checkoutUrl", session.checkoutUrl());
                });
        return R.ok().put("data", data);
    }

    @PostMapping(
            value = "/webhook",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE}
    )
    public R webhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signatureHeader
    ) {
        stripePaymentService.handleWebhook(payload, signatureHeader);
        // Stripe treats any 2xx as acknowledged.
        return R.ok();
    }
}
