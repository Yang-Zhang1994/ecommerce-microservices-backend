package com.atguigu.gulimall.cart.to;

import lombok.Data;

/**
 * Request-scoped user identity for cart operations.
 */
@Data
public class UserInfoTo {

    /** Logged-in member id, null for anonymous visitor. */
    private Long userId;

    /** Guest cart key from {@code user-key} cookie; set only for anonymous visitors (or kept after login for merge). */
    private String userKey;

    /** True when request already carried a {@code user-key} cookie (returning guest). */
    private boolean tempUser;
}
