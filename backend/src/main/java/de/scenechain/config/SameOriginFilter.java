package de.scenechain.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SameOriginFilter extends OncePerRequestFilter {
    private static final Set<String> SAFE = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (SAFE.contains(request.getMethod()) || !request.getRequestURI().startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }
        String fetchSite = request.getHeader("Sec-Fetch-Site");
        if ("cross-site".equalsIgnoreCase(fetchSite)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String origin = request.getHeader("Origin");
        if (origin != null && !sameOrigin(request, origin)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean sameOrigin(HttpServletRequest request, String origin) {
        try {
            URI value = new URI(origin);
            int requestPort = request.getServerPort();
            int originPort = value.getPort() == -1 ? ("https".equalsIgnoreCase(value.getScheme()) ? 443 : 80) : value.getPort();
            return request.getScheme().equalsIgnoreCase(value.getScheme())
                && request.getServerName().equalsIgnoreCase(value.getHost())
                && requestPort == originPort;
        } catch (URISyntaxException error) {
            return false;
        }
    }
}
