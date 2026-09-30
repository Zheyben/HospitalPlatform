-- ============================================================
-- Hospital Platform
-- Flyway Migration
-- V3__support_appointment_lifecycle.sql
-- ============================================================

ALTER TABLE appointments
    DROP CONSTRAINT appointments_slot_id_key;

ALTER TABLE appointments
    ADD COLUMN rescheduled_from_id UUID NULL,
    ADD CONSTRAINT fk_appointments_rescheduled_from
        FOREIGN KEY (rescheduled_from_id)
        REFERENCES appointments(id),
    ADD CONSTRAINT uq_appointments_rescheduled_from
        UNIQUE (rescheduled_from_id),
    ADD CONSTRAINT chk_appointments_rescheduled_from_not_self
        CHECK (rescheduled_from_id IS NULL OR rescheduled_from_id <> id);

CREATE UNIQUE INDEX uq_appointments_active_slot
    ON appointments(slot_id)
    WHERE appointment_status IN ('SCHEDULED', 'CONFIRMED');
