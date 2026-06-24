package com.atguigu.gulimall.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Redis-backed idempotency for POST checkout / submit (header {@code Idempotency-Key}).
 */
@Service
public class IdempotencyService {

    private static final Duration TTL = Duration.ofHours(24);
    private static final String PREFIX = "gulimall:idempotency:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public IdempotencyService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public <T> T execute(String scope, String idempotencyKey, Class<T> type, Supplier<T> action) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return action.get();
        }
        String key = PREFIX + scope + ":" + idempotencyKey.trim();
        String cached = redis.opsForValue().get(key);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, type);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("idempotency cache corrupt", e);
            }
        }
        T result = action.get();
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(result), TTL);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("idempotency cache write failed", e);
        }
        return result;
    }

    public Optional<String> rawCached(String scope, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return Optional.empty();
        }
        return Optional.ofNullable(redis.opsForValue().get(PREFIX + scope + ":" + idempotencyKey.trim()));
    }
}
