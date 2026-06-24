package com.atguigu.gulimall.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;

/**
 * Spring Session 3.x + Redis: shared {@link jakarta.servlet.http.HttpSession} across auth-server
 * replicas.
 * <p>Session attribute serialization uses Spring Session defaults (JDK). Do not register
 * {@code GenericJackson2JsonRedisSerializer} for {@code springSessionDefaultRedisSerializer}: OAuth2
 * stores {@link org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest} et al.
 * in the session; Jackson cannot reliably round-trip those types and causes 500 on the next request.</p>
 * <p>Cookie domain: set {@code gulimall.auth.session.cookie-domain=.ecommerce.com} in production so
 * {@code www.ecommerce.com} and other subdomains receive the same {@code SESSION} cookie.</p>
 */
@Configuration
@EnableRedisHttpSession
public class SpringSessionRedisConfig {

    /**
     * Session cookie: optional parent domain for subdomain SSO. Leave domain empty for localhost
     * (host-only cookie).
     */
    @Bean
    public CookieSerializer cookieSerializer(
            @Value("${gulimall.auth.session.cookie-domain:}") String cookieDomain,
            @Value("${gulimall.auth.session.cookie-secure:false}") boolean cookieSecure) {
        return new HostAwareCookieSerializer(cookieDomain, cookieSecure);
    }
}
