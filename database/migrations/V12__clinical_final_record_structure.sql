CREATE TABLE clinical_final_records (
    encounter_id UUID PRIMARY KEY REFERENCES clinical_encounters(id) ON DELETE RESTRICT,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE RESTRICT,
    professional_id UUID NOT NULL REFERENCES professionals(id) ON DELETE RESTRICT,
    clinical_record_number TEXT NOT NULL CHECK (btrim(clinical_record_number) <> ''),
    patient_first_name TEXT,
    patient_last_name TEXT,
    patient_document_type VARCHAR(20) NOT NULL,
    patient_document_number TEXT NOT NULL CHECK (btrim(patient_document_number) <> ''),
    patient_birth_date DATE,
    patient_insurance TEXT,
    patient_address TEXT,
    patient_phone TEXT,
    professional_first_name TEXT,
    professional_last_name TEXT,
    professional_license_number TEXT NOT NULL CHECK (btrim(professional_license_number) <> ''),
    professional_simulated_rne VARCHAR(20) NOT NULL
        CHECK (professional_simulated_rne ~ '^SIM-RNE-[0-9a-f]{12}$'),
    specialty_name TEXT NOT NULL CHECK (btrim(specialty_name) <> ''),
    appointment_date DATE NOT NULL,
    appointment_start_time TIME NOT NULL,
    finalized_at TIMESTAMPTZ NOT NULL,
    draft_version INTEGER NOT NULL CHECK (draft_version >= 0),
    content_sha256 CHAR(64) NOT NULL CHECK (content_sha256 ~ '^[0-9a-f]{64}$')
);

CREATE INDEX ix_clinical_final_records_patient ON clinical_final_records (patient_id, finalized_at DESC);

CREATE TABLE clinical_history_entries (
    encounter_id UUID PRIMARY KEY REFERENCES clinical_final_records(encounter_id) ON DELETE RESTRICT,
    pathological TEXT,
    surgical TEXT,
    allergies_and_reactions TEXT,
    usual_medication TEXT,
    transfusions TEXT,
    relevant_habits TEXT,
    hospitalizations TEXT,
    other_personal TEXT,
    father_history TEXT,
    mother_history TEXT,
    siblings_history TEXT,
    children_history TEXT,
    grandparents_history TEXT,
    other_family TEXT,
    family_source TEXT,
    family_source_date DATE,
    family_observation TEXT,
    pregnancies INTEGER CHECK (pregnancies >= 0),
    births INTEGER CHECK (births >= 0),
    miscarriages INTEGER CHECK (miscarriages >= 0),
    cesareans INTEGER CHECK (cesareans >= 0),
    last_menstrual_period DATE,
    other_gynecologic TEXT,
    other_alerts TEXT
);

CREATE TABLE clinical_assessments (
    encounter_id UUID PRIMARY KEY REFERENCES clinical_final_records(encounter_id) ON DELETE RESTRICT,
    reason TEXT NOT NULL CHECK (btrim(reason) <> ''),
    symptoms_and_current_illness TEXT NOT NULL CHECK (btrim(symptoms_and_current_illness) <> ''),
    illness_duration NUMERIC(11,3) CHECK (illness_duration > 0),
    illness_duration_unit VARCHAR(10)
        CHECK (illness_duration_unit IN ('HOURS', 'DAYS', 'WEEKS', 'MONTHS')),
    biological_functions TEXT,
    reviewed_history_and_allergies TEXT,
    prior_treatment_and_response TEXT,
    systolic_blood_pressure INTEGER CHECK (systolic_blood_pressure > 0),
    diastolic_blood_pressure INTEGER CHECK (diastolic_blood_pressure > 0),
    heart_rate INTEGER CHECK (heart_rate > 0),
    respiratory_rate INTEGER CHECK (respiratory_rate > 0),
    temperature_celsius NUMERIC(6,2) CHECK (temperature_celsius > 0),
    oxygen_saturation_percent INTEGER CHECK (oxygen_saturation_percent BETWEEN 1 AND 100),
    weight_kg NUMERIC(8,3) CHECK (weight_kg > 0),
    height_cm NUMERIC(8,3) CHECK (height_cm > 0),
    general_condition TEXT,
    head_and_neck TEXT,
    cardiopulmonary_and_abdomen TEXT,
    extremities_and_neurologic TEXT,
    other_findings TEXT,
    CHECK ((illness_duration IS NULL) = (illness_duration_unit IS NULL))
);

CREATE TABLE clinical_diagnoses (
    encounter_id UUID PRIMARY KEY REFERENCES clinical_final_records(encounter_id) ON DELETE RESTRICT,
    primary_diagnosis TEXT NOT NULL CHECK (btrim(primary_diagnosis) <> ''),
    icd10_code_id UUID NOT NULL REFERENCES icd10_codes(id) ON DELETE RESTRICT,
    icd10_code_snapshot TEXT NOT NULL CHECK (btrim(icd10_code_snapshot) <> ''),
    icd10_description_snapshot TEXT NOT NULL CHECK (btrim(icd10_description_snapshot) <> ''),
    diagnosis_type VARCHAR(20) NOT NULL CHECK (diagnosis_type IN ('PRESUMPTIVE', 'DEFINITIVE')),
    observations TEXT
);

