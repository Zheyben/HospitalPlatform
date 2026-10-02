-- C1-B: approved academic domain and capacity rules. V1-V4 remain immutable.
-- Flyway runs this migration in one PostgreSQL transaction.

CREATE TEMP TABLE c1_patient_document_map (
  patient_id uuid PRIMARY KEY, old_number text NOT NULL, new_number text NOT NULL UNIQUE
) ON COMMIT DROP;
INSERT INTO c1_patient_document_map VALUES
('093e273b-43d9-4bb1-b275-c9bed2ccb358','1790812486462683','90000001'),
('1ad64dbe-35eb-4b93-a411-39fd309ee39e','1790812366391234','90000002'),
('2295852d-0f98-4cbd-8f77-f3539c59fc3e','B2-56a05b4757fa43e0','90000003'),
('33db4d03-1a03-4165-a91b-2ba09149d1a8','B2-26fd4ea41c8a40e2','90000004'),
('43d10d5d-6e03-4d80-8f9a-4018039d78f0','1790813053314','90000005'),
('63ac1e83-d3e2-402a-a47f-30fae309e177','B2F-b810c705cef24f7b','90000006'),
('6b8bde3d-5014-434f-8c74-f45de837ab23','1790812176982980','90000007'),
('80c2e11a-2910-458f-af27-77e2bd9f7e93','9020260930180602','90000008'),
('85bb8787-8ef8-42a5-81de-cbdd60ec789d','1790813533229783','90000009'),
('8dbac740-af9c-4c97-ab95-39d122b0fbb1','1790813322872346','90000010'),
('aaba452c-4a0b-4fb3-a600-8ac74a45881c','1790812870450706','90000011'),
('af3e4cb6-b6b8-4fd3-bbad-a739585708d8','1790812195194439','90000012'),
('dc0a1aeb-5077-4002-bd2e-baaadcbeb5b7','1790812286082548','90000013'),
('f7e7e408-03b7-4f14-8ee7-b85e3851ffff','1790812462256890','90000014');

DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM c1_patient_document_map m JOIN patients p ON p.id=m.patient_id
             WHERE p.document_type<>'DNI' OR p.document_number<>m.old_number) THEN
    RAISE EXCEPTION 'C1-B patient mapping source differs from precheck' USING ERRCODE='23514';
  END IF;
  IF EXISTS (SELECT 1 FROM c1_patient_document_map m JOIN patients p
             ON p.document_type='DNI' AND p.document_number=m.new_number AND p.id<>m.patient_id) THEN
    RAISE EXCEPTION 'C1-B synthetic DNI already occupied' USING ERRCODE='23505';
  END IF;
END $$;
UPDATE patients p SET document_number=m.new_number
  FROM c1_patient_document_map m WHERE p.id=m.patient_id AND p.document_number=m.old_number;

DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM professionals WHERE id='88743663-5b1b-3868-bf1a-aa0371adfac3'
             AND license_number<>'DEMO-CMP-0001') THEN
    RAISE EXCEPTION 'C1-B license mapping source differs from precheck' USING ERRCODE='23514';
  END IF;
  IF EXISTS (SELECT 1 FROM professionals WHERE id<>'88743663-5b1b-3868-bf1a-aa0371adfac3'
             AND license_number='900001') THEN
    RAISE EXCEPTION 'C1-B synthetic license already occupied' USING ERRCODE='23505';
  END IF;
END $$;
UPDATE professionals SET license_number='900001'
  WHERE id='88743663-5b1b-3868-bf1a-aa0371adfac3' AND license_number='DEMO-CMP-0001';

UPDATE users SET username=lower(btrim(username)), email=lower(btrim(email))
  WHERE username<>lower(btrim(username)) OR email<>lower(btrim(email));
UPDATE patients SET document_type=upper(btrim(document_type)),
                    document_number=upper(btrim(document_number))
  WHERE document_type<>upper(btrim(document_type)) OR document_number<>upper(btrim(document_number));
UPDATE professionals SET license_number=btrim(license_number)
  WHERE license_number<>btrim(license_number);

DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM patients WHERE document_type IS NULL OR document_number IS NULL
      OR NOT (document_type IN ('DNI','CE','PASSPORT') AND
          ((document_type='DNI' AND document_number ~ '^[0-9]{8}$') OR
           (document_type='CE' AND document_number ~ '^[A-Z0-9]{8,12}$') OR
           (document_type='PASSPORT' AND document_number ~ '^[A-Z0-9]{6,12}$')))) THEN
    RAISE EXCEPTION 'C1-B incompatible patient document remains' USING ERRCODE='23514';
  END IF;
  IF EXISTS (SELECT 1 FROM professionals WHERE license_number !~ '^[0-9]{4,6}$') THEN
    RAISE EXCEPTION 'C1-B incompatible professional license remains' USING ERRCODE='23514';
  END IF;
  IF EXISTS (SELECT 1 FROM (SELECT document_type,document_number FROM patients
             GROUP BY 1,2 HAVING count(*)>1) duplicates) THEN
    RAISE EXCEPTION 'C1-B duplicate documentary identity' USING ERRCODE='23505';
  END IF;
END $$;

ALTER TABLE patients DROP CONSTRAINT patients_document_number_key;
ALTER TABLE patients ALTER COLUMN document_type SET NOT NULL;
ALTER TABLE patients ALTER COLUMN document_number SET NOT NULL;
ALTER TABLE patients ADD CONSTRAINT uq_patients_document_identity UNIQUE(document_type,document_number);
ALTER TABLE patients ADD CONSTRAINT ck_patients_document_domain CHECK (
  (document_type='DNI' AND document_number ~ '^[0-9]{8}$') OR
  (document_type='CE' AND document_number ~ '^[A-Z0-9]{8,12}$') OR
  (document_type='PASSPORT' AND document_number ~ '^[A-Z0-9]{6,12}$'));
ALTER TABLE professionals ADD CONSTRAINT ck_professionals_license_digits
  CHECK (license_number ~ '^[0-9]{4,6}$');
ALTER TABLE users ADD CONSTRAINT ck_users_email_canonical CHECK (email=lower(btrim(email)));
ALTER TABLE users ADD CONSTRAINT ck_users_username_canonical CHECK (username=lower(btrim(username)));

-- Capacity DDL and gateway functions are appended from the validated C1A2-20.2 artifacts.

-- Capacity DDL adapted from the validated C1A2-20 candidate.
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
    RAISE EXCEPTION 'Slot outside weekly schedule: date %, time %..%, schedule day %, time %..%',
      NEW.slot_date, NEW.start_time, NEW.end_time, v_schedule.day_of_week,
      v_schedule.start_time, v_schedule.end_time USING ERRCODE = '23514';
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

-- Capacity gateway functions adapted from the validated C1A2-20 design.

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



CREATE FUNCTION guard_new_professional_specialty() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM professionals p JOIN specialties sp ON sp.id=NEW.specialty_id
                 WHERE p.id=NEW.professional_id AND p.deleted_at IS NULL
                   AND sp.active AND sp.deleted_at IS NULL) THEN
    RAISE EXCEPTION 'Inactive professional or specialty cannot be associated' USING ERRCODE='23514';
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER trg_new_professional_specialty
  BEFORE INSERT OR UPDATE ON professional_specialties
  FOR EACH ROW EXECUTE FUNCTION guard_new_professional_specialty();

CREATE FUNCTION guard_user_role_removal() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE v_role text;
BEGIN
  SELECT name INTO v_role FROM roles WHERE id=OLD.role_id;
  IF v_role='PATIENT' AND EXISTS (SELECT 1 FROM patients WHERE user_id=OLD.user_id) THEN
    RAISE EXCEPTION 'Linked patient role cannot be removed directly' USING ERRCODE='23514';
  END IF;
  IF v_role='PROFESSIONAL' AND EXISTS (SELECT 1 FROM professionals WHERE user_id=OLD.user_id) THEN
    RAISE EXCEPTION 'Linked professional role cannot be removed directly' USING ERRCODE='23514';
  END IF;
  IF v_role='ADMIN' AND EXISTS (SELECT 1 FROM users WHERE id=OLD.user_id
                              AND enabled AND deleted_at IS NULL)
    AND NOT EXISTS (
      SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id
        JOIN users u ON u.id=ur.user_id
      WHERE r.name='ADMIN' AND u.enabled AND u.deleted_at IS NULL
        AND u.id<>OLD.user_id
    ) THEN
    RAISE EXCEPTION 'Last active ADMIN role cannot be removed' USING ERRCODE='23514';
  END IF;
  RETURN OLD;
