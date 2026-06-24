/**
 * RestClient configuration for HTTP Interfaces (Spring Boot 3.2+).
 * Use baseUrl("http://service-name") when calling other microservices; LoadBalancer resolves via Consul.
 */
package io.renren.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
