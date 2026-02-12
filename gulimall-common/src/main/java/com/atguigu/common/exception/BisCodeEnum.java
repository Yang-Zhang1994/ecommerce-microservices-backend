package com.atguigu.common.exception;

public enum BisCodeEnum {
    UNKNOWN_EXCEPTION(10000, "Unknown exception"),
    VALID_EXCEPTION(10001, "Parameter format validation failed");

    private final int code;
    private final String message;

    BisCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