CREATE TABLE clinical_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    encounter_id UUID NOT NULL UNIQUE REFERENCES clinical_final_records(encounter_id) ON DELETE RESTRICT,
    procedure_id UUID NOT NULL REFERENCES procedures(id) ON DELETE RESTRICT,
    procedure_code_snapshot TEXT NOT NULL CHECK (btrim(procedure_code_snapshot) <> ''),
    procedure_name_snapshot TEXT NOT NULL CHECK (btrim(procedure_name_snapshot) <> ''),
    priority VARCHAR(20) CHECK (priority IN ('ROUTINE', 'PREFERRED', 'URGENT')),
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' CHECK (status = 'REQUESTED'),
    requested_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE clinical_treatment_plans (
    encounter_id UUID PRIMARY KEY REFERENCES clinical_final_records(encounter_id) ON DELETE RESTRICT,
    therapeutic_plan TEXT NOT NULL CHECK (btrim(therapeutic_plan) <> ''),
    general_indications TEXT NOT NULL CHECK (btrim(general_indications) <> ''),
    patient_education TEXT,
    warning_signs TEXT,
    referral_type VARCHAR(20) CHECK (referral_type IN ('NONE', 'INTERCONSULTATION')),
    referral_specialty_id UUID REFERENCES specialties(id) ON DELETE RESTRICT,
    referral_specialty_snapshot TEXT,
    suggested_follow_up_date DATE,
    follow_up_reason TEXT,
    pending_results_and_plan TEXT,
    complementary_observations TEXT,
    CHECK ((referral_type = 'INTERCONSULTATION' AND referral_specialty_id IS NOT NULL
            AND referral_specialty_snapshot IS NOT NULL AND btrim(referral_specialty_snapshot) <> '')
        OR (referral_type IS DISTINCT FROM 'INTERCONSULTATION' AND referral_specialty_id IS NULL
            AND referral_specialty_snapshot IS NULL))
);

CREATE SEQUENCE clinical_prescription_number_seq AS BIGINT START WITH 1;

CREATE TABLE clinical_prescriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    encounter_id UUID NOT NULL UNIQUE REFERENCES clinical_final_records(encounter_id) ON DELETE RESTRICT,
    prescription_number TEXT NOT NULL UNIQUE
        DEFAULT ('RX-' || nextval('clinical_prescription_number_seq')::text),
    issued_at TIMESTAMPTZ NOT NULL,
    additional_precautions TEXT,
    non_pharmacological_recommendations TEXT,
    warning_signs TEXT,
    additional_care TEXT,
    follow_up_observations TEXT
);

ALTER TABLE medication_presentations
    ADD CONSTRAINT uq_medication_presentations_id_medication UNIQUE (id, medication_id);

CREATE TABLE clinical_prescription_items (
    prescription_id UUID NOT NULL REFERENCES clinical_prescriptions(id) ON DELETE RESTRICT,
    item_number INTEGER NOT NULL CHECK (item_number > 0),
    medication_id UUID NOT NULL REFERENCES medications(id) ON DELETE RESTRICT,
    presentation_id UUID NOT NULL,
    medication_name_snapshot TEXT NOT NULL CHECK (btrim(medication_name_snapshot) <> ''),
    commercial_name_snapshot TEXT,
    presentation_name_snapshot TEXT NOT NULL CHECK (btrim(presentation_name_snapshot) <> ''),
    concentration_snapshot TEXT NOT NULL CHECK (btrim(concentration_snapshot) <> ''),
    pharmaceutical_form_snapshot TEXT NOT NULL CHECK (btrim(pharmaceutical_form_snapshot) <> ''),
    dose NUMERIC(11,3) NOT NULL CHECK (dose > 0),
    dose_unit VARCHAR(20) NOT NULL
        CHECK (dose_unit IN ('MG', 'G', 'ML', 'DROPS', 'TABLET', 'CAPSULE', 'IU')),
    frequency VARCHAR(20) NOT NULL
        CHECK (frequency IN ('EVERY_6_HOURS', 'EVERY_8_HOURS', 'EVERY_12_HOURS', 'EVERY_24_HOURS', 'ONCE')),
    route VARCHAR(20) NOT NULL
        CHECK (route IN ('ORAL', 'SUBLINGUAL', 'INTRAMUSCULAR', 'INTRAVENOUS', 'SUBCUTANEOUS',
                        'TOPICAL', 'INHALATION', 'OPHTHALMIC', 'OTIC', 'RECTAL', 'VAGINAL')),
    duration NUMERIC(11,3) NOT NULL CHECK (duration > 0),
    duration_unit VARCHAR(10) NOT NULL CHECK (duration_unit IN ('DAYS', 'WEEKS')),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    usage_instructions TEXT NOT NULL CHECK (btrim(usage_instructions) <> ''),
    PRIMARY KEY (prescription_id, item_number),
    FOREIGN KEY (presentation_id, medication_id)
        REFERENCES medication_presentations(id, medication_id) ON DELETE RESTRICT
);

REVOKE ALL ON clinical_final_records, clinical_history_entries, clinical_assessments,
    clinical_diagnoses, clinical_orders, clinical_treatment_plans, clinical_prescriptions,
    clinical_prescription_items FROM PUBLIC;
REVOKE ALL ON SEQUENCE clinical_prescription_number_seq FROM PUBLIC;
