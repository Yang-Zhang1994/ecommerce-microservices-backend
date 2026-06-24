package com.atguigu.gulimall.auth.service.impl;

import com.atguigu.gulimall.auth.config.SmsProperties;
import com.atguigu.gulimall.auth.sms.TwilioSmsSender;
import com.atguigu.common.exception.BisCodeEnum;
import com.atguigu.common.utils.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsCodeServiceImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SmsProperties smsProperties;

    @Mock
    private TwilioSmsSender twilioSmsSender;

    @InjectMocks
    private SmsCodeServiceImpl smsCodeService;

    private static final String MOBILE = "4155559876";
    private static final String E164 = "+14155559876";
    private static final String CODE = "467757";

    @BeforeEach
    void setUp() {
        when(smsProperties.getPhoneRegion()).thenReturn(SmsProperties.PhoneRegion.NANP);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void wrongCodeDoesNotDeleteStoredCode() {
        when(valueOperations.get("auth:sms:code:" + E164)).thenReturn(CODE);

        Optional<R> error = smsCodeService.validate(MOBILE, "000000");

        assertTrue(error.isPresent());
        assertEquals(BisCodeEnum.SMS_CODE_INCORRECT.getCode(), error.get().getCode());
        verify(stringRedisTemplate, never()).delete(anyString());
    }

    @Test
    void correctCodeAfterWrongAttemptStillValid() {
        when(valueOperations.get("auth:sms:code:" + E164)).thenReturn(CODE);

        assertTrue(smsCodeService.validate(MOBILE, "000000").isPresent());
        assertTrue(smsCodeService.validate(MOBILE, CODE).isEmpty());
        verify(stringRedisTemplate, never()).delete(anyString());
    }

    @Test
    void consumeOnlyDeletesMatchingCode() {
        when(valueOperations.get("auth:sms:code:" + E164)).thenReturn(CODE);

        smsCodeService.consume(MOBILE, "000000");
        verify(stringRedisTemplate, never()).delete(anyString());

        smsCodeService.consume(MOBILE, CODE);
        verify(stringRedisTemplate).delete(eq("auth:sms:code:" + E164));
    }
}
