package de.scenechain.user;

import org.junit.jupiter.api.Test;

class HotspotAggregateRepositoryTest {
    @Test
    void bindsSceneVersionInsteadOfHardCodingIt() {
        org.assertj.core.api.Assertions.assertThat(
            HotspotAggregateRepository.UPSERT_SQL.chars().filter(value -> value == '?').count()).isEqualTo(4);
        org.assertj.core.api.Assertions.assertThat(HotspotAggregateRepository.UPSERT_SQL)
            .contains("scene_version", "VALUES (?, ?, ?, ?, 1)");
    }
}
