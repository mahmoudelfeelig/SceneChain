package de.scenechain.study;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class RetentionServiceTest {
    @Test
    void choosesEarlierOfCollectionAndPublicationDeadlines() {
        assertThat(RetentionService.cutoff("2025-01-01T00:00:00Z", "2026-01-01T00:00:00Z"))
            .isEqualTo(OffsetDateTime.parse("2026-07-01T00:00:00Z"));
    }
}
