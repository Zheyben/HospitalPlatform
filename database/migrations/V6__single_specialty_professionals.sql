-- Preserve existing data. A conflicting legacy assignment must be reviewed before this migration.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM professional_specialties
    GROUP BY professional_id HAVING count(*) > 1
  ) THEN
    RAISE EXCEPTION 'Multiple specialties found for a professional; resolve before V6';
  END IF;
END $$;

CREATE UNIQUE INDEX uq_professional_one_specialty
  ON professional_specialties(professional_id);

-- A fresh installation must be able to create a linked professional account.
INSERT INTO roles (name, description)
VALUES ('PROFESSIONAL', 'Professional account')
ON CONFLICT (name) DO NOTHING;

INSERT INTO specialties(name, active)
VALUES ('Medicina General', true), ('Traumatología', true), ('Odontología', true),
       ('Psicología', true), ('Oftalmología', true)
ON CONFLICT (name) DO NOTHING;

CREATE OR REPLACE FUNCTION capacity_professional_reactivate(p_professional uuid)
RETURNS void LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
BEGIN
  PERFORM capacity_lock_context(ARRAY[p_professional]);
  IF EXISTS (SELECT 1 FROM professionals WHERE id=p_professional AND deleted_at IS NULL) THEN
    RETURN;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM professionals p
      JOIN users u ON u.id=p.user_id
      JOIN professional_specialties ps ON ps.professional_id=p.id
      JOIN specialties sp ON sp.id=ps.specialty_id
    WHERE p.id=p_professional AND p.deleted_at IS NOT NULL
      AND u.enabled AND u.deleted_at IS NULL
      AND sp.active AND sp.deleted_at IS NULL
  ) THEN
    RAISE EXCEPTION 'Professional account or specialty is not operational' USING ERRCODE='23514';
  END IF;
  UPDATE professionals SET deleted_at=NULL, updated_at=clock_timestamp()
    WHERE id=p_professional AND deleted_at IS NOT NULL;
END $$;
