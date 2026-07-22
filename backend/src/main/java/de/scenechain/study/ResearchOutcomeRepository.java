package de.scenechain.study;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ResearchOutcomeRepository {
    public record OutcomeRow(String id, UUID subjectId, String type, String condition,
                             String primaryValue, String secondaryValue, OffsetDateTime createdAt) {}
    private final JdbcTemplate jdbc;

    public ResearchOutcomeRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean observer(String handle, String condition, boolean success) {
        return jdbc.update("""
            INSERT INTO observer_outcomes(subject_id, observed_condition, complete_chain_success,
                observation_count, attempt_count, recording_used)
            SELECT s.subject_id, ?, ?, 1, 1, FALSE FROM study_subjects s
            JOIN accounts a ON a.id=s.account_id WHERE a.handle=? AND a.enabled=TRUE
            ON CONFLICT DO NOTHING
            """, condition, success, handle) == 1;
    }

    public boolean lockout(String handle, int attempts, int retryAfter, boolean waitCommunicated) {
        return jdbc.update("""
            INSERT INTO lockout_outcomes(subject_id, attempts_until_throttle, retry_after_seconds,
                wait_communicated, disposable_account)
            SELECT s.subject_id, ?, ?, ?, TRUE FROM study_subjects s
            JOIN accounts a ON a.id=s.account_id WHERE a.handle=? AND a.enabled=TRUE
            ON CONFLICT DO NOTHING
            """, attempts, retryAfter, waitCommunicated, handle) == 1;
    }

    public boolean report(String handle, String accessibilityCode, boolean recoveryUsed) {
        return jdbc.update("""
            INSERT INTO participant_reports(subject_id, accessibility_code, recovery_used)
            SELECT s.subject_id, ?, ? FROM study_subjects s
            JOIN accounts a ON a.id=s.account_id WHERE a.handle=? AND a.enabled=TRUE
            ON CONFLICT DO NOTHING
            """, accessibilityCode, recoveryUsed, handle) == 1;
    }

    public List<OutcomeRow> export(OffsetDateTime from, OffsetDateTime to, int limit) {
        return jdbc.query("""
            SELECT outcome_id, subject_id, outcome_type, condition, primary_value, secondary_value, created_at
            FROM (
              SELECT 'observer-'||id AS outcome_id, subject_id, 'observer' AS outcome_type,
                observed_condition AS condition, complete_chain_success::text AS primary_value,
                'one-view;one-attempt;no-recording' AS secondary_value, created_at FROM observer_outcomes
              UNION ALL
              SELECT 'lockout-'||id, subject_id, 'lockout', NULL, attempts_until_throttle::text,
                'retry-after='||retry_after_seconds||';communicated='||wait_communicated, created_at FROM lockout_outcomes
              UNION ALL
              SELECT 'report-'||id, subject_id, 'participant-report', NULL, accessibility_code,
                'recovery-used='||recovery_used, created_at FROM participant_reports
            ) outcomes WHERE created_at >= ? AND created_at < ? ORDER BY created_at, outcome_id LIMIT ?
            """, (rs, row) -> new OutcomeRow(rs.getString("outcome_id"), rs.getObject("subject_id", UUID.class),
                rs.getString("outcome_type"), rs.getString("condition"), rs.getString("primary_value"),
                rs.getString("secondary_value"), rs.getObject("created_at", OffsetDateTime.class)), from, to, limit);
    }
}
