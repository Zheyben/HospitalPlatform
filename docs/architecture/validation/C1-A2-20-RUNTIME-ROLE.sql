-- Experimental capacity-only role grants. Run only in an isolated validation DB.
-- The application-wide production role requires a separate privilege inventory.
BEGIN;
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM c1_runtime;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA public FROM c1_runtime;
GRANT USAGE ON SCHEMA public TO c1_runtime;
GRANT SELECT ON users,patients,professionals,specialties,professional_specialties,
  schedules,availability_slots,appointments TO c1_runtime;
GRANT EXECUTE ON FUNCTION hospital_business_now() TO c1_runtime;
GRANT EXECUTE ON FUNCTION capacity_reserve(uuid,uuid,text) TO c1_runtime;
GRANT EXECUTE ON FUNCTION capacity_cancel(uuid) TO c1_runtime;
GRANT EXECUTE ON FUNCTION capacity_reschedule(uuid,uuid) TO c1_runtime;
GRANT EXECUTE ON FUNCTION capacity_schedule_status(uuid,boolean) TO c1_runtime;
GRANT EXECUTE ON FUNCTION capacity_schedule_edit(uuid,time,time) TO c1_runtime;
GRANT EXECUTE ON FUNCTION capacity_generate_slot(uuid,date,time,time) TO c1_runtime;
GRANT EXECUTE ON FUNCTION capacity_appointment_stage(uuid,text) TO c1_runtime;
GRANT EXECUTE ON FUNCTION capacity_appointment_confirm(uuid) TO c1_runtime;
GRANT EXECUTE ON FUNCTION capacity_appointment_complete(uuid) TO c1_runtime;
GRANT EXECUTE ON FUNCTION capacity_professional_deactivate(uuid) TO c1_runtime;
GRANT EXECUTE ON FUNCTION capacity_user_disable(uuid) TO c1_runtime;
COMMIT;
