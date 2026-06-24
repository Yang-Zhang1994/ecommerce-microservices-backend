package com.atguigu.gulimall.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Uses a host-only {@code SESSION} cookie on loopback hosts so Google OAuth (and other flows) work
 * while {@code GULIMALL_SESSION_COOKIE_DOMAIN=.ecommerce.com} stays set for production.
 * <p>Spring Session's {@link DefaultCookieSerializer} validates that cookie domain matches request host;
 * {@code Domain=ecommerce.com} + {@code Host: localhost} throws and yields HTTP 500.</p>
 */
final class HostAwareCookieSerializer implements CookieSerializer {

    private final CookieSerializer withConfiguredDomain;
    private final CookieSerializer localhostOnly;

    HostAwareCookieSerializer(String cookieDomain, boolean cookieSecure) {
        this.withConfiguredDomain = buildDelegate(cookieDomain, cookieSecure);
        this.localhostOnly = buildDelegate(null, cookieSecure);
    }

    private static DefaultCookieSerializer buildDelegate(String cookieDomain, boolean cookieSecure) {
        DefaultCookieSerializer s = new DefaultCookieSerializer();
        s.setCookieName("SESSION");
        s.setCookiePath("/");
        String normalized = normalizeSpringSessionDomain(cookieDomain);
        if (StringUtils.hasText(normalized)) {
            // Spring Session rejects leading "." here (Invalid cookie domain: .example.com)
            s.setDomainName(normalized);
        }
        s.setUseHttpOnlyCookie(true);
        s.setUseSecureCookie(cookieSecure);
        s.setSameSite("Lax");
        return s;
    }

    /**
     * Strips a leading dot ({@code .ecommerce.com} → {@code ecommerce.com}) for {@link DefaultCookieSerializer}.
     */
    static String normalizeSpringSessionDomain(String cookieDomain) {
        if (!StringUtils.hasText(cookieDomain)) {
            return null;
        }
        String d = cookieDomain.trim();
        if (d.startsWith(".")) {
            d = d.substring(1);
        }
        return d.isEmpty() ? null : d;
    }

    static boolean isLoopbackHost(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        return isLoopbackHost(request.getServerName());
    }

    static boolean isLoopbackHost(String serverName) {
        if (!StringUtils.hasText(serverName)) {
            return false;
        }
        String h = serverName.trim().toLowerCase();
        return "localhost".equals(h)
                || "127.0.0.1".equals(h)
                || "::1".equals(h)
                || "[::1]".equals(serverName.trim());
    }

    private CookieSerializer delegate(HttpServletRequest request) {
        return isLoopbackHost(request) ? localhostOnly : withConfiguredDomain;
    }

    @Override
    public List<String> readCookieValues(HttpServletRequest request) {
        return delegate(request).readCookieValues(request);
    }

    @Override
    public void writeCookieValue(CookieValue cookieValue) {
        HttpServletRequest request = cookieValue.getRequest();
        delegate(request).writeCookieValue(cookieValue);
    }
}
