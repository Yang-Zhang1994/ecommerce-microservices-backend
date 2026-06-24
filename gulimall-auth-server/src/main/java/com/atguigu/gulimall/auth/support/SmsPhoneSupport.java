package com.atguigu.gulimall.auth.support;

import com.atguigu.gulimall.auth.config.SmsProperties.PhoneRegion;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Normalizes phone numbers for Redis keys and validates by region.
 */
public final class SmsPhoneSupport {

    private static final Pattern CHINA_MOBILE = Pattern.compile("^1[3-9]\\d{9}$");
    /** NANP: NPA and NXX first digits 2–9 */
    private static final Pattern NANP_NATIONAL = Pattern.compile("^[2-9]\\d{2}[2-9]\\d{2}\\d{4}$");

    private SmsPhoneSupport() {
    }

    /**
     * @return E.164-style key, e.g. {@code +86138…} or {@code +16135550123}
     */
    public static Optional<String> normalizeE164(String raw, PhoneRegion region) {
        if (raw == null) {
            return Optional.empty();
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return Optional.empty();
        }
        return switch (region) {
            case CHINA -> normalizeChina(s);
            case NANP -> normalizeNanp(s);
        };
    }

    public static boolean isValid(String raw, PhoneRegion region) {
        return normalizeE164(raw, region).isPresent();
    }

    private static Optional<String> normalizeChina(String s) {
        String digits = digitsOnly(s);
        if (digits.startsWith("86") && digits.length() == 13) {
            digits = digits.substring(2);
        }
        if (!CHINA_MOBILE.matcher(digits).matches()) {
            return Optional.empty();
        }
        return Optional.of("+86" + digits);
    }

    private static Optional<String> normalizeNanp(String s) {
        String digits = digitsOnly(s);
        if (digits.startsWith("1") && digits.length() == 11) {
            digits = digits.substring(1);
        }
        if (!NANP_NATIONAL.matcher(digits).matches()) {
            return Optional.empty();
        }
        return Optional.of("+1" + digits);
    }

    private static String digitsOnly(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
