package de.scenechain.study;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ResearchAdminAccess {
    private final JdbcTemplate jdbc;
    private final String adminUser;
    private final byte[] adminPassword;
    private final byte[] pseudonymKey;
    private final boolean secureCookies;

    public ResearchAdminAccess(JdbcTemplate jdbc,
            @Value("${scenechain.research.admin-user:}") String adminUser,
            @Value("${scenechain.research.admin-password:}") String adminPassword,
            @Value("${scenechain.research.pseudonym-key:}") String pseudonymKey,
            @Value("${scenechain.cookie-secure:false}") boolean secureCookies) {
        this.jdbc = jdbc; this.adminUser = adminUser;
        this.adminPassword = adminPassword.getBytes(StandardCharsets.UTF_8);
        this.pseudonymKey = pseudonymKey.getBytes(StandardCharsets.UTF_8);
        this.secureCookies = secureCookies;
    }

    public String authorize(String authorization, HttpServletRequest request, String action) {
        String actor = "anonymous";
        try {
            if (authorization == null || !authorization.startsWith("Basic ") || adminUser.isBlank()
                || adminPassword.length < 20 || pseudonymKey.length < 32) throw new IllegalArgumentException();
            String decoded = new String(Base64.getDecoder().decode(authorization.substring(6)), StandardCharsets.UTF_8);
            int separator = decoded.indexOf(':');
            if (separator < 1) throw new IllegalArgumentException();
            actor = decoded.substring(0, separator);
            if (!MessageDigest.isEqual(adminUser.getBytes(StandardCharsets.UTF_8), actor.getBytes(StandardCharsets.UTF_8))
                || !MessageDigest.isEqual(adminPassword, decoded.substring(separator + 1).getBytes(StandardCharsets.UTF_8))
                || (secureCookies && !request.isSecure())) throw new IllegalArgumentException();
            return actor;
        } catch (RuntimeException error) {
            audit(actor, action, "denied");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public void success(String actor, String action) { audit(actor, action, "success"); }
    public void denied(String actor, String action) { audit(actor, action, "denied"); }

    public String pseudonym(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(pseudonymKey, "HmacSHA256"));
            return java.util.HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.US_ASCII))).substring(0, 24);
        } catch (Exception error) { throw new IllegalStateException("Pseudonymisation failed", error); }
    }

    private void audit(String actor, String action, String outcome) {
        String safeActor = pseudonymKey.length < 32 ? "0".repeat(64) : pseudonym(actor);
        jdbc.update("INSERT INTO research_audit_events(actor_pseudonym, action, outcome) VALUES (?, ?, ?)",
            safeActor, action, outcome);
    }
}
