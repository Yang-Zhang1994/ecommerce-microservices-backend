package com.atguigu.gulimall.order.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * HTTP 调用会员服务；使用 Resilience4j 熔断，避免 member 慢/挂拖垮订单线程。
 */
@Component
public class MemberProfileHttpClient {

    private static final Logger log = LoggerFactory.getLogger(MemberProfileHttpClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${order.member-service-url:http://gulimall-gateway:88/api/member}")
    private String memberServiceUrl;

    public MemberProfileHttpClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public record MemberFields(String username, String nickname) {}

    @CircuitBreaker(name = "memberService", fallbackMethod = "fetchProfileFallback")
    public Optional<MemberFields> fetchProfile(Long memberId) {
        if (memberId == null) {
            return Optional.empty();
        }
        String url = memberServiceUrl.replaceAll("/$", "") + "/member/info/" + memberId;
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            return Optional.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(resp.getBody());
            if (root.path("code").asInt(1) != 0) {
                return Optional.empty();
            }
            JsonNode m = root.path("member");
            if (m.isMissingNode() || !m.isObject()) {
                return Optional.empty();
            }
            String un = textOrNull(m, "username");
            String nick = textOrNull(m, "nickname");
            return Optional.of(new MemberFields(un, nick));
        } catch (IOException e) {
            throw new IllegalStateException("member profile parse failed", e);
        }
    }

    @SuppressWarnings("unused")
    private Optional<MemberFields> fetchProfileFallback(Long memberId, Throwable t) {
        log.error(
                "Member profile fetch failed for ID: {}, entering fallback. Reason: {}",
                memberId,
                t != null ? t.getMessage() : "null");
        return Optional.empty();
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || !v.isTextual()) {
            return null;
        }
        String s = v.asText();
        return StringUtils.hasText(s) ? s : null;
    }
}
