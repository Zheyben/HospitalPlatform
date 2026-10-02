-- Keep existing patient rows valid; registration requires insurance at the API boundary.
ALTER TABLE patients
    ADD COLUMN insurance VARCHAR(150);

-- A fresh installation must be able to assign the PATIENT role at registration.
INSERT INTO roles (name, description)
VALUES ('PATIENT', 'Patient account')
ON CONFLICT (name) DO NOTHING;
