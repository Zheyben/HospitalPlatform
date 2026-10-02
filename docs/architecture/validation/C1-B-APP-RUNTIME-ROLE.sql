-- Isolated validation only. Create the LOGIN role separately with an ephemeral
-- password, then apply this script as the database owner on a V5 database.
-- Schema migration and development seed are owner operations before this step.
BEGIN;
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM c1b_app_runtime;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA public FROM c1b_app_runtime;
GRANT USAGE ON SCHEMA public TO c1b_app_runtime;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO c1b_app_runtime;
GRANT INSERT, UPDATE, DELETE ON users, patients, user_roles, refresh_tokens, audit_logs
  TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION hospital_business_now() TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_reserve(uuid,uuid,text) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_cancel(uuid,uuid) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_reschedule(uuid,uuid) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_appointment_confirm(uuid) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_appointment_stage(uuid,text) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_appointment_complete(uuid) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_schedule_create(uuid,uuid,integer,time,time) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_schedule_reconfigure(uuid,uuid,uuid,integer,time,time)
  TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_schedule_status(uuid,boolean) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_generate_slot_created(uuid,date,time,time) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_professional_deactivate(uuid) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_user_disable(uuid) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_user_status(uuid,boolean) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_lock_user(uuid) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_lock_appointment(uuid) TO c1b_app_runtime;
GRANT EXECUTE ON FUNCTION capacity_lock_reschedule(uuid,uuid) TO c1b_app_runtime;
COMMIT;
