package com.atguigu.gulimall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 线程池参数，对应配置前缀 {@code gulimall.thread.*}（可在 application.yml / 环境变量中覆盖）。
 * 由 {@link MyThreadConfig} 上的 {@code @EnableConfigurationProperties} 注册为 Bean。
 */
@Data
@ConfigurationProperties(prefix = "gulimall.thread")
public class ThreadPoolConfigProperties {

    /** 核心线程数，默认与教程一致 */
    private Integer coreSize = 20;

    /** 最大线程数 */
    private Integer maxSize = 200;

    /** 非核心线程空闲存活时间（秒） */
    private Integer keepAliveTime = 10;

    /** 队列容量 */
    private Integer queueCapacity = 100000;
}
