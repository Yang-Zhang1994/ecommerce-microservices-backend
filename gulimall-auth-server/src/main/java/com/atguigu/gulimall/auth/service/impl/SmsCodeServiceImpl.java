package com.atguigu.gulimall.auth.service.impl;

import com.atguigu.gulimall.auth.config.SmsProperties;
import com.atguigu.gulimall.auth.service.SmsCodeService;
import com.atguigu.gulimall.auth.sms.TwilioSmsSender;
import com.atguigu.gulimall.auth.support.SmsPhoneSupport;
import com.atguigu.common.exception.BisCodeEnum;
import com.atguigu.common.utils.R;
import com.twilio.exception.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SmsCodeServiceImpl implements SmsCodeService {

    private static final Logger log = LoggerFactory.getLogger(SmsCodeServiceImpl.class);
    private static final String KEY_CODE = "auth:sms:code:";
    private static final String KEY_INTERVAL = "auth:sms:interval:";
    private static final int CODE_TTL_SECONDS = 300;
    private static final int SEND_INTERVAL_SECONDS = 60;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SmsProperties smsProperties;

    @Autowired
    private TwilioSmsSender twilioSmsSender;

    @Override
    public R sendCode(String mobile) {
        Optional<String> e164 = SmsPhoneSupport.normalizeE164(mobile, smsProperties.getPhoneRegion());
        if (StringUtils.isBlank(mobile) || e164.isEmpty()) {
            return R.error(BisCodeEnum.PHONE_FORMAT_INVALID);
        }
        String m = e164.get();
        try {
            Boolean firstSend = stringRedisTemplate.opsForValue()
                    .setIfAbsent(KEY_INTERVAL + m, "1", Duration.ofSeconds(SEND_INTERVAL_SECONDS));
            if (Boolean.FALSE.equals(firstSend)) {
                return R.error(BisCodeEnum.SMS_CODE_RATE_TOO_HIGH);
            }
            String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
            stringRedisTemplate.opsForValue().set(KEY_CODE + m, code, Duration.ofSeconds(CODE_TTL_SECONDS));
            if (twilioSmsSender.isConfigured()) {
                String body = smsProperties.getVerificationMessageTemplate().replace("{code}", code);
                twilioSmsSender.send(m, body);
                log.debug("SMS sent via Twilio, e164={}", m);
            } else {
                log.warn("[SMS stub] e164={} code={} (configure gulimall.auth.sms.twilio for Twilio)", m, code);
            }
            return R.ok("Verification code sent");
        } catch (ApiException e) {
            log.error("Twilio send failed, mobile={}, status={}, code={}", m, e.getStatusCode(), e.getCode(), e);
            stringRedisTemplate.delete(KEY_INTERVAL + m);
            if (isTwilioTrialDestinationBlocked(e)) {
                return R.error(BisCodeEnum.SMS_DESTINATION_NOT_ALLOWED);
            }
            return R.error(BisCodeEnum.VERIFICATION_SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("send sms code failed, mobile={}", m, e);
            stringRedisTemplate.delete(KEY_INTERVAL + m);
            return R.error(BisCodeEnum.VERIFICATION_SERVICE_UNAVAILABLE);
        }
    }

    /** Twilio trial accounts reject unverified destinations (REST 400, error code 21608). */
    private static boolean isTwilioTrialDestinationBlocked(ApiException e) {
        if (e.getCode() != null && e.getCode() == 21608) {
            return true;
        }
        String msg = e.getMessage();
        return msg != null && msg.toLowerCase().contains("unverified");
    }

    @Override
    public Optional<R> validate(String mobile, String code) {
        Optional<String> e164 = SmsPhoneSupport.normalizeE164(mobile, smsProperties.getPhoneRegion());
        if (StringUtils.isBlank(mobile) || e164.isEmpty()) {
            return Optional.of(R.error(BisCodeEnum.PHONE_FORMAT_INVALID));
        }
        if (StringUtils.isBlank(code)) {
            return Optional.of(R.error(BisCodeEnum.SMS_VERIFICATION_CODE_REQUIRED));
        }
        String m = e164.get();
        try {
            String cached = stringRedisTemplate.opsForValue().get(KEY_CODE + m);
            if (cached == null) {
                return Optional.of(R.error(BisCodeEnum.SMS_CODE_EXPIRED));
            }
            if (!cached.equals(code.trim())) {
                // Wrong guess does not remove the stored code; user can retry with the same SMS.
                return Optional.of(R.error(BisCodeEnum.SMS_CODE_INCORRECT));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("validate sms code failed, mobile={}", m, e);
            return Optional.of(R.error(BisCodeEnum.VERIFICATION_SERVICE_UNAVAILABLE));
        }
    }

    @Override
    public void consume(String mobile, String code) {
        Optional<String> e164 = SmsPhoneSupport.normalizeE164(mobile, smsProperties.getPhoneRegion());
        if (e164.isEmpty() || StringUtils.isBlank(code)) {
            return;
        }
        String m = e164.get();
        String key = KEY_CODE + m;
        try {
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null && cached.equals(code.trim())) {
                stringRedisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.error("consume sms code failed, mobile={}", m, e);
        }
    }

    @Override
    public Optional<R> validateAndConsume(String mobile, String code) {
        Optional<R> error = validate(mobile, code);
        if (error.isPresent()) {
            return error;
        }
        consume(mobile, code);
        return Optional.empty();
    }
}
