ALTER TABLE professionals
    ADD COLUMN simulated_rne VARCHAR(20)
        GENERATED ALWAYS AS ('SIM-RNE-' || right(replace(id::text, '-', ''), 12)) STORED;

ALTER TABLE professionals
    ADD CONSTRAINT uq_professionals_simulated_rne UNIQUE (simulated_rne);

ALTER TABLE clinical_encounters
    ADD COLUMN simulated_care_type VARCHAR(80) NOT NULL DEFAULT 'Consulta externa (dato simulado)',
    ADD COLUMN simulated_service VARCHAR(80) NOT NULL DEFAULT 'Servicio ambulatorio (dato simulado)';
