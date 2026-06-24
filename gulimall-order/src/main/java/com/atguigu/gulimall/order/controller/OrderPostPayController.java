package com.atguigu.gulimall.order.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.service.PostPayMemberService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * After Stripe redirects back, exposes member hint verified via Stripe API so the mall header can
 * populate {@code gulimall_session} without calling {@code /order/list} (login interceptor).
 */
@RestController
@RequestMapping("order")
public class OrderPostPayController {

    private final PostPayMemberService postPayMemberService;

    public OrderPostPayController(PostPayMemberService postPayMemberService) {
        this.postPayMemberService = postPayMemberService;
    }

    @GetMapping("/post-pay/member")
    public R memberAfterStripe(
            @RequestParam("orderSn") String orderSn,
            @RequestParam("session_id") String sessionId
    ) {
        if (!StringUtils.hasText(orderSn) || !StringUtils.hasText(sessionId)) {
            return R.error(400, "orderSn and session_id required");
        }
        return postPayMemberService
                .memberProfileAfterStripeCheckout(orderSn.trim(), sessionId.trim())
                .map(m -> R.ok().put("member", m))
                .orElseGet(() -> R.error(404, "Invalid or unpaid checkout session, or no member context"));
    }
}
