package com.atguigu.gulimall.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SMS / phone validation. Use {@link PhoneRegion#NANP} for Canada and US (+1).
 */
@ConfigurationProperties(prefix = "gulimall.auth.sms")
public class SmsProperties {

    /**
     * {@code CHINA} — mainland 11-digit mobiles (1[3-9]…).<br>
     * {@code NANP} — North America +1 (Canada/US), 10-digit national number.
     */
    private PhoneRegion phoneRegion = PhoneRegion.CHINA;

    /**
     * SMS body for Twilio; {@code {code}} is replaced with the numeric OTP (plain text only).
     */
    private String verificationMessageTemplate = "Your verification code is {code}. Valid for 5 minutes.";

    /**
     * When {@link TwilioProperties#isComplete()} is true, verification SMS is sent via Twilio;
     * otherwise codes are only logged (dev stub).
     */
    private TwilioProperties twilio = new TwilioProperties();

    public PhoneRegion getPhoneRegion() {
        return phoneRegion;
    }

    public void setPhoneRegion(PhoneRegion phoneRegion) {
        this.phoneRegion = phoneRegion;
    }

    public String getVerificationMessageTemplate() {
        return verificationMessageTemplate;
    }

    public void setVerificationMessageTemplate(String verificationMessageTemplate) {
        this.verificationMessageTemplate =
                verificationMessageTemplate == null || verificationMessageTemplate.isBlank()
                        ? "Your verification code is {code}. Valid for 5 minutes."
                        : verificationMessageTemplate;
    }

    public TwilioProperties getTwilio() {
        return twilio;
    }

    public void setTwilio(TwilioProperties twilio) {
        this.twilio = twilio != null ? twilio : new TwilioProperties();
    }

    public enum PhoneRegion {
        CHINA,
        NANP
    }

    public static class TwilioProperties {

        private String accountSid = "";
        private String authToken = "";
        /** Twilio caller ID or number, E.164 (e.g. {@code +12065551234}). */
        private String fromNumber = "";

        public boolean isComplete() {
            return accountSid != null && !accountSid.isBlank()
                    && authToken != null && !authToken.isBlank()
                    && fromNumber != null && !fromNumber.isBlank();
        }

        public String getAccountSid() {
            return accountSid;
        }

        public void setAccountSid(String accountSid) {
            this.accountSid = accountSid == null ? "" : accountSid;
        }

        public String getAuthToken() {
            return authToken;
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken == null ? "" : authToken;
        }

        public String getFromNumber() {
            return fromNumber;
        }

        public void setFromNumber(String fromNumber) {
            this.fromNumber = fromNumber == null ? "" : fromNumber;
        }
    }
}
