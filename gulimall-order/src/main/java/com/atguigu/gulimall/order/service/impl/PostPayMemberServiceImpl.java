package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.client.MemberProfileHttpClient;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.PostPayMemberService;
import com.atguigu.gulimall.order.service.StripePaymentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PostPayMemberServiceImpl implements PostPayMemberService {

    private final StripePaymentService stripePaymentService;
    private final MemberProfileHttpClient memberProfileHttpClient;

    public PostPayMemberServiceImpl(
            StripePaymentService stripePaymentService,
            MemberProfileHttpClient memberProfileHttpClient
    ) {
        this.stripePaymentService = stripePaymentService;
        this.memberProfileHttpClient = memberProfileHttpClient;
    }

    @Override
    public Optional<Map<String, Object>> memberProfileAfterStripeCheckout(String orderSn, String sessionId) {
        if (!StringUtils.hasText(orderSn) || !StringUtils.hasText(sessionId)) {
            return Optional.empty();
        }
        OrderEntity order =
                stripePaymentService.verifyPaidCheckoutSession(sessionId.trim(), orderSn.trim());
        if (order == null) {
            return Optional.empty();
        }
        Long mid = order.getMemberId();
        String fallbackUsername = order.getMemberUsername();
        if (mid == null && !StringUtils.hasText(fallbackUsername)) {
            return Optional.empty();
        }

        String username = StringUtils.hasText(fallbackUsername) ? fallbackUsername.trim() : null;
        String nickname = null;
        if (mid != null) {
            Optional<MemberProfileHttpClient.MemberFields> profile = memberProfileHttpClient.fetchProfile(mid);
            if (profile.isPresent()) {
                MemberProfileHttpClient.MemberFields fields = profile.get();
                if (StringUtils.hasText(fields.username())) {
                    username = fields.username().trim();
                }
                if (StringUtils.hasText(fields.nickname())) {
                    nickname = fields.nickname().trim();
                }
            }
        }
        if (!StringUtils.hasText(nickname)) {
            nickname = username;
        }

        Map<String, Object> member = new LinkedHashMap<>();
        if (mid != null) {
            member.put("id", mid);
        }
        if (StringUtils.hasText(username)) {
            member.put("username", username);
        }
        if (StringUtils.hasText(nickname)) {
            member.put("nickname", nickname);
        }
        return Optional.of(member);
    }
}