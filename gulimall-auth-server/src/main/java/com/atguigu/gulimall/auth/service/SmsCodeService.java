package com.atguigu.gulimall.auth.service;

import com.atguigu.common.utils.R;

import java.util.Optional;

/**
 * SMS verification: stores codes in Redis and enforces send interval; sends via Twilio when configured.
 */
public interface SmsCodeService {

    R sendCode(String mobile);

    /**
     * Validates the code without removing it (allows retrying register after username/password errors).
     *
     * @return empty if valid; otherwise an error {@link R}
     */
    Optional<R> validate(String mobile, String code);

    /**
     * Removes a previously validated code after successful registration.
     */
    void consume(String mobile, String code);

    /**
     * Validates the code and removes it after successful use (one-time).
     *
     * @return empty if valid; otherwise an error {@link R}
     */
    Optional<R> validateAndConsume(String mobile, String code);
}
