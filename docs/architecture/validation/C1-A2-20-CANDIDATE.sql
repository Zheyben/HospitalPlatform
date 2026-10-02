-- EXPERIMENT ONLY. Run on an isolated PostgreSQL 16 database after V1-V4.
-- This is not a Flyway migration and must never be applied to the principal DB.
BEGIN;
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM availability_slots sl
    CROSS JOIN LATERAL (
      SELECT count(*) AS occupied FROM appointments a WHERE a.slot_id=sl.id
        AND a.appointment_status IN ('SCHEDULED','CONFIRMED','COMPLETED')
    ) occ
    WHERE (sl.status='RESERVED' AND occ.occupied <> 1)
       OR (sl.status<>'RESERVED' AND occ.occupied <> 0)
  ) THEN
    RAISE EXCEPTION 'Preflight: existing slot/appointment incoherence' USING ERRCODE='23514';
  END IF;
  IF EXISTS (
    SELECT 1 FROM appointments a JOIN availability_slots sl ON sl.id=a.slot_id
      JOIN schedules s ON s.id=sl.schedule_id WHERE a.professional_id<>s.professional_id
  ) THEN
    RAISE EXCEPTION 'Preflight: appointment/professional mismatch' USING ERRCODE='23514';
  END IF;
  IF EXISTS (
    SELECT 1 FROM availability_slots sl JOIN schedules s ON s.id=sl.schedule_id
    WHERE EXTRACT(DOW FROM sl.slot_date)::int<>s.day_of_week
       OR sl.start_time<s.start_time OR sl.end_time>s.end_time
  ) THEN
    RAISE EXCEPTION 'Preflight: slot outside schedule' USING ERRCODE='23514';
  END IF;
END $$;
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE hospital_business_config (
  singleton boolean PRIMARY KEY DEFAULT true CHECK (singleton),
  zone_name text NOT NULL
);

CREATE FUNCTION guard_business_zone() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.zone_name <> 'America/Lima' THEN
    RAISE EXCEPTION 'Business zone must remain America/Lima' USING ERRCODE = '22023';
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER trg_business_zone_guard BEFORE INSERT OR UPDATE ON hospital_business_config
  FOR EACH ROW EXECUTE FUNCTION guard_business_zone();
INSERT INTO hospital_business_config(singleton, zone_name) VALUES (true, 'America/Lima');

CREATE FUNCTION hospital_business_now() RETURNS timestamp
LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path = pg_catalog AS $$
DECLARE v_zone text;
BEGIN
  SELECT zone_name INTO STRICT v_zone FROM public.hospital_business_config WHERE singleton;
  RETURN clock_timestamp() AT TIME ZONE v_zone;
END $$;
REVOKE ALL ON hospital_business_config FROM PUBLIC;

ALTER TABLE schedules ADD COLUMN capacity_protected boolean NOT NULL DEFAULT false;
UPDATE schedules s SET capacity_protected = true
WHERE NOT s.active AND (
  EXISTS (SELECT 1 FROM availability_slots sl WHERE sl.schedule_id = s.id
          AND sl.status = 'RESERVED'
          AND sl.slot_date + sl.end_time > hospital_business_now())
  OR EXISTS (SELECT 1 FROM appointments a JOIN availability_slots sl ON sl.id = a.slot_id
             WHERE sl.schedule_id = s.id AND a.appointment_status IN ('SCHEDULED','CONFIRMED','COMPLETED')
               AND sl.slot_date + sl.end_time > hospital_business_now())
);

ALTER TABLE schedules ADD CONSTRAINT fk_schedule_professional_specialty
  FOREIGN KEY (professional_id, specialty_id)
  REFERENCES professional_specialties(professional_id, specialty_id);

