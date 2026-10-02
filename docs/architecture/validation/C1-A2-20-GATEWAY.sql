-- Isolated PostgreSQL 16 experiment, applied after V1-V4 + C1-A2-20-CANDIDATE.sql.
-- The runtime receives SELECT and EXECUTE, but no direct DML on sensitive tables.
BEGIN;

CREATE OR REPLACE FUNCTION capacity_lock_context(
  p_professionals uuid[] DEFAULT ARRAY[]::uuid[],
  p_schedules uuid[] DEFAULT ARRAY[]::uuid[],
  p_slots uuid[] DEFAULT ARRAY[]::uuid[],
  p_appointments uuid[] DEFAULT ARRAY[]::uuid[]
) RETURNS void LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_id uuid;
BEGIN
  -- First gate serializes all capacity mutations before any row lock is taken.
  PERFORM pg_advisory_xact_lock(12020, 2);
  FOR v_id IN
    SELECT DISTINCT p.user_id FROM professionals p
    WHERE p.id=ANY(p_professionals) AND p.user_id IS NOT NULL ORDER BY p.user_id
  LOOP
    PERFORM 1 FROM users WHERE id=v_id FOR UPDATE;
  END LOOP;
  FOR v_id IN SELECT DISTINCT id FROM unnest(p_professionals) AS t(id) ORDER BY id LOOP
    PERFORM 1 FROM professionals WHERE id=v_id FOR UPDATE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Professional missing' USING ERRCODE='23503'; END IF;
  END LOOP;
  FOR v_id IN SELECT DISTINCT id FROM unnest(p_schedules) AS t(id) ORDER BY id LOOP
    PERFORM 1 FROM schedules WHERE id=v_id FOR UPDATE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Schedule missing' USING ERRCODE='23503'; END IF;
  END LOOP;
  FOR v_id IN SELECT DISTINCT id FROM unnest(p_slots) AS t(id) ORDER BY id LOOP
    PERFORM 1 FROM availability_slots WHERE id=v_id FOR UPDATE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Slot missing' USING ERRCODE='23503'; END IF;
  END LOOP;
  FOR v_id IN SELECT DISTINCT id FROM unnest(p_appointments) AS t(id) ORDER BY id LOOP
    PERFORM 1 FROM appointments WHERE id=v_id FOR UPDATE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Appointment missing' USING ERRCODE='23503'; END IF;
  END LOOP;
END $$;

CREATE OR REPLACE FUNCTION capacity_reserve(p_slot uuid, p_patient uuid, p_reason text DEFAULT NULL)
RETURNS uuid LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_schedule uuid; v_professional uuid; v_appointment uuid := gen_random_uuid(); v_count int;
BEGIN
  PERFORM pg_advisory_xact_lock(12020, 2);
  SELECT s.id,s.professional_id INTO v_schedule,v_professional
    FROM availability_slots sl JOIN schedules s ON s.id=sl.schedule_id WHERE sl.id=p_slot;
  IF NOT FOUND THEN RAISE EXCEPTION 'Slot missing' USING ERRCODE='23503'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_professional],ARRAY[v_schedule],ARRAY[p_slot]);
  UPDATE availability_slots SET status='RESERVED' WHERE id=p_slot AND status='AVAILABLE';
  GET DIAGNOSTICS v_count = ROW_COUNT;
  IF v_count<>1 THEN RAISE EXCEPTION 'SLOT_UNAVAILABLE' USING ERRCODE='23514'; END IF;
  INSERT INTO appointments(id,patient_id,professional_id,slot_id,appointment_status,reason)
    VALUES(v_appointment,p_patient,v_professional,p_slot,'SCHEDULED',p_reason);
  RETURN v_appointment;
END $$;

CREATE OR REPLACE FUNCTION capacity_cancel(p_appointment uuid)
RETURNS void LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_slot uuid; v_schedule uuid; v_professional uuid; v_status text;
BEGIN
  PERFORM pg_advisory_xact_lock(12020, 2);
  SELECT a.slot_id,s.id,a.professional_id INTO v_slot,v_schedule,v_professional
    FROM appointments a JOIN availability_slots sl ON sl.id=a.slot_id
    JOIN schedules s ON s.id=sl.schedule_id WHERE a.id=p_appointment;
  IF NOT FOUND THEN RAISE EXCEPTION 'Appointment missing' USING ERRCODE='23503'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_professional],ARRAY[v_schedule],ARRAY[v_slot],ARRAY[p_appointment]);
  SELECT appointment_status INTO v_status FROM appointments WHERE id=p_appointment;
  IF v_status NOT IN ('SCHEDULED','CONFIRMED') THEN
    RAISE EXCEPTION 'Appointment cannot be cancelled' USING ERRCODE='23514';
  END IF;
  UPDATE appointments SET appointment_status='CANCELLED' WHERE id=p_appointment;
  UPDATE availability_slots SET status='AVAILABLE' WHERE id=v_slot;
