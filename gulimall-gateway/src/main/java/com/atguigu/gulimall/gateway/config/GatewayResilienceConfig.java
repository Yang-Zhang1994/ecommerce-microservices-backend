package com.atguigu.gulimall.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Gateway 限流 key：按客户端 IP（NAT 出口会共享同一 IP，生产可改为用户 ID + 路由维度）。
 * <p>
 * 在 AWS ALB 等负载均衡后：{@code X-Forwarded-For} 为逗号分隔链，ALB 会把<strong>它看到的 TCP 客户端 IP</strong>
 * 追加在末尾；若客户端自带伪造的 XFF，通常出现在前面，因此这里取<strong>最后一个非空段</strong>更稳妥。
 * 直连网关（无 LB）时一般无该头，回退到 {@code RemoteAddress}。
 */
@Configuration
public class GatewayResilienceConfig {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String fromHeader = clientIpFromXForwardedFor(exchange.getRequest().getHeaders().getFirst(X_FORWARDED_FOR));
            if (StringUtils.hasText(fromHeader)) {
                return Mono.just(fromHeader);
            }
            String remote = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                    .map(InetSocketAddress::getAddress)
                    .map(InetAddress::getHostAddress)
                    .orElse("unknown");
            return Mono.just(remote);
        };
    }

    /**
     * 取 X-Forwarded-For 中<strong>最后一个</strong>非空 token（适配 ALB 追加行为）；无有效值时返回 null。
     */
    static String clientIpFromXForwardedFor(String headerValue) {
        if (!StringUtils.hasText(headerValue)) {
            return null;
        }
        List<String> parts = Arrays.asList(headerValue.split(","));
        for (int i = parts.size() - 1; i >= 0; i--) {
            String trimmed = parts.get(i).trim();
            if (StringUtils.hasText(trimmed)) {
                return trimmed;
            }
        }
        return null;
    }
}
