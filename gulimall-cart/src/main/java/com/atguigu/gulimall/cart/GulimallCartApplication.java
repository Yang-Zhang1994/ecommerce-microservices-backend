package com.atguigu.gulimall.cart;

import com.atguigu.common.config.ProductApiConfig;
import com.atguigu.common.config.RestClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Gulimall - Cart service entrypoint.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Import({RestClientConfig.class, ProductApiConfig.class})
public class GulimallCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCartApplication.class, args);
    }
}
