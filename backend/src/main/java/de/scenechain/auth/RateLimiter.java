package de.scenechain.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class RateLimiter {
    private static final DefaultRedisScript<Long> SLIDING_WINDOW = new DefaultRedisScript<>("""
        local key = KEYS[1]
        local cutoff = tonumber(ARGV[1])
        local now = tonumber(ARGV[2])
        local limit = tonumber(ARGV[3])
        redis.call('ZREMRANGEBYSCORE', key, 0, cutoff)
        if redis.call('ZCARD', key) >= limit then return 0 end
        redis.call('ZADD', key, now, ARGV[4])
        redis.call('PEXPIRE', key, tonumber(ARGV[5]))
        return 1
        """, Long.class);
    private final StringRedisTemplate redis;

    public RateLimiter(StringRedisTemplate redis) { this.redis = redis; }

    public boolean allow(String bucket, int limit, Duration window) {
        long now = Instant.now().toEpochMilli();
        long cutoff = now - window.toMillis();
        Long result = redis.execute(SLIDING_WINDOW, List.of(bucket),
            String.valueOf(cutoff), String.valueOf(now), String.valueOf(limit),
            now + ":" + UUID.randomUUID(), String.valueOf(window.plusSeconds(5).toMillis()));
        return result != null && result == 1L;
    }
}