CREATE FUNCTION guard_schedule_capacity() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF TG_OP = 'UPDATE' THEN
    IF ROW(NEW.professional_id, NEW.specialty_id, NEW.day_of_week, NEW.start_time, NEW.end_time)
       IS DISTINCT FROM
       ROW(OLD.professional_id, OLD.specialty_id, OLD.day_of_week, OLD.start_time, OLD.end_time)
       AND EXISTS (SELECT 1 FROM availability_slots WHERE schedule_id = OLD.id) THEN
      RAISE EXCEPTION 'Schedule with slots cannot be structurally edited' USING ERRCODE = '23514';
    END IF;
    IF OLD.active AND NOT NEW.active THEN
      NEW.capacity_protected := true;
    ELSIF NOT NEW.active AND OLD.capacity_protected AND NOT NEW.capacity_protected THEN
      IF EXISTS (SELECT 1 FROM availability_slots sl WHERE sl.schedule_id = OLD.id
                 AND sl.status = 'RESERVED'
                 AND sl.slot_date + sl.end_time > hospital_business_now())
         OR EXISTS (SELECT 1 FROM appointments a JOIN availability_slots sl ON sl.id = a.slot_id
                    WHERE sl.schedule_id = OLD.id
                      AND a.appointment_status IN ('SCHEDULED','CONFIRMED','COMPLETED')
                      AND sl.slot_date + sl.end_time > hospital_business_now()) THEN
        RAISE EXCEPTION 'Future occupied capacity still exists' USING ERRCODE = '23514';
      END IF;
    END IF;
  END IF;
  IF NEW.active THEN
    PERFORM 1 FROM professionals p JOIN specialties sp ON sp.id = NEW.specialty_id
      JOIN professional_specialties ps ON ps.professional_id = p.id AND ps.specialty_id = sp.id
      WHERE p.id = NEW.professional_id AND p.deleted_at IS NULL
        AND sp.active AND sp.deleted_at IS NULL
        AND (p.user_id IS NULL OR EXISTS (
          SELECT 1 FROM users u WHERE u.id=p.user_id AND u.enabled AND u.deleted_at IS NULL))
      FOR SHARE OF p, sp;
    IF NOT FOUND THEN
      RAISE EXCEPTION 'Inactive or unassociated professional/specialty' USING ERRCODE = '23514';
    END IF;
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER trg_schedule_capacity_guard BEFORE INSERT OR UPDATE ON schedules
  FOR EACH ROW EXECUTE FUNCTION guard_schedule_capacity();

ALTER TABLE schedules ADD CONSTRAINT ex_schedule_professional_capacity
  EXCLUDE USING gist (
    professional_id WITH =,
    day_of_week WITH =,
    tsrange(date '2000-01-01' + start_time,
            date '2000-01-01' + end_time, '[)') WITH &&
  ) WHERE (active OR capacity_protected);

ALTER TABLE availability_slots ADD CONSTRAINT ck_slot_30_minutes
  CHECK (end_time - start_time = interval '30 minutes');
ALTER TABLE availability_slots ADD CONSTRAINT ex_slot_schedule_date_interval
  EXCLUDE USING gist (
    schedule_id WITH =,
    slot_date WITH =,
    tsrange(slot_date + start_time, slot_date + end_time, '[)') WITH &&
  );
CREATE INDEX ix_slots_reserved_by_schedule_date
  ON availability_slots(schedule_id, slot_date, end_time) WHERE status = 'RESERVED';

DROP INDEX uq_appointments_active_slot;
CREATE UNIQUE INDEX uq_appointments_occupied_slot ON appointments(slot_id)
  WHERE appointment_status IN ('SCHEDULED','CONFIRMED','COMPLETED');

CREATE FUNCTION guard_slot_capacity() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
  v_schedule schedules%ROWTYPE;
