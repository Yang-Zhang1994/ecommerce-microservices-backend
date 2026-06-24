package com.atguigu.common.config;

import com.atguigu.common.client.SearchApi;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Provides SearchApi HTTP Interface client for modules that call gulimall-search.
 */
@Configuration
public class SearchApiConfig {

    @Bean
    public SearchApi searchApi(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {
        RestClient restClient = loadBalancedRestClientBuilder
                .baseUrl("http://gulimall-search")
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(SearchApi.class);
    }
}
