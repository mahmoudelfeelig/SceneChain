package de.scenechain.user;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class HotspotAggregateRepository {
    static final String UPSERT_SQL = """
        INSERT INTO hotspot_counts(scene_id, scene_version, cell_id, action_id, count)
        VALUES (?, ?, ?, ?, 1)
        ON CONFLICT (scene_id, scene_version, cell_id, action_id)
        DO UPDATE SET count = hotspot_counts.count + 1
        """;
    public record Hotspot(int sceneId, int sceneVersion, int cellId, int actionId) {}
    private final JdbcTemplate jdbc;

    public HotspotAggregateRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increment(List<Hotspot> stages) {
        for (Hotspot stage : stages) {
            jdbc.update(UPSERT_SQL, stage.sceneId(), stage.sceneVersion(), stage.cellId(), stage.actionId());
        }
    }
}
