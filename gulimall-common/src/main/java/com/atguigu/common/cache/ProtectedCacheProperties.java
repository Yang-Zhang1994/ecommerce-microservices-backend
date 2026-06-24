package com.atguigu.common.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Tunables for {@link ProtectedCache}. Bound from {@code gulimall.cache.protected.*};
 * all values have sensible defaults so no config is required.
 */
@ConfigurationProperties(prefix = "gulimall.cache.protected")
public class ProtectedCacheProperties {

    /** Base time-to-live for a real (non-null) cached value. */
    private Duration baseTtl = Duration.ofMinutes(30);

    /** Max extra random TTL added on top of {@link #baseTtl} to avoid correlated expiry (avalanche). */
    private Duration ttlJitter = Duration.ofMinutes(5);

    /** Short TTL for cached null markers (penetration guard); keeps misses from sticking too long. */
    private Duration nullTtl = Duration.ofSeconds(60);

    /** How long the per-key load lock is held before it auto-expires (breakdown guard). */
    private Duration lockTtl = Duration.ofSeconds(5);

    /** Total time a waiter will spin re-reading the cache while another thread loads. */
    private Duration lockWaitTimeout = Duration.ofMillis(500);

    /** Sleep between cache re-reads while waiting for the lock holder. */
    private Duration lockWaitInterval = Duration.ofMillis(50);

    public Duration getBaseTtl() {
        return baseTtl;
    }

    public void setBaseTtl(Duration baseTtl) {
        this.baseTtl = baseTtl;
    }

    public Duration getTtlJitter() {
        return ttlJitter;
    }

    public void setTtlJitter(Duration ttlJitter) {
        this.ttlJitter = ttlJitter;
    }

    public Duration getNullTtl() {
        return nullTtl;
    }

    public void setNullTtl(Duration nullTtl) {
        this.nullTtl = nullTtl;
    }

    public Duration getLockTtl() {
        return lockTtl;
    }

    public void setLockTtl(Duration lockTtl) {
        this.lockTtl = lockTtl;
    }

    public Duration getLockWaitTimeout() {
        return lockWaitTimeout;
    }

    public void setLockWaitTimeout(Duration lockWaitTimeout) {
        this.lockWaitTimeout = lockWaitTimeout;
    }

    public Duration getLockWaitInterval() {
        return lockWaitInterval;
    }

    public void setLockWaitInterval(Duration lockWaitInterval) {
        this.lockWaitInterval = lockWaitInterval;
    }
}
