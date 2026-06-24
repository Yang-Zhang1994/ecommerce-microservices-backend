package com.atguigu.gulimall.member.exception;

import com.atguigu.common.exception.BisException;
import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.atguigu.gulimall.member.controller")
public class MemberExceptionControllerAdvice {

    @ExceptionHandler(BisException.class)
    public R handleBisException(BisException e) {
        return R.error(e.getBiz());
    }
}
