CREATE TABLE study_subjects (
    subject_id UUID PRIMARY KEY,
    account_id UUID NOT NULL UNIQUE REFERENCES accounts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

INSERT INTO study_subjects(subject_id, account_id)
SELECT gen_random_uuid(), id FROM accounts;

UPDATE study_events event
SET subject_id = subject.subject_id
FROM study_subjects subject
WHERE event.subject_id = subject.account_id;

ALTER TABLE study_events
    ADD CONSTRAINT study_events_subject_fk
    FOREIGN KEY (subject_id) REFERENCES study_subjects(subject_id) ON DELETE CASCADE;

ALTER TABLE accounts
    ADD COLUMN consent_version VARCHAR(32) NOT NULL DEFAULT 'pre-versioned-prototype',
    ADD COLUMN withdrawn_at TIMESTAMPTZ;

CREATE TABLE study_sessions (
    subject_id UUID PRIMARY KEY REFERENCES study_subjects(subject_id) ON DELETE CASCADE,
    sequence_code VARCHAR(3) NOT NULL CHECK (sequence_code IN ('PDS','PSD','DPS','DSP','SPD','SDP')),
    period SMALLINT NOT NULL DEFAULT 0 CHECK (period BETWEEN 0 AND 2),
    phase VARCHAR(24) NOT NULL DEFAULT 'practice' CHECK (phase IN ('practice','measured','workload','retention','complete','withdrawn')),
    trial_number SMALLINT NOT NULL DEFAULT 0 CHECK (trial_number BETWEEN 0 AND 9),
    practice_successes SMALLINT NOT NULL DEFAULT 0 CHECK (practice_successes BETWEEN 0 AND 2),
    retention_period SMALLINT NOT NULL DEFAULT 0 CHECK (retention_period BETWEEN 0 AND 2),
    retention_due_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    viewport_width INTEGER NOT NULL CHECK (viewport_width BETWEEN 1024 AND 10000),
    viewport_height INTEGER NOT NULL CHECK (viewport_height BETWEEN 600 AND 10000),
    input_method VARCHAR(16) NOT NULL CHECK (input_method IN ('mouse','trackpad','keyboard','touch','other')),
    browser_family VARCHAR(16) NOT NULL CHECK (browser_family IN ('chromium','firefox','safari','other')),
    deviation_code VARCHAR(32),
    system_failure BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE study_events
    ADD COLUMN period SMALLINT CHECK (period BETWEEN 0 AND 2),
    ADD COLUMN phase VARCHAR(24),
    ADD COLUMN trial_number SMALLINT,
    ADD COLUMN first_attempt BOOLEAN,
    ADD COLUMN timed_out BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN viewport_width INTEGER,
    ADD COLUMN input_method VARCHAR(16),
    ADD COLUMN browser_family VARCHAR(16);

CREATE TABLE workload_responses (
    subject_id UUID NOT NULL REFERENCES study_subjects(subject_id) ON DELETE CASCADE,
    period SMALLINT NOT NULL CHECK (period BETWEEN 0 AND 2),
    mental SMALLINT NOT NULL CHECK (mental BETWEEN 0 AND 20),
    physical SMALLINT NOT NULL CHECK (physical BETWEEN 0 AND 20),
    temporal SMALLINT NOT NULL CHECK (temporal BETWEEN 0 AND 20),
    performance SMALLINT NOT NULL CHECK (performance BETWEEN 0 AND 20),
    effort SMALLINT NOT NULL CHECK (effort BETWEEN 0 AND 20),
    frustration SMALLINT NOT NULL CHECK (frustration BETWEEN 0 AND 20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (subject_id, period)
);

CREATE TABLE release_gate (
    singleton BOOLEAN PRIMARY KEY DEFAULT TRUE CHECK (singleton),
    protocol_sha256 CHAR(64),
    manifest_sha256 CHAR(64),
    preregistration_id VARCHAR(160),
    ethics_reference VARCHAR(160),
    data_protection_reference VARCHAR(160),
    approved_at TIMESTAMPTZ,
    approved_by VARCHAR(160),
    recruitment_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    CHECK (NOT recruitment_enabled OR (
        protocol_sha256 IS NOT NULL AND manifest_sha256 IS NOT NULL
        AND preregistration_id IS NOT NULL AND ethics_reference IS NOT NULL
        AND data_protection_reference IS NOT NULL AND approved_at IS NOT NULL
        AND approved_by IS NOT NULL
    ))
);

INSERT INTO release_gate(singleton) VALUES (TRUE);

CREATE TABLE research_audit_events (
    id BIGSERIAL PRIMARY KEY,
    actor_pseudonym CHAR(64) NOT NULL,
    action VARCHAR(48) NOT NULL,
    outcome VARCHAR(16) NOT NULL CHECK (outcome IN ('success','denied','failure')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
