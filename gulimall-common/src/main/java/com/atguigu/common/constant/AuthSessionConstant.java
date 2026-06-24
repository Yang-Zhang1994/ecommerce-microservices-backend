package com.atguigu.common.constant;

/**
 * Servlet session attribute names shared by {@code gulimall-auth-server} and services that read the
 * same Spring Session + Redis store (e.g. {@code gulimall-cart}).
 */
public final class AuthSessionConstant {

    private AuthSessionConstant() {}

    /**
     * Member profile map after password login or OAuth; consumed by
     * {@code GET /auth/oauth/member/session} and by cart when merging anonymous carts.
     */
    public static final String SESSION_MEMBER_KEY = "OAUTH_MEMBER_PROFILE";
}