END $$;

CREATE OR REPLACE FUNCTION capacity_reschedule(p_appointment uuid, p_new_slot uuid)
RETURNS uuid LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_old_slot uuid; v_old_schedule uuid; v_old_professional uuid;
        v_new_schedule uuid; v_new_professional uuid; v_patient uuid; v_reason text;
        v_new_appointment uuid := gen_random_uuid(); v_count int;
BEGIN
  PERFORM pg_advisory_xact_lock(12020, 2);
  SELECT a.slot_id,s.id,a.professional_id,a.patient_id,a.reason
    INTO v_old_slot,v_old_schedule,v_old_professional,v_patient,v_reason
    FROM appointments a JOIN availability_slots sl ON sl.id=a.slot_id
    JOIN schedules s ON s.id=sl.schedule_id WHERE a.id=p_appointment;
  IF NOT FOUND THEN RAISE EXCEPTION 'Appointment missing' USING ERRCODE='23503'; END IF;
  SELECT s.id,s.professional_id INTO v_new_schedule,v_new_professional
    FROM availability_slots sl JOIN schedules s ON s.id=sl.schedule_id WHERE sl.id=p_new_slot;
  IF NOT FOUND THEN RAISE EXCEPTION 'New slot missing' USING ERRCODE='23503'; END IF;
  IF v_old_slot=p_new_slot THEN RAISE EXCEPTION 'Same slot' USING ERRCODE='23514'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_old_professional,v_new_professional],
    ARRAY[v_old_schedule,v_new_schedule],ARRAY[v_old_slot,p_new_slot],ARRAY[p_appointment]);
  IF NOT EXISTS (SELECT 1 FROM appointments WHERE id=p_appointment
                 AND slot_id=v_old_slot AND appointment_status IN ('SCHEDULED','CONFIRMED')) THEN
    RAISE EXCEPTION 'Appointment changed' USING ERRCODE='23514';
  END IF;
  UPDATE availability_slots SET status='RESERVED' WHERE id=p_new_slot AND status='AVAILABLE';
  GET DIAGNOSTICS v_count = ROW_COUNT;
  IF v_count<>1 THEN RAISE EXCEPTION 'SLOT_UNAVAILABLE' USING ERRCODE='23514'; END IF;
  INSERT INTO appointments(id,patient_id,professional_id,slot_id,appointment_status,reason,rescheduled_from_id)
    VALUES(v_new_appointment,v_patient,v_new_professional,p_new_slot,'SCHEDULED',v_reason,p_appointment);
  UPDATE appointments SET appointment_status='RESCHEDULED' WHERE id=p_appointment;
  UPDATE availability_slots SET status='AVAILABLE' WHERE id=v_old_slot;
  RETURN v_new_appointment;
END $$;

CREATE OR REPLACE FUNCTION capacity_schedule_status(p_schedule uuid, p_active boolean)
RETURNS void LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_professional uuid;
BEGIN
  PERFORM pg_advisory_xact_lock(12020, 2);
  SELECT professional_id INTO v_professional FROM schedules WHERE id=p_schedule;
  IF NOT FOUND THEN RAISE EXCEPTION 'Schedule missing' USING ERRCODE='23503'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_professional],ARRAY[p_schedule]);
  UPDATE schedules SET active=p_active WHERE id=p_schedule;
END $$;

CREATE OR REPLACE FUNCTION capacity_schedule_edit(p_schedule uuid, p_start time, p_end time)
RETURNS void LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_professional uuid;
BEGIN
  PERFORM pg_advisory_xact_lock(12020, 2);
  SELECT professional_id INTO v_professional FROM schedules WHERE id=p_schedule;
  IF NOT FOUND THEN RAISE EXCEPTION 'Schedule missing' USING ERRCODE='23503'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_professional],ARRAY[p_schedule]);
  UPDATE schedules SET start_time=p_start,end_time=p_end WHERE id=p_schedule;
END $$;

CREATE OR REPLACE FUNCTION capacity_generate_slot(p_schedule uuid,p_date date,p_start time,p_end time)
RETURNS uuid LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_professional uuid; v_slot uuid;
BEGIN
  PERFORM pg_advisory_xact_lock(12020, 2);
  SELECT professional_id INTO v_professional FROM schedules WHERE id=p_schedule;
  IF NOT FOUND THEN RAISE EXCEPTION 'Schedule missing' USING ERRCODE='23503'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_professional],ARRAY[p_schedule]);
  INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status)
    VALUES(gen_random_uuid(),p_schedule,p_date,p_start,p_end,'AVAILABLE')
    ON CONFLICT (schedule_id,slot_date,start_time) DO NOTHING RETURNING id INTO v_slot;
  IF v_slot IS NULL THEN
    SELECT id INTO v_slot FROM availability_slots
      WHERE schedule_id=p_schedule AND slot_date=p_date AND start_time=p_start;
  END IF;
  RETURN v_slot;
