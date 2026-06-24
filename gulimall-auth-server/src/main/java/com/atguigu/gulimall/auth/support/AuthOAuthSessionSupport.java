package com.atguigu.gulimall.auth.support;

import com.atguigu.common.constant.AuthSessionConstant;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Session attribute for the mall-facing member profile (OAuth callback or password login).
 * <p>Stored under {@link AuthSessionConstant#SESSION_MEMBER_KEY} for {@code GET /auth/oauth/member/session}.</p>
 */
public final class AuthOAuthSessionSupport {

    /** Alias for {@link AuthSessionConstant#SESSION_MEMBER_KEY}. */
    public static final String SESSION_MEMBER_KEY = AuthSessionConstant.SESSION_MEMBER_KEY;

    private AuthOAuthSessionSupport() {}

    /** Normalize {@code R.get("member")} from member-service into a string-keyed map for Redis session storage. */
    public static Map<String, Object> coerceMemberMap(Object memberObj, ObjectMapper objectMapper) {
        if (memberObj == null) {
            return null;
        }
        if (memberObj instanceof Map<?, ?> raw) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : raw.entrySet()) {
                out.put(String.valueOf(e.getKey()), e.getValue());
            }
            return out;
        }
        try {
            return objectMapper.convertValue(memberObj, new TypeReference<Map<String, Object>>() {});
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
