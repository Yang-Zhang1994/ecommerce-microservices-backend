package com.atguigu.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GulimallCorsConfiguration {
    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许的源：Next 商城前端（开发 3001）+ 通用匹配
        corsConfiguration.addAllowedOriginPattern("http://localhost:3001");
        corsConfiguration.addAllowedOriginPattern("http://127.0.0.1:3001");
        corsConfiguration.addAllowedOriginPattern("http://auth.ecommerce.com:3001");
        corsConfiguration.addAllowedOriginPattern("*");
        // 允许所有请求头
        corsConfiguration.addAllowedHeader("*");
        // 允许所有请求方法
        corsConfiguration.addAllowedMethod("*");
        // 允许携带认证信息
        corsConfiguration.setAllowCredentials(true);
        // 预检请求的缓存时间（秒）
        corsConfiguration.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(source);
    }
}
