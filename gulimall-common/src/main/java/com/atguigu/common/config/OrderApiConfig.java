package com.atguigu.common.config;

import com.atguigu.common.client.OrderApi;
import com.atguigu.common.resilience.ResilientHttpClientFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

@Configuration
@Import(ResilienceClientConfig.class)
public class OrderApiConfig {

    @Bean
    public OrderApi orderApi(
            @LoadBalanced RestClient.Builder loadBalancedRestClientBuilder,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        return ResilientHttpClientFactory.create(
                OrderApi.class,
                loadBalancedRestClientBuilder,
                "http://gulimall-order",
                circuitBreakerRegistry,
                "orderApi");
    }
}
