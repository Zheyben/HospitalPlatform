CREATE TABLE medical_catalog_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    catalog_type VARCHAR(30) NOT NULL CHECK (catalog_type IN ('ICD10', 'MEDICATION', 'PROCEDURE')),
    source_name TEXT NOT NULL CHECK (btrim(source_name) <> ''),
    source_version TEXT NOT NULL CHECK (btrim(source_version) <> ''),
    license_reference TEXT NOT NULL CHECK (btrim(license_reference) <> ''),
    approved_by_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    approved_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (catalog_type, source_name, source_version),
    UNIQUE (id, catalog_type)
);

CREATE TABLE icd10_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) NOT NULL UNIQUE CHECK (btrim(code) <> ''),
    description TEXT NOT NULL CHECK (btrim(description) <> ''),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    source_id UUID NOT NULL,
    source_type VARCHAR(30) NOT NULL DEFAULT 'ICD10' CHECK (source_type = 'ICD10'),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_id, source_type) REFERENCES medical_catalog_sources(id, catalog_type) ON DELETE RESTRICT
);

CREATE TABLE medications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    generic_name TEXT NOT NULL CHECK (btrim(generic_name) <> ''),
    commercial_name TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    source_id UUID NOT NULL,
    source_type VARCHAR(30) NOT NULL DEFAULT 'MEDICATION' CHECK (source_type = 'MEDICATION'),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (commercial_name IS NULL OR btrim(commercial_name) <> ''),
    FOREIGN KEY (source_id, source_type) REFERENCES medical_catalog_sources(id, catalog_type) ON DELETE RESTRICT
);

CREATE TABLE medication_presentations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medication_id UUID NOT NULL REFERENCES medications(id) ON DELETE RESTRICT,
    name TEXT NOT NULL CHECK (btrim(name) <> ''),
    concentration TEXT NOT NULL CHECK (btrim(concentration) <> ''),
    pharmaceutical_form TEXT NOT NULL CHECK (btrim(pharmaceutical_form) <> ''),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (medication_id, name, concentration, pharmaceutical_form)
);

CREATE TABLE procedures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE CHECK (btrim(code) <> ''),
    name TEXT NOT NULL CHECK (btrim(name) <> ''),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    source_id UUID NOT NULL,
    source_type VARCHAR(30) NOT NULL DEFAULT 'PROCEDURE' CHECK (source_type = 'PROCEDURE'),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_id, source_type) REFERENCES medical_catalog_sources(id, catalog_type) ON DELETE RESTRICT
);

CREATE INDEX ix_presentations_medication ON medication_presentations (medication_id, name) WHERE active;

REVOKE ALL ON medical_catalog_sources, icd10_codes, medications, medication_presentations, procedures FROM PUBLIC;
