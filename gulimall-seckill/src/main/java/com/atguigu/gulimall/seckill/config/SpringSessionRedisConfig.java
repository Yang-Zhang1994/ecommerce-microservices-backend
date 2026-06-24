package com.atguigu.gulimall.seckill.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.util.StringUtils;

/**
 * Shared Redis-backed {@code SESSION} cookie with auth-server / cart so kill can resolve member id.
 */
@Configuration
@EnableRedisHttpSession
public class SpringSessionRedisConfig {

    @Bean
    public CookieSerializer cookieSerializer(
            @Value("${gulimall.auth.session.cookie-domain:}") String cookieDomain,
            @Value("${gulimall.auth.session.cookie-secure:false}") boolean cookieSecure) {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("SESSION");
        serializer.setCookiePath("/");
        if (StringUtils.hasText(cookieDomain)) {
            String d = cookieDomain.trim();
            if (d.startsWith(".")) {
                d = d.substring(1);
            }
            if (StringUtils.hasText(d)) {
                serializer.setDomainName(d);
            }
        }
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(cookieSecure);
        serializer.setSameSite("Lax");
        return serializer;
    }
}