END $$;

CREATE OR REPLACE FUNCTION capacity_appointment_stage(p_appointment uuid,p_stage text)
RETURNS void LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_slot uuid; v_schedule uuid; v_professional uuid; v_current text; v_status text;
BEGIN
  PERFORM pg_advisory_xact_lock(12020, 2);
  SELECT a.slot_id,s.id,a.professional_id INTO v_slot,v_schedule,v_professional
    FROM appointments a JOIN availability_slots sl ON sl.id=a.slot_id
    JOIN schedules s ON s.id=sl.schedule_id WHERE a.id=p_appointment;
  IF NOT FOUND THEN RAISE EXCEPTION 'Appointment missing' USING ERRCODE='23503'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_professional],ARRAY[v_schedule],ARRAY[v_slot],ARRAY[p_appointment]);
  SELECT flow_stage,appointment_status INTO v_current,v_status FROM appointments WHERE id=p_appointment;
  IF v_status<>'CONFIRMED' OR NOT (
       (p_stage='CHECK_IN' AND v_current IS NULL) OR
       (p_stage='WAITING' AND v_current='CHECK_IN') OR
       (p_stage='IN_ATTENTION' AND v_current='WAITING')) THEN
    RAISE EXCEPTION 'Invalid stage transition' USING ERRCODE='23514';
  END IF;
  UPDATE appointments SET flow_stage=p_stage WHERE id=p_appointment;
END $$;

CREATE OR REPLACE FUNCTION capacity_appointment_confirm(p_appointment uuid)
RETURNS void LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_slot uuid; v_schedule uuid; v_professional uuid;
BEGIN
  PERFORM pg_advisory_xact_lock(12020, 2);
  SELECT a.slot_id,s.id,a.professional_id INTO v_slot,v_schedule,v_professional
    FROM appointments a JOIN availability_slots sl ON sl.id=a.slot_id
    JOIN schedules s ON s.id=sl.schedule_id WHERE a.id=p_appointment;
  IF NOT FOUND THEN RAISE EXCEPTION 'Appointment missing' USING ERRCODE='23503'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_professional],ARRAY[v_schedule],ARRAY[v_slot],ARRAY[p_appointment]);
  UPDATE appointments SET appointment_status='CONFIRMED' WHERE id=p_appointment AND appointment_status='SCHEDULED';
  IF NOT FOUND THEN RAISE EXCEPTION 'Cannot confirm appointment' USING ERRCODE='23514'; END IF;
END $$;

CREATE OR REPLACE FUNCTION capacity_appointment_complete(p_appointment uuid)
RETURNS void LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_slot uuid; v_schedule uuid; v_professional uuid;
BEGIN
  PERFORM pg_advisory_xact_lock(12020, 2);
  SELECT a.slot_id,s.id,a.professional_id INTO v_slot,v_schedule,v_professional
    FROM appointments a JOIN availability_slots sl ON sl.id=a.slot_id
    JOIN schedules s ON s.id=sl.schedule_id WHERE a.id=p_appointment;
  IF NOT FOUND THEN RAISE EXCEPTION 'Appointment missing' USING ERRCODE='23503'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_professional],ARRAY[v_schedule],ARRAY[v_slot],ARRAY[p_appointment]);
  UPDATE appointments SET appointment_status='COMPLETED',flow_stage='FINISHED'
    WHERE id=p_appointment AND appointment_status='CONFIRMED' AND flow_stage='IN_ATTENTION';
  IF NOT FOUND THEN RAISE EXCEPTION 'Cannot complete appointment' USING ERRCODE='23514'; END IF;
END $$;

CREATE OR REPLACE FUNCTION capacity_professional_deactivate(p_professional uuid)
RETURNS void LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
BEGIN
  PERFORM capacity_lock_context(ARRAY[p_professional]);
  UPDATE professionals SET deleted_at=clock_timestamp() WHERE id=p_professional AND deleted_at IS NULL;
END $$;

CREATE OR REPLACE FUNCTION capacity_user_disable(p_user uuid)
RETURNS void LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_professional uuid;
BEGIN
  PERFORM pg_advisory_xact_lock(12020, 2);
  SELECT id INTO v_professional FROM professionals WHERE user_id=p_user;
  IF v_professional IS NOT NULL THEN
    PERFORM capacity_lock_context(ARRAY[v_professional]);
  ELSE
    PERFORM 1 FROM users WHERE id=p_user FOR UPDATE;
  END IF;
  UPDATE users SET enabled=false WHERE id=p_user;
END $$;

COMMIT;
