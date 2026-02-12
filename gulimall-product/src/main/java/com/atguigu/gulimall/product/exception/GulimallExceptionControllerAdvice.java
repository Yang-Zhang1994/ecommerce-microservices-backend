package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BisCodeEnum;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("An exception occurred {}, Error Type {}", e.getMessage(), e.getClass());

        BindingResult result = e.getBindingResult();
        HashMap<String, String> errorMap = new HashMap<>();

        result.getFieldErrors().forEach((fieldError) -> {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return R.error(BisCodeEnum.VALID_EXCEPTION.getCode(), BisCodeEnum.VALID_EXCEPTION.getMessage()).put("data", errorMap);
    }


    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        log.error("An exception occurred: ", throwable);
        return R.error(BisCodeEnum.UNKNOWN_EXCEPTION.getCode(), BisCodeEnum.UNKNOWN_EXCEPTION.getMessage());
    }
}
