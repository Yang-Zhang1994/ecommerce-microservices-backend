package com.atguigu.common.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Provides a load-balanced RestClient.Builder for HTTP Interfaces + RestClient.
 * Services use baseUrl("http://service-name") and the LoadBalancer resolves the name via Consul.
 */
@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder(RestTemplateBuilder restTemplateBuilder) {
        return RestClient.builder()
                .requestFactory(restTemplateBuilder
                        .setConnectTimeout(Duration.ofSeconds(3))
                        .setReadTimeout(Duration.ofSeconds(10))
                        .buildRequestFactory());
    }
}
