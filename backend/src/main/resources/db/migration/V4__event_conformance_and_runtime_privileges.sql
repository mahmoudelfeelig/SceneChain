ALTER TABLE study_events
    DROP COLUMN viewport_width,
    ADD COLUMN viewport_class VARCHAR(24) CHECK (viewport_class IN ('desktop-standard','desktop-wide')),
    ADD COLUMN deviation_code VARCHAR(32),
    ADD COLUMN system_failure BOOLEAN NOT NULL DEFAULT FALSE;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'scenechain_app') THEN
        REVOKE INSERT, UPDATE, DELETE ON release_gate FROM scenechain_app;
    END IF;
END
$$;
