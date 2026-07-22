ALTER TABLE accounts ADD COLUMN consented_at TIMESTAMPTZ;

CREATE INDEX study_events_created_at_idx ON study_events(created_at);
