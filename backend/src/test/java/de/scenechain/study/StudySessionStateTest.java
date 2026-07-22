package de.scenechain.study;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StudySessionStateTest {
    @Test
    void resolvesImmediateAndRetentionConditionsFromTheFrozenSequence() {
        var immediate = new StudySessionRepository.State(UUID.randomUUID(), "PDS", 1, "measured", 0, 2,
            0, null, 1440, 900, "mouse", "chromium", null, false);
        var retention = new StudySessionRepository.State(immediate.subjectId(), "PDS", 2, "retention", 0, 0,
            2, OffsetDateTime.now().minusSeconds(1), 1440, 900, "mouse", "chromium", null, false);
        assertThat(immediate.condition()).isEqualTo("direct");
        assertThat(retention.condition()).isEqualTo("shielded");
        assertThat(retention.retentionReady()).isTrue();
    }
}
