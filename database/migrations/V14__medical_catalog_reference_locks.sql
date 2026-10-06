-- Runtime callers can lock catalog references without UPDATE privileges on catalog rows.
CREATE FUNCTION medical_lock_active_icd10(p_id uuid) RETURNS boolean
LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,pg_temp AS $$
DECLARE active_result boolean;
BEGIN
    SELECT active INTO active_result FROM public.icd10_codes WHERE id = p_id FOR SHARE;
    RETURN COALESCE(active_result, false);
END $$;

CREATE FUNCTION medical_lock_active_procedure(p_id uuid) RETURNS boolean
LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,pg_temp AS $$
DECLARE active_result boolean;
BEGIN
    SELECT active INTO active_result FROM public.procedures WHERE id = p_id FOR SHARE;
    RETURN COALESCE(active_result, false);
END $$;

CREATE FUNCTION medical_lock_active_specialty(p_id uuid) RETURNS boolean
LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,pg_temp AS $$
DECLARE active_result boolean;
BEGIN
    SELECT active AND deleted_at IS NULL INTO active_result
    FROM public.specialties WHERE id = p_id FOR SHARE;
    RETURN COALESCE(active_result, false);
END $$;

CREATE FUNCTION medical_lock_active_presentation(p_medication_id uuid, p_presentation_id uuid)
RETURNS boolean LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,pg_temp AS $$
DECLARE active_result boolean;
BEGIN
    SELECT m.active AND p.active INTO active_result
    FROM public.medications m
    JOIN public.medication_presentations p ON p.medication_id = m.id
    WHERE m.id = p_medication_id AND p.id = p_presentation_id
    FOR SHARE OF m, p;
    RETURN COALESCE(active_result, false);
END $$;

REVOKE ALL ON FUNCTION medical_lock_active_icd10(uuid) FROM PUBLIC;
REVOKE ALL ON FUNCTION medical_lock_active_procedure(uuid) FROM PUBLIC;
REVOKE ALL ON FUNCTION medical_lock_active_specialty(uuid) FROM PUBLIC;
REVOKE ALL ON FUNCTION medical_lock_active_presentation(uuid,uuid) FROM PUBLIC;
