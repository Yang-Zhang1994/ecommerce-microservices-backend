package com.atguigu.common.exception;

/**
 * Business rule violation; handled in web layer and converted to unified API response.
 */
public class BisException extends RuntimeException {

    private final BisCodeEnum biz;

    public BisException(BisCodeEnum biz) {
        super(biz.getMessage());
        this.biz = biz;
    }

    public BisCodeEnum getBiz() {
        return biz;
    }
}
