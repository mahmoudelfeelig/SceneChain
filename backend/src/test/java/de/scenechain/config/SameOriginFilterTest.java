package de.scenechain.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SameOriginFilterTest {
    private final SameOriginFilter filter = new SameOriginFilter();

    @Test
    void permitsMatchingOriginAndRejectsCrossSiteOrigin() throws Exception {
        var allowed = request("http://localhost:8088", "same-origin");
        var allowedResponse = new MockHttpServletResponse();
        boolean[] called = {false};
        FilterChain chain = (request, response) -> called[0] = true;
        filter.doFilter(allowed, allowedResponse, chain);
        assertThat(called[0]).isTrue();

        var rejected = request("https://attacker.example", "cross-site");
        var rejectedResponse = new MockHttpServletResponse();
        filter.doFilter(rejected, rejectedResponse, chain);
        assertThat(rejectedResponse.getStatus()).isEqualTo(403);
    }

    private MockHttpServletRequest request(String origin, String fetchSite) {
        var request = new MockHttpServletRequest("POST", "/api/auth/attempts");
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8088);
        request.addHeader("Origin", origin);
        request.addHeader("Sec-Fetch-Site", fetchSite);
        return request;
    }
}
