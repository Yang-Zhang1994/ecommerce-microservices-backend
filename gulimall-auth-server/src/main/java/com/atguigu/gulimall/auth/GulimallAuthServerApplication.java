package com.atguigu.gulimall.auth;

import com.atguigu.gulimall.auth.config.SmsProperties;
import com.atguigu.common.config.MemberApiConfig;
import com.atguigu.common.config.RestClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

/**
 * Auth center microservice: Consul service name {@code gulimall-auth-server}, health {@code /actuator/health}.
 */
@EnableDiscoveryClient
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@EnableConfigurationProperties(SmsProperties.class)
@Import({ RestClientConfig.class, MemberApiConfig.class })
public class GulimallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthServerApplication.class, args);
    }
}
