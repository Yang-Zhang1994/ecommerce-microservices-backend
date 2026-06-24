package com.atguigu.gulimall.auth.controller;

import com.atguigu.gulimall.auth.config.SmsProperties;
import com.atguigu.gulimall.auth.service.SmsCodeService;
import com.atguigu.gulimall.auth.support.AuthOAuthSessionSupport;
import com.atguigu.gulimall.auth.support.SmsPhoneSupport;
import com.atguigu.common.client.MemberApi;
import com.atguigu.common.exception.BisCodeEnum;
import com.atguigu.common.to.member.MemberGoogleOAuthTo;
import com.atguigu.common.to.member.MemberLoginTo;
import com.atguigu.common.to.member.MemberRegisterTo;
import com.atguigu.common.to.member.SmsSendTo;
import com.atguigu.common.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Account register/login: clients call the auth service; internally delegates to the member service.
 * Via gateway: {@code POST /api/auth/register}, {@code POST /api/auth/login}.
 */
@RestController
@RequestMapping("/auth")
public class AuthAccountController {

    private static final Logger log = LoggerFactory.getLogger(AuthAccountController.class);

    @Autowired
    private MemberApi memberApi;

    @Autowired
    private SmsCodeService smsCodeService;

    @Autowired
    private SmsProperties smsProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${gulimall.auth.session.cookie-domain:}")
    private String sessionCookieDomain;

    @Value("${gulimall.auth.session.cookie-secure:false}")
    private boolean sessionCookieSecure;

