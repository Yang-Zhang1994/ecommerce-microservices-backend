package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.AuthSessionConstant;
import com.atguigu.gulimall.cart.constant.CartConstant;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Set;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves logged-in user (from Session) and anonymous user (from user-key cookie).
 * <p>{@code user-key} is issued only for guests. Logged-in requests keep an existing
 * {@code user-key} when present so {@code mergeTempCartIntoUserCartIfNeeded} can merge
 * pre-login temp carts; new keys are not created after login.</p>
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CartInterceptor.class);
    private static final Set<String> COMMON_SECOND_LEVEL_SUFFIXES = Set.of(
            "com.cn", "net.cn", "org.cn", "gov.cn"
    );
    public static final ThreadLocal<UserInfoTo> THREAD_LOCAL = new ThreadLocal<>();
    @Value("${gulimall.cart.temp-user.cookie-domain:}")
    private String tempUserCookieDomain;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UserInfoTo userInfo = new UserInfoTo();
        boolean needSetTempCookie = false;

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object memberObj = session.getAttribute(AuthSessionConstant.SESSION_MEMBER_KEY);
            Long userId = extractUserId(memberObj);
            if (userId != null) {
                userInfo.setUserId(userId);
            } else if (memberObj != null) {
                if (memberObj instanceof Map<?, ?> map) {
                    log.warn("Session member exists but userId is unreadable. memberType={}, keys={}",
                            memberObj.getClass().getName(), map.keySet());
                } else {
                    log.warn("Session member exists but userId is unreadable. memberType={}",
                            memberObj.getClass().getName());
                }
            }
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(cookie.getName())) {
                    userInfo.setUserKey(cookie.getValue());
                    userInfo.setTempUser(true);
                    break;
                }
            }
        }

        boolean anonymous = userInfo.getUserId() == null;
        if ((userInfo.getUserKey() == null || userInfo.getUserKey().isBlank()) && anonymous) {
            userInfo.setUserKey(UUID.randomUUID().toString());
            userInfo.setTempUser(false);
            needSetTempCookie = true;
        }

        if (needSetTempCookie) {
            addTempUserCookie(request, response, userInfo.getUserKey());
        }

        THREAD_LOCAL.set(userInfo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           org.springframework.web.servlet.ModelAndView modelAndView) {
        UserInfoTo userInfo = THREAD_LOCAL.get();
        if (userInfo == null
                || userInfo.getUserId() != null
                || userInfo.isTempUser()
                || !StringUtils.hasText(userInfo.getUserKey())) {
            return;
        }
        // First anonymous visit: cookie may also be set in preHandle; refresh for MVC views.
        addTempUserCookie(request, response, userInfo.getUserKey());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        THREAD_LOCAL.remove();
    }

    private Long extractUserId(Object memberObj) {
        if (memberObj == null) {
            return null;
        }
        if (memberObj instanceof Number n) {
            return n.longValue();
        }
        if (memberObj instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (memberObj instanceof Map<?, ?> map) {
            Object idObj = firstNonNull(
                    map.get("id"),
                    map.get("memberId"),
                    map.get("userId"),
                    map.get("uid"));
            return toLong(idObj);
        }

        // Defensive fallback: tolerate session values stored as DTO objects.
        Long reflected = invokeLongGetter(memberObj, "getId");
        if (reflected != null) return reflected;
        reflected = invokeLongGetter(memberObj, "getMemberId");
        if (reflected != null) return reflected;
        return invokeLongGetter(memberObj, "getUserId");
    }

    private Object firstNonNull(Object... values) {
        if (values == null) return null;
        for (Object v : values) {
            if (v != null) return v;
        }
        return null;
    }

    private Long toLong(Object value) {
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

    private Long invokeLongGetter(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            Object out = m.invoke(target);
            return toLong(out);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void addTempUserCookie(HttpServletRequest request, HttpServletResponse response, String userKey) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
                .from(CartConstant.TEMP_USER_COOKIE_NAME, userKey)
                .path("/")
                .maxAge(Duration.ofSeconds(CartConstant.TEMP_USER_COOKIE_TIMEOUT))
                .httpOnly(true)
                .sameSite("Lax");

        String domain = resolveCookieDomain(request);
        if (StringUtils.hasText(domain)) {
            builder.domain(domain);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private String resolveCookieDomain(HttpServletRequest request) {
        if (StringUtils.hasText(tempUserCookieDomain)) {
            return tempUserCookieDomain.trim();
        }
        return resolveTopLevelDomain(request);
    }

    /**
     * Infer registrable domain for subdomain sharing.
     * Falls back to host-only cookie (null) when host is local/internal/unknown.
     */
    private String resolveTopLevelDomain(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String host = request.getServerName();
        if (!StringUtils.hasText(host)) {
            return null;
        }
        String h = host.trim().toLowerCase();
        if ("localhost".equals(h) || h.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            return null;
        }
        String[] parts = h.split("\\.");
        if (parts.length < 2) {
            return null;
        }

        // Internal pseudo-domains are often rejected by browsers when used as cookie domain.
        String suffix = parts[parts.length - 1];
        if ("local".equals(suffix) || "internal".equals(suffix)) {
            return null;
        }

        // Handle common country-code second level suffixes: *.com.cn -> example.com.cn
        if (parts.length >= 3) {
            String lastTwo = parts[parts.length - 2] + "." + parts[parts.length - 1];
            if (COMMON_SECOND_LEVEL_SUFFIXES.contains(lastTwo)) {
                return parts[parts.length - 3] + "." + lastTwo;
            }
        }
        return parts[parts.length - 2] + "." + parts[parts.length - 1];
    }
}
