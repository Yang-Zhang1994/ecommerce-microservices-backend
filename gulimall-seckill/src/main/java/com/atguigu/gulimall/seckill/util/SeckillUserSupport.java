package com.atguigu.gulimall.seckill.util;

import com.atguigu.common.constant.AuthSessionConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Reads the logged-in member id from the shared Spring Session (key {@code OAUTH_MEMBER_PROFILE}),
 * mirroring {@code gulimall-cart}'s CartInterceptor so seckill resolves the same user.
 */
public final class SeckillUserSupport {

    private SeckillUserSupport() {}

    public static Long currentMemberId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return extractUserId(session.getAttribute(AuthSessionConstant.SESSION_MEMBER_KEY));
    }

    private static Long extractUserId(Object memberObj) {
        if (memberObj == null) {
            return null;
        }
        if (memberObj instanceof Number n) {
            return n.longValue();
        }
        if (memberObj instanceof String s) {
            return toLong(s);
        }
        if (memberObj instanceof Map<?, ?> map) {
            Object idObj = firstNonNull(map.get("id"), map.get("memberId"), map.get("userId"), map.get("uid"));
            return toLong(idObj);
        }
        Long reflected = invokeLongGetter(memberObj, "getId");
        if (reflected != null) return reflected;
        reflected = invokeLongGetter(memberObj, "getMemberId");
        if (reflected != null) return reflected;
        return invokeLongGetter(memberObj, "getUserId");
    }

    private static Object firstNonNull(Object... values) {
        if (values == null) return null;
        for (Object v : values) {
            if (v != null) return v;
        }
        return null;
    }

    private static Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Long invokeLongGetter(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            return toLong(m.invoke(target));
        } catch (Exception ignored) {
            return null;
        }
    }
}
