CREATE SEQUENCE clinical_record_number_seq AS bigint START WITH 1;

CREATE TABLE clinical_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL UNIQUE REFERENCES patients(id) ON DELETE RESTRICT,
    record_number TEXT NOT NULL UNIQUE DEFAULT ('HC-' || nextval('clinical_record_number_seq')::text),
    opened_at TIMESTAMP NOT NULL
);

-- Existing patients keep their original registration timestamp; unknown demographics remain absent.
INSERT INTO clinical_records (patient_id, opened_at)
SELECT id, created_at FROM patients ORDER BY created_at, id;

CREATE FUNCTION create_clinical_record_header()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER
SET search_path = pg_catalog, pg_temp AS $$
BEGIN
    EXECUTE format(
        'INSERT INTO %I.clinical_records (patient_id, opened_at) VALUES ($1, $2)',
        TG_TABLE_SCHEMA
    ) USING NEW.id, NEW.created_at;
    RETURN NEW;
END $$;

CREATE TRIGGER trg_patient_clinical_record_header
AFTER INSERT ON patients
FOR EACH ROW EXECUTE FUNCTION create_clinical_record_header();

REVOKE ALL ON clinical_records FROM PUBLIC;
REVOKE ALL ON SEQUENCE clinical_record_number_seq FROM PUBLIC;
REVOKE EXECUTE ON FUNCTION create_clinical_record_header() FROM PUBLIC;