BEGIN
  IF TG_OP = 'DELETE' THEN
    IF EXISTS (SELECT 1 FROM appointments WHERE slot_id = OLD.id) THEN
      RAISE EXCEPTION 'Cannot delete slot with appointment history' USING ERRCODE = '23514';
    END IF;
    RETURN OLD;
  END IF;
  SELECT * INTO v_schedule FROM schedules WHERE id = NEW.schedule_id FOR UPDATE;
  IF NOT FOUND THEN RAISE EXCEPTION 'Schedule not found' USING ERRCODE = '23503'; END IF;
  IF TG_OP = 'UPDATE' AND
     ROW(NEW.schedule_id,NEW.slot_date,NEW.start_time,NEW.end_time) IS DISTINCT FROM
     ROW(OLD.schedule_id,OLD.slot_date,OLD.start_time,OLD.end_time) AND
     EXISTS (SELECT 1 FROM appointments WHERE slot_id = OLD.id) THEN
    RAISE EXCEPTION 'Cannot move slot with appointment history' USING ERRCODE = '23514';
  END IF;
  IF EXTRACT(DOW FROM NEW.slot_date)::int <> v_schedule.day_of_week
     OR NEW.start_time < v_schedule.start_time OR NEW.end_time > v_schedule.end_time
     OR NEW.end_time - NEW.start_time <> interval '30 minutes' THEN
    RAISE EXCEPTION 'Slot outside weekly schedule' USING ERRCODE = '23514';
  END IF;
  IF TG_OP = 'INSERT' OR
     (TG_OP = 'UPDATE' AND ROW(NEW.schedule_id,NEW.slot_date,NEW.start_time,NEW.end_time)
                         IS DISTINCT FROM ROW(OLD.schedule_id,OLD.slot_date,OLD.start_time,OLD.end_time)) THEN
    IF NOT v_schedule.active THEN
      RAISE EXCEPTION 'Cannot create slot on inactive schedule' USING ERRCODE = '23514';
    END IF;
    IF NEW.slot_date + NEW.start_time <= hospital_business_now() THEN
      RAISE EXCEPTION 'Cannot create or move slot into the past' USING ERRCODE = '23514';
    END IF;
  END IF;
  IF TG_OP = 'INSERT' OR
     (TG_OP = 'UPDATE' AND ROW(NEW.schedule_id,NEW.slot_date,NEW.start_time,NEW.end_time)
        IS DISTINCT FROM ROW(OLD.schedule_id,OLD.slot_date,OLD.start_time,OLD.end_time)) OR
     (NEW.status = 'RESERVED' AND OLD.status <> 'RESERVED') THEN
    IF NOT v_schedule.active OR NEW.slot_date + NEW.start_time <= hospital_business_now() THEN
      RAISE EXCEPTION 'Slot is not reservable' USING ERRCODE = '23514';
    END IF;
    PERFORM 1 FROM professionals p WHERE p.id=v_schedule.professional_id AND p.deleted_at IS NULL FOR SHARE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Professional inactive' USING ERRCODE = '23514'; END IF;
    PERFORM 1 FROM specialties sp WHERE sp.id=v_schedule.specialty_id AND sp.active AND sp.deleted_at IS NULL FOR SHARE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Specialty inactive' USING ERRCODE = '23514'; END IF;
    PERFORM 1 FROM professional_specialties ps
      WHERE ps.professional_id=v_schedule.professional_id AND ps.specialty_id=v_schedule.specialty_id FOR SHARE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Professional-specialty association missing' USING ERRCODE = '23514'; END IF;
    PERFORM 1 FROM professionals p JOIN users u ON u.id=p.user_id
      WHERE p.id=v_schedule.professional_id AND u.enabled AND u.deleted_at IS NULL FOR SHARE OF u;
    IF NOT FOUND AND EXISTS (SELECT 1 FROM professionals p WHERE p.id=v_schedule.professional_id AND p.user_id IS NOT NULL) THEN
      RAISE EXCEPTION 'Professional account disabled' USING ERRCODE = '23514';
    END IF;
  END IF;
  IF TG_OP = 'UPDATE' AND OLD.status = 'RESERVED' AND NEW.status <> 'RESERVED'
     AND EXISTS (SELECT 1 FROM appointments a WHERE a.slot_id=OLD.id AND a.appointment_status='COMPLETED') THEN
    RAISE EXCEPTION 'Completed appointment consumes the slot' USING ERRCODE = '23514';
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER trg_slot_capacity_guard BEFORE INSERT OR UPDATE OR DELETE ON availability_slots
  FOR EACH ROW EXECUTE FUNCTION guard_slot_capacity();

