package de.scenechain.study;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StudyEventRepository {
    public record ExportRow(long id, UUID subjectId, String condition, String outcome,
                            int totalMs, Integer[] stageMs, int retryCount, Integer period, String phase,
                            Integer trialNumber, Boolean firstAttempt, boolean timedOut, String viewportClass,
                            String inputMethod, String browserFamily, String deviationCode, boolean systemFailure,
                            java.time.OffsetDateTime createdAt) {}

    private static final java.util.Set<String> CONDITIONS = java.util.Set.of("password", "direct", "shielded", "enrollment");
    private static final java.util.Set<String> OUTCOMES = java.util.Set.of("success", "failure", "abandoned");
    private final JdbcTemplate jdbc;

    public StudyEventRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void record(UUID subjectId, String condition, String outcome, int totalMs, List<Integer> stageMs, int retryCount) {
        validate(subjectId, condition, outcome, totalMs, stageMs, retryCount);
        jdbc.update("""
            INSERT INTO study_events(subject_id, condition, outcome, total_ms, stage_ms, retry_count)
            SELECT subject_id, ?, ?, ?, ?, ? FROM study_subjects WHERE account_id = ?
            """, condition, outcome, totalMs, stageMs.toArray(Integer[]::new), retryCount, subjectId);
    }

    static void validate(UUID subjectId, String condition, String outcome, int totalMs, List<Integer> stageMs, int retryCount) {
        if (subjectId == null || !CONDITIONS.contains(condition) || !OUTCOMES.contains(outcome)
            || totalMs < 0 || totalMs > 3_600_000 || retryCount < 0 || retryCount > 100
            || stageMs == null || stageMs.size() > 5 || stageMs.stream().anyMatch(ms -> ms == null || ms < 0 || ms > 600_000)) {
            throw new IllegalArgumentException("Invalid allowlisted study event");
        }
    }

    public List<ExportRow> exportRows(java.time.OffsetDateTime from, java.time.OffsetDateTime to, int limit) {
        return jdbc.query("""
            SELECT id, subject_id, condition, outcome, total_ms, stage_ms, retry_count, period, phase,
                   trial_number, first_attempt, timed_out, viewport_class, input_method, browser_family,
                   deviation_code, system_failure, created_at
            FROM study_events WHERE created_at >= ? AND created_at < ? ORDER BY id LIMIT ?
            """,
            (rs, row) -> new ExportRow(rs.getLong("id"), rs.getObject("subject_id", UUID.class), rs.getString("condition"),
                rs.getString("outcome"), rs.getInt("total_ms"), (Integer[]) rs.getArray("stage_ms").getArray(),
                rs.getInt("retry_count"), (Integer) rs.getObject("period"), rs.getString("phase"),
                (Integer) rs.getObject("trial_number"), (Boolean) rs.getObject("first_attempt"),
                rs.getBoolean("timed_out"), rs.getString("viewport_class"), rs.getString("input_method"),
                rs.getString("browser_family"), rs.getString("deviation_code"), rs.getBoolean("system_failure"),
                rs.getObject("created_at", java.time.OffsetDateTime.class)), from, to, limit);
    }
}
