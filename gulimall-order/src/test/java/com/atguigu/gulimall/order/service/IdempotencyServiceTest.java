package com.atguigu.gulimall.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOps;

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService(redis, new ObjectMapper());
    }

    @Test
    void execute_returnsCachedResultWithoutReRunningAction() throws Exception {
        when(redis.opsForValue()).thenReturn(valueOps);
        String cachedJson = new ObjectMapper().writeValueAsString(new Result("done"));
        when(valueOps.get("gulimall:idempotency:order-submit:key-1")).thenReturn(cachedJson);

        AtomicInteger runs = new AtomicInteger();
        Result out = idempotencyService.execute(
                "order-submit",
                "key-1",
                Result.class,
                () -> {
                    runs.incrementAndGet();
                    return new Result("new");
                });

        assertEquals("done", out.value());
        assertEquals(0, runs.get());
    }

    @Test
    void execute_withoutKeyAlwaysRunsAction() {
        AtomicInteger runs = new AtomicInteger();
        Result out = idempotencyService.execute(
                "order-submit",
                "  ",
                Result.class,
                () -> {
                    runs.incrementAndGet();
                    return new Result("live");
                });

        assertEquals("live", out.value());
        assertEquals(1, runs.get());
        verify(valueOps, org.mockito.Mockito.never()).get(any());
        verify(valueOps, org.mockito.Mockito.never()).set(any(), any(), any());
    }

    private record Result(String value) {}
}
