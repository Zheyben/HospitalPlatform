CREATE TABLE clinical_encounters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID NOT NULL UNIQUE REFERENCES appointments(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'FINALIZED')),
    started_at TIMESTAMPTZ,
    legacy_start BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK ((legacy_start AND started_at IS NULL) OR (NOT legacy_start AND started_at IS NOT NULL))
);

-- Preserve already-running operational attentions without inventing a clinical start timestamp.
INSERT INTO clinical_encounters (appointment_id, legacy_start)
SELECT id, TRUE FROM appointments
WHERE appointment_status = 'CONFIRMED' AND flow_stage = 'IN_ATTENTION';

REVOKE ALL ON clinical_encounters FROM PUBLIC;
