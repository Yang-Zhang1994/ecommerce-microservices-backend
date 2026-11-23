package com.atguigu.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Exposes the shared {@link ProtectedCache} as a bean when Redis is on the classpath.
 * Registered via {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * so {@link StringRedisTemplate} exists before {@link ConditionalOnBean} is evaluated.
 * Redis is an optional dependency of this module, so the whole class is skipped (by class name, without
 * loading it) in services that don't depend on Redis; otherwise they fail at startup with
 * NoClassDefFoundError: StringRedisTemplate.
 * Tunables live under {@code gulimall.cache.protected.*}.
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
@EnableConfigurationProperties(ProtectedCacheProperties.class)
public class ProtectedCacheConfig {

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnMissingBean
    public ProtectedCache protectedCache(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            ProtectedCacheProperties properties) {
        return new ProtectedCache(stringRedisTemplate, objectMapper, properties);
    }
}
