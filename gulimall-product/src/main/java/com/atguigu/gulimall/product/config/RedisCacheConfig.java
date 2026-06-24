package com.atguigu.gulimall.product.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Spring Cache + Redis 配置，使用 RedisCacheManagerBuilderCustomizer 确保配置生效。
 * 与 application.yml 中 spring.cache.redis 一致：JSON 序列化、TTL、key 前缀、缓存空值。
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class RedisCacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(CacheProperties cacheProperties) {
        return builder -> {
            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                    .serializeKeysWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(
                                    new StringRedisSerializer()))
                    .serializeValuesWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(
                                    new GenericJackson2JsonRedisSerializer()));

            CacheProperties.Redis redisProperties = cacheProperties.getRedis();
            if (redisProperties.getTimeToLive() != null) {
                config = config.entryTtl(redisProperties.getTimeToLive());
            }
            if (redisProperties.getKeyPrefix() != null) {
                config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
            }
            if (Boolean.FALSE.equals(redisProperties.isUseKeyPrefix())) {
                config = config.disableKeyPrefix();
            }
            if (Boolean.FALSE.equals(redisProperties.isCacheNullValues())) {
                config = config.disableCachingNullValues();
            }
            builder.cacheDefaults(config);
        };
    }
}
