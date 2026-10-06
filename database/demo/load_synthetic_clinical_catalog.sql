-- Academic demo only. These are fictional labels, codes and presentations.
-- Run with psql as the schema owner and -v approver_email=<existing ADMIN email>.
\set ON_ERROR_STOP on
BEGIN;
SELECT set_config('hospital.demo.approver_email', :'approver_email', true);

DO $$
BEGIN
    IF (SELECT count(*) FROM users u
        JOIN user_roles ur ON ur.user_id = u.id
        JOIN roles r ON r.id = ur.role_id
        WHERE u.email = current_setting('hospital.demo.approver_email')
          AND u.enabled AND u.deleted_at IS NULL AND r.name = 'ADMIN') <> 1 THEN
        RAISE EXCEPTION 'Demo catalog requires one existing active ADMIN approver';
    END IF;
END;
$$;

INSERT INTO medical_catalog_sources
    (id, catalog_type, source_name, source_version, license_reference,
     approved_by_user_id, approved_at)
SELECT source.id, source.catalog_type, 'HospitalPlatform synthetic demo catalog',
       'demo-v1', 'SYNTHETIC-DEMO-ONLY; no clinical validity', u.id, current_timestamp
FROM (VALUES
    ('d0000000-0000-4000-8000-000000000001'::uuid, 'ICD10'),
    ('d0000000-0000-4000-8000-000000000002'::uuid, 'MEDICATION'),
    ('d0000000-0000-4000-8000-000000000003'::uuid, 'PROCEDURE')
) AS source(id, catalog_type)
CROSS JOIN users u
WHERE u.email = current_setting('hospital.demo.approver_email')
ON CONFLICT (id) DO NOTHING;

INSERT INTO icd10_codes (id, code, description, source_id)
VALUES ('d1000000-0000-4000-8000-000000000001', 'DEMO-DX-001',
        '[DEMO] Diagnostico sintetico sin validez clinica',
        'd0000000-0000-4000-8000-000000000001')
ON CONFLICT (id) DO NOTHING;

INSERT INTO medications (id, generic_name, source_id) VALUES
    ('d2000000-0000-4000-8000-000000000001', '[DEMO] Medicamento sintetico A',
     'd0000000-0000-4000-8000-000000000002'),
    ('d2000000-0000-4000-8000-000000000002', '[DEMO] Medicamento sintetico B',
     'd0000000-0000-4000-8000-000000000002')
ON CONFLICT (id) DO NOTHING;

INSERT INTO medication_presentations
    (id, medication_id, name, concentration, pharmaceutical_form) VALUES
    ('d3000000-0000-4000-8000-000000000001',
     'd2000000-0000-4000-8000-000000000001', '[DEMO] Presentacion A1',
     'NO CLINICA', 'SIMULADA'),
    ('d3000000-0000-4000-8000-000000000002',
     'd2000000-0000-4000-8000-000000000001', '[DEMO] Presentacion A2',
     'NO CLINICA', 'SIMULADA'),
    ('d3000000-0000-4000-8000-000000000003',
     'd2000000-0000-4000-8000-000000000002', '[DEMO] Presentacion B1',
     'NO CLINICA', 'SIMULADA')
ON CONFLICT (id) DO NOTHING;

INSERT INTO procedures (id, code, name, source_id) VALUES
    ('d4000000-0000-4000-8000-000000000001', 'DEMO-PROC-001',
     '[DEMO] Procedimiento sintetico A', 'd0000000-0000-4000-8000-000000000003'),
    ('d4000000-0000-4000-8000-000000000002', 'DEMO-PROC-002',
     '[DEMO] Procedimiento sintetico B', 'd0000000-0000-4000-8000-000000000003')
ON CONFLICT (id) DO NOTHING;

