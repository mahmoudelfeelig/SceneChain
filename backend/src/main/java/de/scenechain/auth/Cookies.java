package de.scenechain.auth;

import de.scenechain.config.SceneChainProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class Cookies {
    public static final String ATTEMPT = "SC_ATTEMPT";
    public static final String ENROLLMENT = "SC_ENROLLMENT";
    public static final String SESSION = "SC_SESSION";
    private final SceneChainProperties properties;

    public Cookies(SceneChainProperties properties) { this.properties = properties; }

    public void set(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
            .httpOnly(true).secure(properties.cookieSecure()).sameSite("Strict")
            .path("/").maxAge(maxAge).build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clear(HttpServletResponse response, String name) { set(response, name, "", 0); }

    public static String read(Cookie[] cookies, String name) {
        if (cookies == null) return null;
        for (Cookie cookie : cookies) if (name.equals(cookie.getName())) return cookie.getValue();
        return null;
    }
}
