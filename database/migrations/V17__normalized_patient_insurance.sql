CREATE TABLE insurance_providers (
    id UUID PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    display_name VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_insurance_provider_code CHECK (btrim(code) <> ''),
    CONSTRAINT ck_insurance_provider_name CHECK (btrim(display_name) <> '')
);

INSERT INTO insurance_providers (id, code, display_name) VALUES
    ('a0000000-0000-4000-8000-000000000001', 'SIS', 'SIS'),
    ('a0000000-0000-4000-8000-000000000002', 'ESSALUD', 'EsSalud'),
    ('a0000000-0000-4000-8000-000000000003', 'PARTICULAR', 'Particular');

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM patients p
        WHERE p.insurance IS NOT NULL
          AND upper(btrim(p.insurance)) NOT IN ('SIS', 'ESSALUD', 'PARTICULAR')
    ) THEN
        RAISE EXCEPTION 'Unknown patient insurance values require explicit migration before V17';
    END IF;
END;
$$;

ALTER TABLE patients
    ADD COLUMN insurance_id UUID REFERENCES insurance_providers(id) ON DELETE RESTRICT;

UPDATE patients p
SET insurance_id = ip.id,
    insurance = ip.display_name
FROM insurance_providers ip
WHERE upper(btrim(p.insurance)) = ip.code;

CREATE FUNCTION normalize_patient_insurance() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = pg_catalog, pg_temp AS $$
DECLARE
    selected_id UUID;
    selected_code TEXT;
    selected_name TEXT;
BEGIN
    IF NEW.insurance_id IS NULL AND (NEW.insurance IS NULL OR btrim(NEW.insurance) = '') THEN
        NEW.insurance := NULL;
        RETURN NEW;
    END IF;

    IF NEW.insurance_id IS NOT NULL THEN
        EXECUTE format('SELECT id, code, display_name FROM %I.insurance_providers WHERE id = $1 AND active',
                TG_TABLE_SCHEMA)
            INTO selected_id, selected_code, selected_name USING NEW.insurance_id;
    ELSE
        EXECUTE format('SELECT id, code, display_name FROM %I.insurance_providers WHERE code = $1 AND active',
                TG_TABLE_SCHEMA)
            INTO selected_id, selected_code, selected_name USING upper(btrim(NEW.insurance));
    END IF;

    IF selected_id IS NULL OR (NEW.insurance IS NOT NULL AND btrim(NEW.insurance) <> ''
            AND upper(btrim(NEW.insurance)) <> selected_code) THEN
        RAISE EXCEPTION 'Invalid patient insurance selection';
    END IF;

    NEW.insurance_id := selected_id;
    NEW.insurance := selected_name;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_normalize_patient_insurance
BEFORE INSERT OR UPDATE OF insurance_id, insurance ON patients
FOR EACH ROW EXECUTE FUNCTION normalize_patient_insurance();

REVOKE ALL ON insurance_providers FROM PUBLIC;
REVOKE ALL ON FUNCTION normalize_patient_insurance() FROM PUBLIC;
