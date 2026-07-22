package de.scenechain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scenechain")
public record SceneChainProperties(
        boolean cookieSecure,
        int attemptTtlSeconds,
        int sessionTtlSeconds,
        Keys keys) {
    public record Keys(String pepper, String metadata, String lookup, String synthetic) {}
}