CREATE FUNCTION guard_professional_deactivation() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF OLD.deleted_at IS NULL AND NEW.deleted_at IS NOT NULL AND EXISTS (
    SELECT 1 FROM appointments a WHERE a.professional_id=OLD.id
      AND a.appointment_status='CONFIRMED'
      AND a.flow_stage IN ('CHECK_IN','WAITING','IN_ATTENTION')
  ) THEN
    RAISE EXCEPTION 'Professional has attention in progress' USING ERRCODE='23514';
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER trg_professional_deactivation_guard BEFORE UPDATE OF deleted_at ON professionals
  FOR EACH ROW EXECUTE FUNCTION guard_professional_deactivation();

CREATE FUNCTION guard_professional_user_disable() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF ((OLD.enabled AND NOT NEW.enabled) OR
      (OLD.deleted_at IS NULL AND NEW.deleted_at IS NOT NULL)) AND EXISTS (
    SELECT 1 FROM professionals p JOIN appointments a ON a.professional_id=p.id
    WHERE p.user_id=OLD.id AND a.appointment_status='CONFIRMED'
      AND a.flow_stage IN ('CHECK_IN','WAITING','IN_ATTENTION')
  ) THEN
    RAISE EXCEPTION 'Professional account has attention in progress' USING ERRCODE='23514';
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER trg_professional_user_disable_guard BEFORE UPDATE OF enabled,deleted_at ON users
  FOR EACH ROW EXECUTE FUNCTION guard_professional_user_disable();

CREATE FUNCTION guard_appointment_capacity() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
  v_slot availability_slots%ROWTYPE;
  v_schedule schedules%ROWTYPE;
BEGIN
  IF TG_OP = 'DELETE' THEN
    RAISE EXCEPTION 'Appointment history cannot be deleted' USING ERRCODE = '23514';
  END IF;
  IF TG_OP = 'INSERT' AND (NEW.appointment_status <> 'SCHEDULED' OR NEW.flow_stage IS NOT NULL) THEN
    RAISE EXCEPTION 'New appointment must start SCHEDULED without a flow stage' USING ERRCODE = '23514';
  END IF;
  IF TG_OP = 'INSERT' OR NEW.slot_id IS DISTINCT FROM OLD.slot_id OR
     NEW.professional_id IS DISTINCT FROM OLD.professional_id THEN
    SELECT * INTO v_slot FROM availability_slots WHERE id=NEW.slot_id FOR SHARE;
    IF NOT FOUND OR v_slot.status <> 'RESERVED' THEN
      RAISE EXCEPTION 'Appointment requires reserved slot' USING ERRCODE = '23514';
    END IF;
    SELECT * INTO v_schedule FROM schedules WHERE id=v_slot.schedule_id;
    IF NEW.professional_id <> v_schedule.professional_id OR NOT v_schedule.active OR
       v_slot.slot_date + v_slot.start_time <= hospital_business_now() THEN
      RAISE EXCEPTION 'Appointment capacity mismatch' USING ERRCODE = '23514';
    END IF;
  END IF;
  IF TG_OP = 'UPDATE' AND NEW.appointment_status IS DISTINCT FROM OLD.appointment_status
     AND NEW.appointment_status IN ('CONFIRMED','CANCELLED','RESCHEDULED') THEN
    SELECT * INTO v_slot FROM availability_slots WHERE id=OLD.slot_id;
    IF v_slot.slot_date + v_slot.start_time <= hospital_business_now() OR
       OLD.flow_stage IN ('CHECK_IN','WAITING','IN_ATTENTION') THEN
      RAISE EXCEPTION 'Past or in-progress appointment transition' USING ERRCODE = '23514';
    END IF;
  END IF;
  IF TG_OP = 'UPDATE' AND OLD.appointment_status = 'COMPLETED' AND
     NEW.appointment_status IS DISTINCT FROM OLD.appointment_status THEN
    RAISE EXCEPTION 'Completed appointment is immutable' USING ERRCODE = '23514';
  END IF;
  IF TG_OP = 'UPDATE' AND NEW.flow_stage IS DISTINCT FROM OLD.flow_stage
     AND NEW.flow_stage IN ('CHECK_IN','WAITING','IN_ATTENTION') THEN
    PERFORM 1 FROM professionals p WHERE p.id=NEW.professional_id AND p.deleted_at IS NULL FOR SHARE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Inactive professional cannot advance attention' USING ERRCODE='23514'; END IF;
    PERFORM 1 FROM professionals p JOIN users u ON u.id=p.user_id
      WHERE p.id=NEW.professional_id AND u.enabled AND u.deleted_at IS NULL FOR SHARE OF u;
    IF NOT FOUND AND EXISTS (SELECT 1 FROM professionals p WHERE p.id=NEW.professional_id AND p.user_id IS NOT NULL) THEN
      RAISE EXCEPTION 'Disabled professional account cannot advance attention' USING ERRCODE='23514';
    END IF;
  END IF;
  IF TG_OP = 'UPDATE' AND NEW.appointment_status = 'COMPLETED' AND OLD.appointment_status <> 'COMPLETED' THEN
    SELECT * INTO v_slot FROM availability_slots WHERE id=OLD.slot_id;
    IF OLD.flow_stage IS DISTINCT FROM 'IN_ATTENTION' OR
       v_slot.slot_date + v_slot.start_time > hospital_business_now() THEN
      RAISE EXCEPTION 'Cannot complete attention before its slot starts' USING ERRCODE = '23514';
    END IF;
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER trg_appointment_capacity_guard BEFORE INSERT OR UPDATE OR DELETE ON appointments
  FOR EACH ROW EXECUTE FUNCTION guard_appointment_capacity();

