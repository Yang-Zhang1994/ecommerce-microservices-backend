package com.atguigu.gulimall.ware;

import com.atguigu.common.config.RestClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

/**
 * 谷粒商城 - 仓储服务
 *
 * @author gulimall
 */
@EnableDiscoveryClient
@SpringBootApplication
@Import(RestClientConfig.class)
public class GulimallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallWareApplication.class, args);
    }
}

