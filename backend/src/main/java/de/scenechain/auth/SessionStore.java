package de.scenechain.auth;

import de.scenechain.config.SceneChainProperties;
import de.scenechain.crypto.CredentialCrypto;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class SessionStore {
    private final StringRedisTemplate redis;
    private final CredentialCrypto crypto;
    private final Duration ttl;

    public SessionStore(StringRedisTemplate redis, CredentialCrypto crypto, SceneChainProperties properties) {
        this.redis = redis;
        this.crypto = crypto;
        this.ttl = Duration.ofSeconds(properties.sessionTtlSeconds());
    }

    public String create(UUID accountId) {
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(crypto.randomBytes(32));
        redis.opsForValue().set("session:" + crypto.keyedHandle(token),
            accountId + ":" + java.time.Instant.now().getEpochSecond(), ttl);
        return token;
    }

    public UUID get(String token) {
        if (token == null) return null;
        String id = redis.opsForValue().get("session:" + crypto.keyedHandle(token));
        if (id == null) return null;
        int separator = id.lastIndexOf(':');
        if (separator < 0) { delete(token); return null; }
        long issuedAt;
        try { issuedAt = Long.parseLong(id.substring(separator + 1)); }
        catch (NumberFormatException error) { delete(token); return null; }
        if (java.time.Instant.now().getEpochSecond() - issuedAt >= ttl.toSeconds()) {
            delete(token);
            return null;
        }
        return UUID.fromString(id.substring(0, separator));
    }

    public void delete(String token) {
        if (token != null) redis.delete("session:" + crypto.keyedHandle(token));
    }
}
