ALTER TABLE patients
    ADD COLUMN marital_status VARCHAR(80),
    ADD COLUMN occupation VARCHAR(120),
    ADD COLUMN district VARCHAR(120),
    ADD COLUMN education_level VARCHAR(120),
    ADD COLUMN affiliation_number VARCHAR(60),
    ADD COLUMN emergency_contact_name VARCHAR(150),
    ADD COLUMN emergency_contact_relationship VARCHAR(80),
    ADD COLUMN emergency_contact_phone VARCHAR(50),
    ADD CONSTRAINT ck_patient_affiliation_insurance CHECK (
        affiliation_number IS NULL OR (
            insurance_id IS NOT NULL
            AND insurance_id <> 'a0000000-0000-4000-8000-000000000003'::uuid
        )
    ),
    ADD CONSTRAINT ck_patient_emergency_phone CHECK (
        emergency_contact_phone IS NULL OR emergency_contact_phone ~ '^\+?[0-9]{7,15}$'
    );

ALTER TABLE clinical_final_records
    ADD COLUMN patient_marital_status VARCHAR(80),
    ADD COLUMN patient_occupation VARCHAR(120),
    ADD COLUMN patient_district VARCHAR(120),
    ADD COLUMN patient_education_level VARCHAR(120),
    ADD COLUMN patient_affiliation_number VARCHAR(60),
    ADD COLUMN patient_emergency_contact_name VARCHAR(150),
    ADD COLUMN patient_emergency_contact_relationship VARCHAR(80),
    ADD COLUMN patient_emergency_contact_phone VARCHAR(50);