    @PostMapping("/sms/send")
    public R sendSms(@RequestBody SmsSendTo req) {
        if (req == null || StringUtils.isBlank(req.getMobile())) {
            return R.error(BisCodeEnum.MOBILE_NUMBER_REQUIRED);
        }
        return smsCodeService.sendCode(req.getMobile());
    }

    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterTo body) {
        if (body == null || StringUtils.isBlank(body.getMobile())) {
            return R.error(BisCodeEnum.REGISTRATION_REQUIRES_MOBILE_AND_SMS);
        }
        Optional<R> smsError = smsCodeService.validate(body.getMobile(), body.getSmsCode());
        if (smsError.isPresent()) {
            return smsError.get();
        }
        R result = registerMember(stripSmsCode(body));
        if (result != null && Integer.valueOf(0).equals(result.getCode())) {
            smsCodeService.consume(body.getMobile(), body.getSmsCode());
        }
        return result;
    }

    private R registerMember(MemberRegisterTo body) {
        SmsPhoneSupport.normalizeE164(body.getMobile(), smsProperties.getPhoneRegion())
                .ifPresent(body::setMobile);
        try {
            return memberApi.register(body);
        } catch (RestClientException e) {
            log.warn("Member register call failed", e);
            return R.error(BisCodeEnum.MEMBER_SERVICE_UNAVAILABLE);
        }
    }

    private static MemberRegisterTo stripSmsCode(MemberRegisterTo src) {
        MemberRegisterTo m = new MemberRegisterTo();
        m.setUsername(src.getUsername());
        m.setPassword(src.getPassword());
        m.setMobile(src.getMobile());
        m.setEmail(src.getEmail());
        return m;
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginTo body, HttpSession session) {
        try {
            R r = memberApi.login(body);
            if (r != null && Integer.valueOf(0).equals(r.getCode())) {
                Map<String, Object> member =
                        AuthOAuthSessionSupport.coerceMemberMap(r.get("member"), objectMapper);
                if (member != null && !member.isEmpty()) {
                    session.setAttribute(AuthOAuthSessionSupport.SESSION_MEMBER_KEY, member);
                }
            }
            return r;
        } catch (RestClientException e) {
            log.warn("Member login call failed", e);
            return R.error(BisCodeEnum.MEMBER_SERVICE_UNAVAILABLE);
        }
    }

    /**
     * Returns the member profile stored in the servlet session after Google OAuth or successful
     * password login ({@code POST /auth/login}); the mall uses it to fill {@code gulimall_session}
     * in localStorage.
     * <p>We keep the attribute for the servlet session lifetime (no read-once remove): duplicate
     * fetches (e.g. React Strict Mode, prefetch) were clearing it before the browser saved to
     * localStorage.</p>
     * <p>Via gateway: {@code GET /api/auth/oauth/member/session}.</p>
     */
    @GetMapping("/oauth/member/session")
    public R oauthMemberSession(HttpSession session) {
        Object raw = session.getAttribute(AuthOAuthSessionSupport.SESSION_MEMBER_KEY);
        if (raw instanceof Map<?, ?> map && !map.isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> copy = new LinkedHashMap<>((Map<String, Object>) map);
            return R.ok().put("member", copy);
        }
        // Stripe / redirects: Redis session exists but OAUTH_MEMBER_PROFILE was never stored (member API
        // flake). Re-fetch via Google principal when Spring Security still holds OAuth2AuthenticationToken.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User user = oauthToken.getPrincipal();
            Map<String, Object> attrs = user.getAttributes();
            MemberGoogleOAuthTo to = new MemberGoogleOAuthTo();
            to.setProvider(
                    oauthToken.getAuthorizedClientRegistrationId() != null
                            ? oauthToken.getAuthorizedClientRegistrationId()
                            : "google");
            Object sub = attrs != null ? attrs.get("sub") : null;
            to.setSubject(sub != null ? String.valueOf(sub) : null);
            Object email = attrs != null ? attrs.get("email") : null;
            to.setEmail(email != null ? String.valueOf(email) : null);
            Object name = attrs != null ? attrs.get("name") : null;
            to.setName(name != null ? String.valueOf(name) : null);
            Object picture = attrs != null ? attrs.get("picture") : null;
            to.setPicture(picture != null ? String.valueOf(picture) : null);
            try {
                R r = memberApi.oauthGoogle(to);
                if (r != null && Integer.valueOf(0).equals(r.getCode())) {
                    Map<String, Object> member =
                            AuthOAuthSessionSupport.coerceMemberMap(r.get("member"), objectMapper);
                    if (member != null && !member.isEmpty()) {
                        session.setAttribute(AuthOAuthSessionSupport.SESSION_MEMBER_KEY, member);
                        return R.ok().put("member", new LinkedHashMap<>(member));
                    }
                }
            } catch (RestClientException e) {
                log.warn("oauth member session rehydrate failed", e);
            }
        }
        return R.ok();
    }

    /**
     * Clears current auth server HttpSession so browser refresh will not restore logged-in member.
     * Via gateway: {@code POST /api/auth/logout}.
     */
    @PostMapping("/logout")
    public R logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        try {
            session.invalidate();
        } catch (IllegalStateException ignored) {
            // Session may already be invalidated; treat as idempotent logout.
        }
        // Proactively delete SESSION cookie on browser side. This avoids confusion where session is
        // invalidated server-side but the cookie still appears in DevTools until tab refresh.
        response.addHeader(HttpHeaders.SET_COOKIE, buildDeleteSessionCookie(null).toString());
        String configuredDomain = normalizeDomain(sessionCookieDomain);
        if (configuredDomain != null) {
            response.addHeader(HttpHeaders.SET_COOKIE, buildDeleteSessionCookie(configuredDomain).toString());
        } else {
            String hostDomain = topLevelDomain(request.getServerName());
            if (hostDomain != null) {
                response.addHeader(HttpHeaders.SET_COOKIE, buildDeleteSessionCookie(hostDomain).toString());
            }
        }
        return R.ok();
    }

    private ResponseCookie buildDeleteSessionCookie(String domain) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from("SESSION", "")
                .path("/")
                .httpOnly(true)
                .secure(sessionCookieSecure)
                .sameSite("Lax")
                .maxAge(Duration.ZERO);
        if (domain != null) {
            b.domain(domain);
        }
        return b.build();
    }

    private String normalizeDomain(String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        if (v.isEmpty()) return null;
        return v.startsWith(".") ? v.substring(1) : v;
    }

    private String topLevelDomain(String host) {
        if (host == null) return null;
        String h = host.trim().toLowerCase();
        if (h.isEmpty() || "localhost".equals(h) || h.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            return null;
        }
        String[] parts = h.split("\\.");
        if (parts.length < 2) return null;
        return parts[parts.length - 2] + "." + parts[parts.length - 1];
    }
}
