package com.atguigu.gulimall.search.config;

import com.atguigu.gulimall.search.client.BrandApi;
import com.atguigu.gulimall.search.client.ProductApi;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * HTTP interface client proxies used by search service.
 */
@Configuration
public class SearchClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

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

    @Bean
    public BrandApi brandApi(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {
        RestClient restClient = loadBalancedRestClientBuilder
                .baseUrl("http://gulimall-product")
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(BrandApi.class);
    }
}
