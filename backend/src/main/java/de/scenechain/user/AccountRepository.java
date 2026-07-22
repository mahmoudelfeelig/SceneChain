package de.scenechain.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AccountRepository {
    public record Account(UUID id, String handle, boolean enabled) {}
    public record Credential(UUID accountId, byte[] salt, byte[] verifier, byte[] encryptedMetadata) {}

    private final JdbcTemplate jdbc;

    public AccountRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<Account> findByHandle(String handle) {
        return jdbc.query("SELECT id, handle, enabled FROM accounts WHERE handle = ?", this::account, handle)
            .stream().findFirst();
    }

    public Optional<Account> findById(UUID id) {
        return jdbc.query("SELECT id, handle, enabled FROM accounts WHERE id = ?", this::account, id)
            .stream().findFirst();
    }

    public Optional<Credential> credential(UUID accountId) {
        return jdbc.query("SELECT account_id, salt, verifier, encrypted_metadata FROM graphical_credentials WHERE account_id = ?",
            (rs, row) -> new Credential(rs.getObject("account_id", UUID.class), rs.getBytes("salt"),
                rs.getBytes("verifier"), rs.getBytes("encrypted_metadata")), accountId).stream().findFirst();
    }

    public Optional<Credential> password(UUID accountId) {
        return jdbc.query("SELECT account_id, salt, verifier, ''::bytea AS encrypted_metadata FROM password_credentials WHERE account_id = ?",
            (rs, row) -> new Credential(rs.getObject("account_id", UUID.class), rs.getBytes("salt"),
                rs.getBytes("verifier"), rs.getBytes("encrypted_metadata")), accountId).stream().findFirst();
    }

    public List<Integer> scenes(UUID accountId) {
        return jdbc.queryForList("SELECT scene_id FROM scene_assignments WHERE account_id = ? ORDER BY stage_index",
            Integer.class, accountId);
    }

    @Transactional
    public void delete(UUID accountId) {
        jdbc.update("DELETE FROM accounts WHERE id = ?", accountId);
    }

    @Transactional
    public int deleteSubjectsBefore(java.time.OffsetDateTime cutoff) {
        return jdbc.update("""
            DELETE FROM accounts a USING study_subjects s
            WHERE s.account_id=a.id AND s.created_at < ?
            """, cutoff);
    }

    @Transactional
    public void create(UUID accountId, String handle, java.time.OffsetDateTime consentedAt,
                       byte[] salt, byte[] verifier, byte[] encrypted,
                       byte[] passwordSalt, byte[] passwordVerifier, List<Integer> sceneIds, List<Integer> sceneVersions) {
        jdbc.update("INSERT INTO accounts(id, handle, consented_at, consent_version) VALUES (?, ?, ?, 'scenechain-consent-2026-07-22')",
            accountId, handle, consentedAt);
        jdbc.update("INSERT INTO study_subjects(subject_id, account_id) VALUES (?, ?)", UUID.randomUUID(), accountId);
        jdbc.update("INSERT INTO graphical_credentials(account_id, protocol_version, policy_version, salt, verifier, encrypted_metadata) VALUES (?, 1, 1, ?, ?, ?)",
            accountId, salt, verifier, encrypted);
        jdbc.update("INSERT INTO password_credentials(account_id, salt, verifier) VALUES (?, ?, ?)",
            accountId, passwordSalt, passwordVerifier);
        for (int i = 0; i < sceneIds.size(); i++) {
            jdbc.update("INSERT INTO scene_assignments(account_id, stage_index, scene_id, scene_version) VALUES (?, ?, ?, ?)",
                accountId, i, sceneIds.get(i), sceneVersions.get(i));
        }
    }

    private Account account(ResultSet rs, int row) throws SQLException {
        return new Account(rs.getObject("id", UUID.class), rs.getString("handle"), rs.getBoolean("enabled"));
    }
}
