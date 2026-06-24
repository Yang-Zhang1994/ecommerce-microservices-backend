package com.atguigu.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Reusable cache-aside helper for high-frequency reads that guards against the three classic cache
 * failure modes. Register it via {@link ProtectedCacheConfig} ({@code @Import} it per module).
 *
 * <ul>
 *   <li><b>Penetration（穿透）</b>: a {@code null} result is cached under a short-lived marker so
 *       repeated lookups for a missing key do not keep hitting the DB.</li>
 *   <li><b>Breakdown（击穿）</b>: on a miss, a distributed lock ({@code SET NX PX}) lets a single
 *       loader rebuild the value while other callers briefly wait and re-read (single-flight).
 *       The lock is released with a compare-and-delete Lua script so a slow loader cannot delete a
 *       lock that has expired and been re-acquired by someone else.</li>
 *   <li><b>Avalanche（雪崩）</b>: real values get {@code baseTtl + random(jitter)} so a batch of
 *       keys populated together does not expire at the same instant.</li>
 * </ul>
 *
 * Dependency-light: uses {@link StringRedisTemplate} and {@link ObjectMapper}.
 */
public class ProtectedCache {

    private static final Logger log = LoggerFactory.getLogger(ProtectedCache.class);

    /** Sentinel stored in Redis to represent a cached {@code null} (penetration guard). */
    private static final String NULL_MARKER = "\u0000NULL\u0000";

    /** In-process sentinel distinguishing "key absent" (real miss) from "cached null". */
    private static final Object MISS = new Object();

    /** Atomic compare-and-delete so we only release a lock we still own. */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final ProtectedCacheProperties props;

    public ProtectedCache(StringRedisTemplate redis, ObjectMapper objectMapper, ProtectedCacheProperties props) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.props = props;
    }

    /**
     * Reads {@code key} from Redis; on a miss, loads via {@code loader} under a distributed lock,
     * writes the result back (with jitter, or a short-lived null marker), and returns it.
     *
     * @param key    fully-qualified Redis key
     * @param type   value type for JSON deserialization
     * @param loader DB/source loader, invoked at most once per key across the cluster on a miss
     * @return the cached or freshly loaded value, possibly {@code null}
     */
    @SuppressWarnings("unchecked")
    public <T> T getWithProtection(String key, Class<T> type, Supplier<T> loader) {
        Object hit = readFromCache(key, type);
        if (hit != MISS) {
            return (T) hit;
        }

        String lockKey = key + ":lock";
        String token = UUID.randomUUID().toString();
        boolean locked = Boolean.TRUE.equals(
                redis.opsForValue().setIfAbsent(lockKey, token, props.getLockTtl()));

        if (!locked) {
            // Another thread is loading. Spin-wait, re-reading the cache, then degrade to a direct load.
            Object waited = waitForOtherLoader(key, type);
            if (waited != MISS) {
                return (T) waited;
            }
            return loader.get();
        }

        try {
            // Double-check: the lock holder may have populated the cache between our read and lock.
            Object recheck = readFromCache(key, type);
            if (recheck != MISS) {
                return (T) recheck;
            }
            T value = loader.get();
            writeToCache(key, value);
            return value;
        } finally {
            releaseLock(lockKey, token);
        }
    }

    /** Removes a key from the cache (call on writes to keep the cache consistent). */
    public void evict(String key) {
        try {
            redis.delete(key);
        } catch (Exception e) {
            log.warn("protected-cache evict failed key={}", key, e);
        }
    }

    /**
     * Reads and decodes a cached value.
     *
     * @return the decoded value (possibly {@code null} when a null-marker is cached), or
     *     {@link #MISS} when the key is absent (a real cache miss).
     */
    private Object readFromCache(String key, Class<?> type) {
        String cached;
        try {
            cached = redis.opsForValue().get(key);
        } catch (Exception e) {
            // Redis hiccup: treat as a miss so the caller can degrade to the DB rather than fail.
            log.warn("protected-cache read failed key={}", key, e);
            return MISS;
        }
        if (cached == null) {
            return MISS;
        }
        if (NULL_MARKER.equals(cached)) {
            return null;
        }
        try {
            return objectMapper.readValue(cached, type);
        } catch (Exception e) {
            // Corrupt/incompatible payload: drop it and treat as a miss to self-heal.
            log.warn("protected-cache decode failed key={}, evicting", key, e);
            evict(key);
            return MISS;
        }
    }

    private void writeToCache(String key, Object value) {
        try {
            if (value == null) {
                redis.opsForValue().set(key, NULL_MARKER, props.getNullTtl());
                return;
            }
            String json = objectMapper.writeValueAsString(value);
            redis.opsForValue().set(key, json, ttlWithJitter());
        } catch (Exception e) {
            log.warn("protected-cache write failed key={}", key, e);
        }
    }

    private Duration ttlWithJitter() {
        long base = props.getBaseTtl().toMillis();
        long jitter = Math.max(0, props.getTtlJitter().toMillis());
        long extra = jitter == 0 ? 0 : ThreadLocalRandom.current().nextLong(jitter + 1);
        return Duration.ofMillis(base + extra);
    }

    private Object waitForOtherLoader(String key, Class<?> type) {
        long deadline = System.currentTimeMillis() + props.getLockWaitTimeout().toMillis();
        long interval = Math.max(1, props.getLockWaitInterval().toMillis());
        while (System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
            Object value = readFromCache(key, type);
            if (value != MISS) {
                return value;
            }
        }
        return MISS;
    }

    private void releaseLock(String lockKey, String token) {
        try {
            redis.execute(UNLOCK_SCRIPT, List.of(lockKey), token);
        } catch (Exception e) {
            log.warn("protected-cache unlock failed lockKey={}", lockKey, e);
        }
    }
}
