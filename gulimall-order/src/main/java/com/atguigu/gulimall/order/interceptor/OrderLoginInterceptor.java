package com.atguigu.gulimall.order.interceptor;

import com.atguigu.common.constant.AuthSessionConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.to.OrderLoginUserTo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

@Component
public class OrderLoginInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(OrderLoginInterceptor.class);
    public static final ThreadLocal<OrderLoginUserTo> THREAD_LOCAL = new ThreadLocal<>();

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${order.auth-session-url:http://gulimall-gateway:88/api/auth/oauth/member/session}")
    private String authSessionUrl;

    public OrderLoginInterceptor(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        HttpSession session = request.getSession(false);
        Object memberObj = session == null ? null : session.getAttribute(AuthSessionConstant.SESSION_MEMBER_KEY);
        if (log.isDebugEnabled()) {
            String memberType = memberObj == null ? "null" : memberObj.getClass().getName();
            String cookieHeader = request.getHeader("Cookie");
            boolean hasCookieHeader = cookieHeader != null && !cookieHeader.isBlank();
            boolean hasSessionCookie = hasCookieHeader && cookieHeader.contains("SESSION=");
            log.debug("order-auth-check uri={} host={} sessionId={} hasCookieHeader={} hasSessionCookie={} hasMemberKey={} memberType={}",
                    request.getRequestURI(),
                    request.getServerName(),
                    session == null ? "null" : session.getId(),
                    hasCookieHeader,
                    hasSessionCookie,
                    memberObj != null,
                    memberType);
        }

        OrderLoginUserTo user = resolveLoginUser(session);
        if (user == null || user.getUserId() == null) {
            session = syncMemberFromAuth(request, session);
            user = resolveLoginUser(session);
        }
        if (user != null && user.getUserId() != null) {
            THREAD_LOCAL.set(user);
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(R.error(401, "Please login first")));
        return false;
    }

    /**
     * Fallback: if order-session has no member key, query auth session endpoint with same cookie,
     * then hydrate current session to avoid false "logged out" after cross-service session drift.
     */
    private HttpSession syncMemberFromAuth(HttpServletRequest request, HttpSession currentSession) {
        String cookieHeader = request.getHeader(HttpHeaders.COOKIE);
        if (cookieHeader == null || cookieHeader.isBlank() || !cookieHeader.contains("SESSION=")) {
            return currentSession;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.COOKIE, cookieHeader);
            ResponseEntity<String> response = restTemplate.exchange(
                    authSessionUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return currentSession;
            }
            Map<String, Object> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object member = body.get("member");
            if (!(member instanceof Map<?, ?> memberMap) || memberMap.isEmpty()) {
                return currentSession;
            }
            HttpSession session = currentSession != null ? currentSession : request.getSession(true);
            session.setAttribute(AuthSessionConstant.SESSION_MEMBER_KEY, member);
            if (log.isDebugEnabled()) {
                log.debug("order-auth-sync hydrated sessionId={} from auth endpoint", session.getId());
            }
            return session;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("order-auth-sync failed: {}", e.getMessage());
            }
            return currentSession;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        THREAD_LOCAL.remove();
    }

    private OrderLoginUserTo resolveLoginUser(HttpSession session) {
        if (session == null) return null;
        Object memberObj = session.getAttribute(AuthSessionConstant.SESSION_MEMBER_KEY);
        if (memberObj == null) return null;

        Long userId = null;
        String username = null;
        Integer integration = null;
        if (memberObj instanceof Map<?, ?> map) {
            userId = toLong(firstNonNull(map.get("id"), map.get("memberId"), map.get("userId"), map.get("uid")));
            Object uname = firstNonNull(map.get("username"), map.get("userName"), map.get("name"), map.get("nickName"));
            username = uname == null ? null : String.valueOf(uname);
            integration = toInt(firstNonNull(map.get("integration"), map.get("point"), map.get("points")), 0);
        } else {
            userId = invokeLongGetter(memberObj, "getId");
            if (userId == null) userId = invokeLongGetter(memberObj, "getMemberId");
            if (userId == null) userId = invokeLongGetter(memberObj, "getUserId");
            username = invokeStringGetter(memberObj, "getUsername");
            if (username == null) username = invokeStringGetter(memberObj, "getUserName");
            if (username == null) username = invokeStringGetter(memberObj, "getName");
            integration = invokeIntGetter(memberObj, "getIntegration");
        }
        if (userId == null) return null;

        OrderLoginUserTo to = new OrderLoginUserTo();
        to.setUserId(userId);
        to.setUsername(username);
        to.setIntegration(integration == null ? 0 : Math.max(0, integration));
        return to;
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
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long invokeLongGetter(Object target, String methodName) {
        try {
            Object out = target.getClass().getMethod(methodName).invoke(target);
            return toLong(out);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer toInt(Object value, int fallback) {
        if (value == null) return fallback;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private Integer invokeIntGetter(Object target, String methodName) {
        try {
            Object out = target.getClass().getMethod(methodName).invoke(target);
            return toInt(out, 0);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String invokeStringGetter(Object target, String methodName) {
        try {
            Object out = target.getClass().getMethod(methodName).invoke(target);
            return out == null ? null : String.valueOf(out);
        } catch (Exception ignored) {
            return null;
        }
    }
}
