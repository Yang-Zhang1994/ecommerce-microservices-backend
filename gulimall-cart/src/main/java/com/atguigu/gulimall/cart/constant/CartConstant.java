package com.atguigu.gulimall.cart.constant;

/**
 * Cart module constants.
 */
public final class CartConstant {

    private CartConstant() {}

    /** Anonymous cart identity cookie name. */
    public static final String TEMP_USER_COOKIE_NAME = "user-key";

    /** Cookie max age: 30 days. */
    public static final int TEMP_USER_COOKIE_TIMEOUT = 60 * 60 * 24 * 30;

    /** Redis key prefix for cart hashes. */
    public static final String CART_REDIS_PREFIX = "gulimall:cart:";
}
