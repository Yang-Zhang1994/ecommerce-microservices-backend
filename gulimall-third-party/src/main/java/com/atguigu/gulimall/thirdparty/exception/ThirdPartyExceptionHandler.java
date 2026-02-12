package com.atguigu.gulimall.thirdparty.exception;

import com.atguigu.common.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常处理：保证接口返回 200 + JSON，便于前端拿到错误信息，避免 500 白屏。
 */
@RestControllerAdvice
public class ThirdPartyExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ThirdPartyExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public R handleException(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        log.warn("S3 presign or other error: {}", msg, e);
        return R.error("S3 presign failed: " + msg);
    }
}
