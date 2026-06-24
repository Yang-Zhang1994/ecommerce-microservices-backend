package com.atguigu.gulimall.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.atguigu.common.client.MemberApi;
import com.atguigu.common.to.member.MemberGoogleOAuthTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.support.AuthOAuthSessionSupport;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.Map;

/**
 * After Google OAuth, creates/links the mall member via member-service and stores the profile in
 * session for {@code GET /auth/oauth/member/session} on the mall origin.
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final MemberApi memberApi;

    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(
            MemberApi memberApi,
            ObjectMapper objectMapper,
            @Value("${gulimall.auth.oauth2.default-target-url:http://localhost:3001/}")
                    String oauth2DefaultTargetUrl) {
        this.memberApi = memberApi;
        this.objectMapper = objectMapper;
        setDefaultTargetUrl(
                oauth2DefaultTargetUrl != null && !oauth2DefaultTargetUrl.isBlank()
                        ? oauth2DefaultTargetUrl.trim()
                        : "http://localhost:3001/");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User user = oauthToken.getPrincipal();
            Map<String, Object> attrs = user.getAttributes();
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();

            MemberGoogleOAuthTo to = new MemberGoogleOAuthTo();
            to.setProvider(registrationId != null ? registrationId : "google");
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
                        request.getSession().setAttribute(AuthOAuthSessionSupport.SESSION_MEMBER_KEY, member);
                        log.debug("Stored OAuth member session for subject {}", to.getSubject());
                    } else {
                        log.warn(
                                "Member oauth/google returned code=0 but usable member map was empty (type={})",
                                r.get("member") != null ? r.get("member").getClass().getName() : "null");
                    }
                } else {
                    log.warn(
                            "Member oauth/google returned non-success: {}",
                            r != null ? r.get("msg") : null);
                }
            } catch (RestClientException e) {
                log.warn("Member oauth/google call failed", e);
            }
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