CREATE FUNCTION assert_slot_appointment_coherence(p_slot_id uuid) RETURNS void
  LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_status text; v_occupied integer;
BEGIN
  SELECT status INTO v_status FROM availability_slots WHERE id=p_slot_id;
  IF NOT FOUND THEN RETURN; END IF;
  SELECT count(*) INTO v_occupied FROM appointments WHERE slot_id=p_slot_id
    AND appointment_status IN ('SCHEDULED','CONFIRMED','COMPLETED');
  IF (v_status='RESERVED' AND v_occupied <> 1) OR
     (v_status<>'RESERVED' AND v_occupied <> 0) THEN
    RAISE EXCEPTION 'Slot/appointment incoherence for %', p_slot_id USING ERRCODE = '23514';
  END IF;
END $$;
CREATE FUNCTION check_slot_appointment_coherence() RETURNS trigger
  LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
BEGIN
  IF TG_TABLE_NAME='appointments' THEN
    IF TG_OP IN ('UPDATE','DELETE') THEN PERFORM assert_slot_appointment_coherence(OLD.slot_id); END IF;
    IF TG_OP IN ('INSERT','UPDATE') THEN PERFORM assert_slot_appointment_coherence(NEW.slot_id); END IF;
  ELSE
    IF TG_OP IN ('UPDATE','DELETE') THEN PERFORM assert_slot_appointment_coherence(OLD.id); END IF;
    IF TG_OP IN ('INSERT','UPDATE') THEN PERFORM assert_slot_appointment_coherence(NEW.id); END IF;
  END IF;
  RETURN NULL;
END $$;
CREATE CONSTRAINT TRIGGER ctr_slot_appointment_coherence
  AFTER INSERT OR UPDATE OR DELETE ON availability_slots
  DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION check_slot_appointment_coherence();
CREATE CONSTRAINT TRIGGER ctr_appointment_slot_coherence
  AFTER INSERT OR UPDATE OR DELETE ON appointments
  DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION check_slot_appointment_coherence();
COMMIT;
