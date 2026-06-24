package com.atguigu.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProtectedCacheTest {

    private static final String NULL_MARKER = "\u0000NULL\u0000";

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOps;

    private ProtectedCache cache;

    @BeforeEach
    void setUp() {
        ProtectedCacheProperties props = new ProtectedCacheProperties();
        cache = new ProtectedCache(redis, new ObjectMapper(), props);
        when(redis.opsForValue()).thenReturn(valueOps);
    }

    private void stubUnlockScript() {
        when(redis.execute(any(DefaultRedisScript.class), any(), any())).thenReturn(1L);
    }

    @Test
    void getWithProtection_returnsCachedValueWithoutCallingLoader() {
        when(valueOps.get("sku:1")).thenReturn("\"cached\"");

        Supplier<String> loader = () -> {
            throw new AssertionError("loader should not run on cache hit");
        };

        String result = cache.getWithProtection("sku:1", String.class, loader);

        assertEquals("cached", result);
        verify(valueOps, never()).setIfAbsent(any(), any(), any(Duration.class));
    }

    @Test
    void getWithProtection_cachesNullMarkerWhenLoaderReturnsNull() {
        stubUnlockScript();
        when(valueOps.get("sku:missing")).thenReturn(null);
        when(valueOps.setIfAbsent(eq("sku:missing:lock"), any(String.class), any(Duration.class)))
                .thenReturn(true);

        assertNull(cache.getWithProtection("sku:missing", String.class, () -> null));

        verify(valueOps).set(eq("sku:missing"), eq(NULL_MARKER), eq(Duration.ofSeconds(60)));
    }

    @Test
    void getWithProtection_loadsAndStoresValueOnMiss() {
        stubUnlockScript();
        when(valueOps.get("sku:2")).thenReturn(null);
        when(valueOps.setIfAbsent(eq("sku:2:lock"), any(String.class), any(Duration.class)))
                .thenReturn(true);

        String result = cache.getWithProtection("sku:2", String.class, () -> "fresh");

        assertEquals("fresh", result);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOps).set(eq("sku:2"), jsonCaptor.capture(), any(Duration.class));
        assertEquals("\"fresh\"", jsonCaptor.getValue());
    }
}