-- A repeated load must encounter the same pack, not silently accept altered rows.
DO $$
DECLARE approver uuid;
BEGIN
    SELECT id INTO approver FROM users
    WHERE email = current_setting('hospital.demo.approver_email');
    IF EXISTS (
        SELECT 1 FROM (VALUES
            ('d0000000-0000-4000-8000-000000000001'::uuid, 'ICD10'),
            ('d0000000-0000-4000-8000-000000000002'::uuid, 'MEDICATION'),
            ('d0000000-0000-4000-8000-000000000003'::uuid, 'PROCEDURE')
        ) AS expected(id, catalog_type)
        LEFT JOIN medical_catalog_sources s ON s.id = expected.id
        WHERE s.catalog_type IS DISTINCT FROM expected.catalog_type
           OR s.source_name IS DISTINCT FROM 'HospitalPlatform synthetic demo catalog'
           OR s.source_version IS DISTINCT FROM 'demo-v1'
           OR s.license_reference IS DISTINCT FROM 'SYNTHETIC-DEMO-ONLY; no clinical validity'
           OR s.approved_by_user_id IS DISTINCT FROM approver
    ) THEN
        RAISE EXCEPTION 'Demo source metadata conflicts with existing data';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM icd10_codes
        WHERE id = 'd1000000-0000-4000-8000-000000000001'
          AND code = 'DEMO-DX-001' AND active
          AND description = '[DEMO] Diagnostico sintetico sin validez clinica'
          AND source_id = 'd0000000-0000-4000-8000-000000000001')
       OR EXISTS (
           SELECT 1 FROM (VALUES
               ('d2000000-0000-4000-8000-000000000001'::uuid,
                '[DEMO] Medicamento sintetico A'),
               ('d2000000-0000-4000-8000-000000000002'::uuid,
                '[DEMO] Medicamento sintetico B')
           ) AS expected(id, generic_name)
           LEFT JOIN medications m ON m.id = expected.id
           WHERE m.generic_name IS DISTINCT FROM expected.generic_name
              OR m.source_id IS DISTINCT FROM 'd0000000-0000-4000-8000-000000000002'::uuid
              OR m.commercial_name IS NOT NULL OR m.active IS DISTINCT FROM true
       )
       OR EXISTS (
           SELECT 1 FROM (VALUES
               ('d3000000-0000-4000-8000-000000000001'::uuid,
                'd2000000-0000-4000-8000-000000000001'::uuid, '[DEMO] Presentacion A1'),
               ('d3000000-0000-4000-8000-000000000002'::uuid,
                'd2000000-0000-4000-8000-000000000001'::uuid, '[DEMO] Presentacion A2'),
               ('d3000000-0000-4000-8000-000000000003'::uuid,
                'd2000000-0000-4000-8000-000000000002'::uuid, '[DEMO] Presentacion B1')
           ) AS expected(id, medication_id, name)
           LEFT JOIN medication_presentations p ON p.id = expected.id
           WHERE p.medication_id IS DISTINCT FROM expected.medication_id
              OR p.name IS DISTINCT FROM expected.name
              OR p.concentration IS DISTINCT FROM 'NO CLINICA'
              OR p.pharmaceutical_form IS DISTINCT FROM 'SIMULADA'
              OR p.active IS DISTINCT FROM true
       )
       OR EXISTS (
           SELECT 1 FROM (VALUES
               ('d4000000-0000-4000-8000-000000000001'::uuid,
                'DEMO-PROC-001', '[DEMO] Procedimiento sintetico A'),
               ('d4000000-0000-4000-8000-000000000002'::uuid,
                'DEMO-PROC-002', '[DEMO] Procedimiento sintetico B')
           ) AS expected(id, code, name)
           LEFT JOIN procedures p ON p.id = expected.id
           WHERE p.code IS DISTINCT FROM expected.code
              OR p.name IS DISTINCT FROM expected.name
              OR p.source_id IS DISTINCT FROM 'd0000000-0000-4000-8000-000000000003'::uuid
              OR p.active IS DISTINCT FROM true
       ) THEN
        RAISE EXCEPTION 'Demo catalog data conflicts with existing data';
    END IF;
END;
$$;
COMMIT;
