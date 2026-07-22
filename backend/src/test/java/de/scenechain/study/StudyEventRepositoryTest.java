package de.scenechain.study;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StudyEventRepositoryTest {
    @Test
    void acceptsOnlyFixedResearchShape() {
        UUID subject = UUID.randomUUID();
        assertThatCode(() -> StudyEventRepository.validate(subject, "direct", "success", 1200,
            List.of(200, 210, 220, 230, 240), 0)).doesNotThrowAnyException();
    }

    @Test
    void rejectsUnknownConditionOutcomeAndUnboundedTiming() {
        UUID subject = UUID.randomUUID();
        assertThatThrownBy(() -> StudyEventRepository.validate(subject, "custom", "success", 1, List.of(), 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StudyEventRepository.validate(subject, "direct", "maybe", 1, List.of(), 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StudyEventRepository.validate(subject, "direct", "success", 3_600_001, List.of(), 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StudyEventRepository.validate(subject, "direct", "success", 1, List.of(600_001), 0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
