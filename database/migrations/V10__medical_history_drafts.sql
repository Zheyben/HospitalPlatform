CREATE TABLE encounter_drafts (
    encounter_id UUID PRIMARY KEY REFERENCES clinical_encounters(id) ON DELETE RESTRICT,
    version INTEGER NOT NULL CHECK (version > 0),
    content JSONB NOT NULL CHECK (jsonb_typeof(content) = 'object'),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

REVOKE ALL ON encounter_drafts FROM PUBLIC;
