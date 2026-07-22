CREATE TABLE observer_outcomes (
    id BIGSERIAL PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES study_subjects(subject_id) ON DELETE CASCADE,
    observed_condition VARCHAR(16) NOT NULL CHECK (observed_condition IN ('direct','shielded')),
    complete_chain_success BOOLEAN NOT NULL,
    observation_count SMALLINT NOT NULL CHECK (observation_count = 1),
    attempt_count SMALLINT NOT NULL CHECK (attempt_count = 1),
    recording_used BOOLEAN NOT NULL CHECK (recording_used = FALSE),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (subject_id, observed_condition)
);

CREATE TABLE lockout_outcomes (
    id BIGSERIAL PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES study_subjects(subject_id) ON DELETE CASCADE,
    attempts_until_throttle SMALLINT NOT NULL CHECK (attempts_until_throttle BETWEEN 1 AND 20),
    retry_after_seconds INTEGER NOT NULL CHECK (retry_after_seconds BETWEEN 1 AND 3600),
    wait_communicated BOOLEAN NOT NULL,
    disposable_account BOOLEAN NOT NULL CHECK (disposable_account = TRUE),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (subject_id)
);

CREATE TABLE participant_reports (
    id BIGSERIAL PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES study_subjects(subject_id) ON DELETE CASCADE,
    accessibility_code VARCHAR(16) NOT NULL CHECK (accessibility_code IN ('none','visual','motor','cognitive','multiple','other')),
    recovery_used BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (subject_id)
);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'scenechain_app') THEN
        REVOKE UPDATE, DELETE ON observer_outcomes, lockout_outcomes, participant_reports FROM scenechain_app;
    END IF;
END
$$;
