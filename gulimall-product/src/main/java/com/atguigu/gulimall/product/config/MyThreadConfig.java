package com.atguigu.gulimall.product.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
public class MyThreadConfig {

    public static final String PRODUCT_ITEM_EXECUTOR = "productItemThreadPool";

    @Bean(name = PRODUCT_ITEM_EXECUTOR)
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties pool) {
        int core = pool.getCoreSize() != null ? pool.getCoreSize() : 20;
        int max = pool.getMaxSize() != null ? pool.getMaxSize() : 200;
        int keep = pool.getKeepAliveTime() != null ? pool.getKeepAliveTime() : 10;
        int cap = pool.getQueueCapacity() != null ? pool.getQueueCapacity() : 100_000;
        return new ThreadPoolExecutor(
                core,
                max,
                keep,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(cap),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
