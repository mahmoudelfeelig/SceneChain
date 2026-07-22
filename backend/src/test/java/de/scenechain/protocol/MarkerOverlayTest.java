package de.scenechain.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.SecureRandom;
import org.junit.jupiter.api.Test;

class MarkerOverlayTest {
    @Test
    void overlayIsExactlyBalanced() {
        var overlay = MarkerOverlay.generate(new SecureRandom());
        assertThat(overlay).hasSize(96);
        for (int marker = 0; marker < 8; marker++) {
            int expected = marker;
            assertThat(overlay.stream().filter(value -> value == expected).count()).isEqualTo(12);
        }
    }

    @Test
    void fourCredentialCellsShareOneChallengeTile() {
        assertThat(Protocol.challengeTile(0)).isEqualTo(0);
        assertThat(Protocol.challengeTile(1)).isEqualTo(0);
        assertThat(Protocol.challengeTile(24)).isEqualTo(0);
        assertThat(Protocol.challengeTile(25)).isEqualTo(0);
        assertThat(Protocol.challengeTile(383)).isEqualTo(95);
    }
}
