package com.atguigu.gulimall.seckill;

import com.atguigu.gulimall.seckill.config.SeckillCouponApiConfig;
import com.atguigu.gulimall.seckill.config.SeckillProductApiConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 谷粒商城 - 秒杀服务。
 *
 * <p>零 DB 耦合：场次数据经 HTTP 从 coupon 拉取，订单经 RabbitMQ 发往 order。
 * 瞬时洪峰由 Redis 信号量扛住，进程崩溃不影响其他微服务。</p>
 */
@EnableScheduling
@Import({SeckillCouponApiConfig.class, SeckillProductApiConfig.class})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }
}
