package de.scenechain.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.scenechain.config.SceneChainProperties;
import de.scenechain.crypto.CredentialCrypto;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CredentialCryptoTest {
    private final CredentialCrypto crypto = new CredentialCrypto(new SceneChainProperties(false, 300, 3600,
        new SceneChainProperties.Keys("pepper", "metadata", "lookup", "synthetic")));

    @Test
    void verifierAndMetadataAreAccountBound() {
        UUID account = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        byte[] value = "credential".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] salt = crypto.randomBytes(16);
        byte[] verifier = crypto.verifier(value, salt, account);
        assertThat(crypto.verify(value, salt, account, verifier)).isTrue();
        assertThat(crypto.verify(value, salt, other, verifier)).isFalse();

        byte[] encrypted = crypto.encrypt(value, account);
        assertThat(crypto.decrypt(encrypted, account)).isEqualTo(value);
        assertThatThrownBy(() -> crypto.decrypt(encrypted, other)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsExampleOrReusedProductionKeys() {
        assertThatThrownBy(() -> new CredentialCrypto(new SceneChainProperties(true, 300, 3600,
            new SceneChainProperties.Keys("development-pepper-change-me", "a".repeat(32),
                "b".repeat(32), "c".repeat(32)))))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new CredentialCrypto(new SceneChainProperties(true, 300, 3600,
            new SceneChainProperties.Keys("a".repeat(32), "a".repeat(32),
                "b".repeat(32), "c".repeat(32)))))
            .isInstanceOf(IllegalStateException.class);
    }
}