END $$;
CREATE TRIGGER trg_user_role_removal BEFORE DELETE ON user_roles
  FOR EACH ROW EXECUTE FUNCTION guard_user_role_removal();

CREATE FUNCTION guard_last_admin_disable() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF OLD.enabled AND OLD.deleted_at IS NULL
     AND (NOT NEW.enabled OR NEW.deleted_at IS NOT NULL)
     AND EXISTS (SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id
                 WHERE ur.user_id=OLD.id AND r.name='ADMIN')
     AND NOT EXISTS (
       SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id
         JOIN users u ON u.id=ur.user_id
       WHERE r.name='ADMIN' AND u.enabled AND u.deleted_at IS NULL AND u.id<>OLD.id
     ) THEN
    RAISE EXCEPTION 'Last active ADMIN account cannot be disabled' USING ERRCODE='23514';
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER trg_last_admin_disable BEFORE UPDATE OF enabled,deleted_at ON users
  FOR EACH ROW EXECUTE FUNCTION guard_last_admin_disable();

-- Application gateway additions. Every entry takes the same advisory gate before row locks.
CREATE FUNCTION capacity_schedule_create(p_professional uuid,p_specialty uuid,p_day integer,
  p_start time,p_end time) RETURNS uuid LANGUAGE plpgsql VOLATILE SECURITY DEFINER
  SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_id uuid := gen_random_uuid();
BEGIN
  PERFORM capacity_lock_context(ARRAY[p_professional]);
  INSERT INTO schedules(id,professional_id,specialty_id,day_of_week,start_time,end_time,active)
    VALUES(v_id,p_professional,p_specialty,p_day,p_start,p_end,true);
  RETURN v_id;
END $$;

CREATE FUNCTION capacity_schedule_reconfigure(p_schedule uuid,p_professional uuid,p_specialty uuid,
  p_day integer,p_start time,p_end time) RETURNS void LANGUAGE plpgsql VOLATILE SECURITY DEFINER
  SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_old_professional uuid;
BEGIN
  PERFORM pg_advisory_xact_lock(12020,2);
  SELECT professional_id INTO v_old_professional FROM schedules WHERE id=p_schedule;
  IF NOT FOUND THEN RAISE EXCEPTION 'Schedule missing' USING ERRCODE='23503'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_old_professional,p_professional],ARRAY[p_schedule]);
  UPDATE schedules SET professional_id=p_professional,specialty_id=p_specialty,
    day_of_week=p_day,start_time=p_start,end_time=p_end WHERE id=p_schedule;
END $$;

CREATE FUNCTION capacity_generate_slot_created(p_schedule uuid,p_date date,p_start time,p_end time)
RETURNS boolean LANGUAGE plpgsql VOLATILE SECURITY DEFINER
  SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_professional uuid; v_id uuid;
BEGIN
  PERFORM pg_advisory_xact_lock(12020,2);
  SELECT professional_id INTO v_professional FROM schedules WHERE id=p_schedule;
  IF NOT FOUND THEN RAISE EXCEPTION 'Schedule missing' USING ERRCODE='23503'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_professional],ARRAY[p_schedule]);
  INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status)
    VALUES(gen_random_uuid(),p_schedule,p_date,p_start,p_end,'AVAILABLE')
    ON CONFLICT (schedule_id,slot_date,start_time) DO NOTHING RETURNING id INTO v_id;
  RETURN v_id IS NOT NULL;
END $$;

CREATE FUNCTION capacity_cancel(p_appointment uuid,p_actor uuid)
RETURNS void LANGUAGE plpgsql VOLATILE SECURITY DEFINER
  SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_slot uuid; v_schedule uuid; v_professional uuid;
