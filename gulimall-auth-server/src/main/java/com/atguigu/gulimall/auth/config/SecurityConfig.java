package com.atguigu.gulimall.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * OAuth2 endpoints live under {@code /auth/...} so the gateway can expose them as
     * {@code /api/auth/...} (RewritePath strips {@code /api}).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
            @Value("${gulimall.auth.oauth2.failure-target-url:http://localhost:88/login?error}")
                    String oauth2FailureTargetUrl)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/actuator/**",
                                                "/auth/**",
                                                "/error")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .oauth2Login(
                        oauth2 ->
                                oauth2.authorizationEndpoint(
                                                a -> a.baseUri("/auth/oauth2/authorization"))
                                        .redirectionEndpoint(
                                                r ->
                                                        r.baseUri("/auth/login/oauth2/code/*"))
                                        .successHandler(oauth2LoginSuccessHandler)
                                        .failureHandler(oauth2FailureHandler(oauth2FailureTargetUrl)));
        return http.build();
    }

    private static AuthenticationFailureHandler oauth2FailureHandler(String failureUrl) {
        SimpleUrlAuthenticationFailureHandler handler = new SimpleUrlAuthenticationFailureHandler();
        handler.setDefaultFailureUrl(
                failureUrl != null && !failureUrl.isBlank()
                        ? failureUrl.trim()
                        : "http://localhost:88/login?error");
        return handler;
    }
}
