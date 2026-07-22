package de.scenechain.study;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReleaseGateRepository {
    private final JdbcTemplate jdbc;
    private final String protocolSha256;

    public ReleaseGateRepository(JdbcTemplate jdbc,
            @Value("${scenechain.protocol-sha256:}") String protocolSha256) {
        this.jdbc = jdbc; this.protocolSha256 = protocolSha256;
    }

    public boolean approved(String manifestSha256) {
        if (protocolSha256.length() != 64 || manifestSha256.length() != 64) return false;
        Integer count = jdbc.queryForObject("""
            SELECT count(*) FROM release_gate WHERE singleton=TRUE AND recruitment_enabled=TRUE
              AND protocol_sha256=? AND manifest_sha256=? AND preregistration_id IS NOT NULL
              AND ethics_reference IS NOT NULL AND data_protection_reference IS NOT NULL
              AND approved_at IS NOT NULL AND approved_by IS NOT NULL
            """, Integer.class, protocolSha256, manifestSha256);
        return count != null && count == 1;
    }
}