BEGIN
  PERFORM pg_advisory_xact_lock(12020,2);
  SELECT a.slot_id,s.id,a.professional_id INTO v_slot,v_schedule,v_professional
    FROM appointments a JOIN availability_slots sl ON sl.id=a.slot_id
    JOIN schedules s ON s.id=sl.schedule_id WHERE a.id=p_appointment;
  IF NOT FOUND THEN RAISE EXCEPTION 'Appointment missing' USING ERRCODE='23503'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_professional],ARRAY[v_schedule],ARRAY[v_slot],ARRAY[p_appointment]);
  UPDATE appointments SET appointment_status='CANCELLED',cancelled_at=hospital_business_now(),
    cancelled_by=p_actor WHERE id=p_appointment AND appointment_status IN ('SCHEDULED','CONFIRMED');
  IF NOT FOUND THEN RAISE EXCEPTION 'Appointment cannot be cancelled' USING ERRCODE='23514'; END IF;
  UPDATE availability_slots SET status='AVAILABLE' WHERE id=v_slot AND status='RESERVED';
  IF NOT FOUND THEN RAISE EXCEPTION 'Slot cannot be released' USING ERRCODE='23514'; END IF;
END $$;

CREATE FUNCTION capacity_lock_user(p_user uuid) RETURNS void LANGUAGE plpgsql VOLATILE
  SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_professional uuid;
BEGIN
  PERFORM pg_advisory_xact_lock(12020,2);
  SELECT id INTO v_professional FROM professionals WHERE user_id=p_user;
  IF v_professional IS NOT NULL THEN
    PERFORM capacity_lock_context(ARRAY[v_professional]);
  ELSE
    PERFORM 1 FROM users WHERE id=p_user FOR UPDATE;
    IF NOT FOUND THEN RAISE EXCEPTION 'User missing' USING ERRCODE='23503'; END IF;
  END IF;
END $$;

CREATE FUNCTION capacity_user_status(p_user uuid,p_enabled boolean) RETURNS void LANGUAGE plpgsql VOLATILE
  SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
BEGIN
  PERFORM capacity_lock_user(p_user);
  UPDATE users SET enabled=p_enabled WHERE id=p_user;
END $$;

-- Lock before application services read mutable appointment state. This keeps
-- duplicate lifecycle requests idempotent under the same capacity lock order.
CREATE FUNCTION capacity_lock_appointment(p_appointment uuid) RETURNS void LANGUAGE plpgsql VOLATILE
  SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_slot uuid; v_schedule uuid; v_professional uuid;
BEGIN
  PERFORM pg_advisory_xact_lock(12020,2);
  SELECT a.slot_id,s.id,a.professional_id INTO v_slot,v_schedule,v_professional
    FROM appointments a JOIN availability_slots sl ON sl.id=a.slot_id
    JOIN schedules s ON s.id=sl.schedule_id WHERE a.id=p_appointment;
  IF NOT FOUND THEN RAISE EXCEPTION 'Appointment missing' USING ERRCODE='23503'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_professional],ARRAY[v_schedule],ARRAY[v_slot],ARRAY[p_appointment]);
END $$;

CREATE FUNCTION capacity_lock_reschedule(p_appointment uuid,p_new_slot uuid) RETURNS void
  LANGUAGE plpgsql VOLATILE SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE v_old_slot uuid; v_old_schedule uuid; v_old_professional uuid;
        v_new_schedule uuid; v_new_professional uuid;
BEGIN
  PERFORM pg_advisory_xact_lock(12020,2);
  SELECT a.slot_id,s.id,a.professional_id INTO v_old_slot,v_old_schedule,v_old_professional
    FROM appointments a JOIN availability_slots sl ON sl.id=a.slot_id
    JOIN schedules s ON s.id=sl.schedule_id WHERE a.id=p_appointment;
  IF NOT FOUND THEN RAISE EXCEPTION 'Appointment missing' USING ERRCODE='23503'; END IF;
  SELECT s.id,s.professional_id INTO v_new_schedule,v_new_professional
    FROM availability_slots sl JOIN schedules s ON s.id=sl.schedule_id WHERE sl.id=p_new_slot;
  IF NOT FOUND THEN RAISE EXCEPTION 'New slot missing' USING ERRCODE='23503'; END IF;
  PERFORM capacity_lock_context(ARRAY[v_old_professional,v_new_professional],
    ARRAY[v_old_schedule,v_new_schedule],ARRAY[v_old_slot,p_new_slot],ARRAY[p_appointment]);
END $$;

-- Gateway entry points are granted to a restricted runtime role explicitly.
REVOKE EXECUTE ON ALL FUNCTIONS IN SCHEMA public FROM PUBLIC;
