CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    handle VARCHAR(32) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE graphical_credentials (
    account_id UUID PRIMARY KEY REFERENCES accounts(id) ON DELETE CASCADE,
    protocol_version SMALLINT NOT NULL,
    policy_version SMALLINT NOT NULL,
    salt BYTEA NOT NULL,
    verifier BYTEA NOT NULL,
    encrypted_metadata BYTEA NOT NULL,
    key_version SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE password_credentials (
    account_id UUID PRIMARY KEY REFERENCES accounts(id) ON DELETE CASCADE,
    salt BYTEA NOT NULL,
    verifier BYTEA NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE scene_assignments (
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    stage_index SMALLINT NOT NULL,
    scene_id INTEGER NOT NULL,
    scene_version SMALLINT NOT NULL,
    PRIMARY KEY (account_id, stage_index)
);

CREATE TABLE hotspot_counts (
    scene_id INTEGER NOT NULL,
    scene_version SMALLINT NOT NULL,
    cell_id SMALLINT NOT NULL,
    action_id SMALLINT NOT NULL,
    count BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (scene_id, scene_version, cell_id, action_id)
);

CREATE TABLE study_events (
    id BIGSERIAL PRIMARY KEY,
    subject_id UUID NOT NULL,
    condition VARCHAR(24) NOT NULL,
    outcome VARCHAR(24) NOT NULL,
    total_ms INTEGER NOT NULL CHECK (total_ms >= 0),
    stage_ms INTEGER[] NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0 CHECK (retry_count >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX study_events_subject_idx ON study_events(subject_id);
