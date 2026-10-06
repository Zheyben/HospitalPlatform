CREATE FUNCTION guard_clinical_completion() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.appointment_status = 'COMPLETED'
     AND OLD.appointment_status IS DISTINCT FROM 'COMPLETED' THEN
    IF NEW.flow_stage IS DISTINCT FROM 'FINISHED' OR NOT EXISTS (
      SELECT 1 FROM clinical_encounters ce
      JOIN clinical_final_records f ON f.encounter_id = ce.id
      JOIN clinical_history_entries h ON h.encounter_id = ce.id
      JOIN clinical_assessments a ON a.encounter_id = ce.id
      JOIN clinical_diagnoses d ON d.encounter_id = ce.id
      JOIN clinical_treatment_plans t ON t.encounter_id = ce.id
      WHERE ce.appointment_id = NEW.id AND ce.status = 'FINALIZED'
        AND (NOT EXISTS (SELECT 1 FROM clinical_prescriptions rx WHERE rx.encounter_id = ce.id)
             OR EXISTS (
               SELECT 1 FROM clinical_prescriptions rx
               JOIN clinical_prescription_items item ON item.prescription_id = rx.id
               WHERE rx.encounter_id = ce.id
             ))
    ) THEN
      RAISE EXCEPTION 'Complete clinical final record required before completion'
        USING ERRCODE = '23514';
    END IF;
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER trg_clinical_completion_guard
  BEFORE UPDATE OF appointment_status ON appointments
  FOR EACH ROW EXECUTE FUNCTION guard_clinical_completion();

REVOKE ALL ON FUNCTION guard_clinical_completion() FROM PUBLIC;
