package com.atguigu.common.config;

import com.atguigu.common.client.CouponApi;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Provides CouponApi HTTP Interface client for modules that call gulimall-coupon.
 * Always uses Consul service discovery (host "gulimall-coupon" resolved via LoadBalancer).
 */
@Configuration
public class CouponApiConfig {

    @Bean
    public CouponApi couponApi(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {
        RestClient restClient = loadBalancedRestClientBuilder
                .baseUrl("http://gulimall-coupon")
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(CouponApi.class);
    }
}
