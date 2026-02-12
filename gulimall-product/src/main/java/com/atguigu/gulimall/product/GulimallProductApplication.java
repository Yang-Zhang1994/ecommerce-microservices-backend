package com.atguigu.gulimall.product;

import com.atguigu.common.config.CouponApiConfig;
import com.atguigu.common.config.RestClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

/**
 * 谷粒商城 - 商品服务
 *
 * @author gulimall
 */
@SpringBootApplication
@EnableDiscoveryClient
@Import({ RestClientConfig.class, CouponApiConfig.class })
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }
}

