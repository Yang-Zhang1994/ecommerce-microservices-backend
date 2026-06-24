package com.atguigu.gulimall.coupon.config;

import com.atguigu.common.client.SeckillApi;
import com.atguigu.common.config.RestClientConfig;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@Import(RestClientConfig.class)
public class SeckillApiConfig {

    @Bean
    public SeckillApi seckillApi(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {
        RestClient restClient = loadBalancedRestClientBuilder
                .baseUrl("http://gulimall-seckill")
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(SeckillApi.class);
    }
}
