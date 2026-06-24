package com.atguigu.gulimall.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Logs every inbound HTTP request at INFO so Docker logs show traffic (Spring Boot does not access-log
 * to stdout by default). Remove or narrow once OAuth debugging is done.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class InboundHttpLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InboundHttpLogFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("{} {}", request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}
