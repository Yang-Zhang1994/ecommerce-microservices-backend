package com.atguigu.common.config;

import com.atguigu.common.client.WareApi;
import com.atguigu.common.resilience.ResilientHttpClientFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

/**
 * Provides WareApi HTTP Interface client for modules that call gulimall-ware.
 */
@Configuration
@Import(ResilienceClientConfig.class)
public class WareApiConfig {

    @Bean
    public WareApi wareApi(
            @LoadBalanced RestClient.Builder loadBalancedRestClientBuilder,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        return ResilientHttpClientFactory.create(
                WareApi.class,
                loadBalancedRestClientBuilder,
                "http://gulimall-ware",
                circuitBreakerRegistry,
                "wareApi");
    }
}
