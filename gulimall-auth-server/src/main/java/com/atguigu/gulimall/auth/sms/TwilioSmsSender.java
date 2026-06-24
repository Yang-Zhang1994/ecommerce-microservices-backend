package com.atguigu.gulimall.auth.sms;

import com.atguigu.gulimall.auth.config.SmsProperties;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Component;

/**
 * Sends SMS via Twilio when {@link SmsProperties.TwilioProperties#isComplete()}.
 */
@Component
public class TwilioSmsSender {

    private final SmsProperties smsProperties;
    private volatile boolean initialized;

    public TwilioSmsSender(SmsProperties smsProperties) {
        this.smsProperties = smsProperties;
    }

    public boolean isConfigured() {
        return smsProperties.getTwilio().isComplete();
    }

    private void ensureInitialized() {
        if (!isConfigured()) {
            throw new IllegalStateException("Twilio credentials are not configured");
        }
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    SmsProperties.TwilioProperties t = smsProperties.getTwilio();
                    Twilio.init(t.getAccountSid(), t.getAuthToken());
                    initialized = true;
                }
            }
        }
    }

    /**
     * @param toE164 destination in E.164
     * @param body   message body (must not contain secrets)
     */
    public void send(String toE164, String body) {
        ensureInitialized();
        SmsProperties.TwilioProperties t = smsProperties.getTwilio();
        Message.creator(new PhoneNumber(toE164), new PhoneNumber(t.getFromNumber()), body).create();
    }
}
