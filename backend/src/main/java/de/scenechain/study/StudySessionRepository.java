package de.scenechain.study;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class StudySessionRepository {
    private static final List<String> SEQUENCES = List.of("PDS", "PSD", "DPS", "DSP", "SPD", "SDP");
    public record State(UUID subjectId, String sequence, int period, String phase, int trialNumber,
                        int practiceSuccesses, int retentionPeriod, OffsetDateTime retentionDueAt,
                        int viewportWidth, int viewportHeight, String inputMethod, String browserFamily,
                        String deviationCode, boolean systemFailure) {
        public String condition() {
            if ("complete".equals(phase) || "withdrawn".equals(phase)) return phase;
            int index = "retention".equals(phase) ? retentionPeriod : period;
            return switch (sequence.charAt(index)) {
                case 'P' -> "password";
                case 'D' -> "direct";
                case 'S' -> "shielded";
                default -> throw new IllegalStateException("Invalid sequence");
            };
        }
        public boolean retentionReady() {
            return retentionDueAt == null || !OffsetDateTime.now().isBefore(retentionDueAt);
        }
    }

    private final JdbcTemplate jdbc;

    public StudySessionRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public State start(UUID accountId, int width, int height, String input, String browser) {
        State existing = findForAccount(accountId);
        if (existing != null) return existing;
        var counts = new java.util.HashMap<String, Integer>();
        SEQUENCES.forEach(sequence -> counts.put(sequence, 0));
        jdbc.query("SELECT sequence_code, count(*) AS total FROM study_sessions GROUP BY sequence_code",
            (org.springframework.jdbc.core.RowCallbackHandler) rs ->
                counts.put(rs.getString("sequence_code"), rs.getInt("total")));
        int minimum = counts.values().stream().min(Integer::compareTo).orElse(0);
        var candidates = new ArrayList<>(SEQUENCES.stream().filter(s -> counts.get(s) == minimum).toList());
        java.util.Collections.shuffle(candidates, new java.security.SecureRandom());
        jdbc.update("""
            INSERT INTO study_sessions(subject_id, sequence_code, viewport_width, viewport_height, input_method, browser_family)
            SELECT subject_id, ?, ?, ?, ?, ? FROM study_subjects WHERE account_id = ?
            """, candidates.getFirst(), width, height, input, browser, accountId);
        return requireForAccount(accountId);
    }

    public State find(UUID accountId) { return findForAccount(accountId); }

    @Transactional
    public State recordTrial(UUID accountId, String condition, boolean success, int totalMs, List<Integer> stageMs) {
        State state = lock(accountId);
        if (!condition.equals(state.condition()) || !(state.phase().equals("practice")
            || state.phase().equals("measured") || state.phase().equals("retention"))) {
            throw new IllegalStateException("Trial does not match assigned study state");
        }
        StudyEventRepository.validate(accountId, condition, success ? "success" : "failure", totalMs, stageMs, 0);
        jdbc.update("""
            INSERT INTO study_events(subject_id, condition, outcome, total_ms, stage_ms, retry_count,
                period, phase, trial_number, first_attempt, timed_out, viewport_class, input_method, browser_family,
                deviation_code, system_failure)
            VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, state.subjectId(), condition, success ? "success" : "failure", totalMs,
            stageMs.toArray(Integer[]::new), state.period(), state.phase(), state.trialNumber(),
            state.trialNumber() == 0, totalMs >= 180_000,
            state.viewportWidth() >= 1440 ? "desktop-wide" : "desktop-standard",
            state.inputMethod(), state.browserFamily(), state.deviationCode(), state.systemFailure());
        if (state.phase().equals("retention")) {
            if (!state.retentionReady()) throw new IllegalStateException("Retention trial is not due");
            if (state.retentionPeriod() == 2) {
                jdbc.update("UPDATE study_sessions SET phase='complete', completed_at=now(), updated_at=now() WHERE subject_id=?", state.subjectId());
            } else {
                jdbc.update("UPDATE study_sessions SET retention_period=retention_period+1, updated_at=now() WHERE subject_id=?", state.subjectId());
            }
        } else if (state.phase().equals("practice")) {
            if (success && state.practiceSuccesses() == 1) {
                jdbc.update("UPDATE study_sessions SET phase='measured', trial_number=0, practice_successes=2, updated_at=now() WHERE subject_id=?", state.subjectId());
            } else {
                jdbc.update("UPDATE study_sessions SET trial_number=LEAST(trial_number+1,9), practice_successes=practice_successes+?, updated_at=now() WHERE subject_id=?",
                    success ? 1 : 0, state.subjectId());
            }
        } else if (state.trialNumber() >= 2) {
            jdbc.update("UPDATE study_sessions SET phase='workload', trial_number=0, updated_at=now() WHERE subject_id=?", state.subjectId());
        } else {
            jdbc.update("UPDATE study_sessions SET trial_number=trial_number+1, updated_at=now() WHERE subject_id=?", state.subjectId());
        }
        return requireForAccount(accountId);
    }

    @Transactional
    public State workload(UUID accountId, int mental, int physical, int temporal, int performance, int effort, int frustration) {
        State state = lock(accountId);
        if (!"workload".equals(state.phase())) throw new IllegalStateException("Workload response is not due");
        jdbc.update("""
            INSERT INTO workload_responses(subject_id, period, mental, physical, temporal, performance, effort, frustration)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, state.subjectId(), state.period(), mental, physical, temporal, performance, effort, frustration);
        if (state.period() == 2) {
            jdbc.update("""
                UPDATE study_sessions SET phase='retention', retention_period=0,
                retention_due_at=now()+interval '7 days', trial_number=0, updated_at=now() WHERE subject_id=?
                """, state.subjectId());
        } else {
            jdbc.update("""
                UPDATE study_sessions SET period=period+1, phase='practice', trial_number=0,
                practice_successes=0, updated_at=now() WHERE subject_id=?
                """, state.subjectId());
        }
        return requireForAccount(accountId);
    }

    private State findForAccount(UUID accountId) {
        return jdbc.query("""
            SELECT s.* FROM study_sessions s JOIN study_subjects u USING(subject_id) WHERE u.account_id=?
            """, this::map, accountId).stream().findFirst().orElse(null);
    }

    private State requireForAccount(UUID accountId) {
        State state = findForAccount(accountId);
        if (state == null) throw new IllegalStateException("Study session not started");
        return state;
    }

    private State lock(UUID accountId) {
        return jdbc.query("""
            SELECT s.* FROM study_sessions s JOIN study_subjects u USING(subject_id)
            WHERE u.account_id=? FOR UPDATE OF s
            """, this::map, accountId).stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("Study session not started"));
    }

    private State map(ResultSet rs, int row) throws SQLException {
        return new State(rs.getObject("subject_id", UUID.class), rs.getString("sequence_code"), rs.getInt("period"),
            rs.getString("phase"), rs.getInt("trial_number"), rs.getInt("practice_successes"),
            rs.getInt("retention_period"), rs.getObject("retention_due_at", OffsetDateTime.class),
            rs.getInt("viewport_width"), rs.getInt("viewport_height"), rs.getString("input_method"),
            rs.getString("browser_family"), rs.getString("deviation_code"), rs.getBoolean("system_failure"));
    }
}
