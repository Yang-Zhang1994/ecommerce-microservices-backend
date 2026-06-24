package com.atguigu.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared Resilience4j registry for outbound HTTP Interface clients.
 */
@Configuration
@ConditionalOnClass(CircuitBreakerRegistry.class)
public class ResilienceClientConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }
}
