package com.atguigu.gulimall.seckill.config;

import com.atguigu.common.client.ProductApi;
import com.atguigu.common.config.RestClientConfig;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Lightweight ProductApi client (plain load-balanced RestClient, no resilience4j) so seckill
 * warm-up can fetch SKU title/image/price from gulimall-product.
 */
@Configuration
@Import(RestClientConfig.class)
public class SeckillProductApiConfig {

    @Bean
    public ProductApi productApi(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {
        RestClient restClient = loadBalancedRestClientBuilder
                .baseUrl("http://gulimall-product")
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(ProductApi.class);
    }
}
