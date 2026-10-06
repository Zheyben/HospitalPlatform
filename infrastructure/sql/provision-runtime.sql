-- Run as the migration/schema owner after Flyway migrations. Before executing,
-- set session GUC hp.runtime_password to a secret supplied outside this file.
-- Do not commit credentials or run Flyway as hospital_app_runtime.
begin;
do $$
begin
    if nullif(current_setting('hp.runtime_password', true), '') is null then
        raise exception 'hp.runtime_password must be set for this session';
    end if;
    if not exists (select 1 from pg_roles where rolname = 'hospital_app_runtime') then
        create role hospital_app_runtime login;
    end if;
    execute format('alter role hospital_app_runtime password %L',
        current_setting('hp.runtime_password'));
end;
$$;

revoke create on schema public from public;
revoke all on all tables in schema public from hospital_app_runtime;
revoke all on all sequences in schema public from hospital_app_runtime;
revoke all on all functions in schema public from hospital_app_runtime;
grant usage on schema public to hospital_app_runtime;
grant select on all tables in schema public to hospital_app_runtime;

grant insert, update, delete on users, patients, user_roles, refresh_tokens, audit_logs
    to hospital_app_runtime;
grant insert, update on professionals, professional_specialties,
    clinical_encounters, encounter_drafts to hospital_app_runtime;
grant insert on clinical_final_records, clinical_history_entries, clinical_assessments,
    clinical_diagnoses, clinical_treatment_plans, clinical_orders,
    clinical_prescriptions, clinical_prescription_items to hospital_app_runtime;
grant usage on sequence clinical_prescription_number_seq to hospital_app_runtime;

grant execute on function hospital_business_now() to hospital_app_runtime;
grant execute on function capacity_reserve(uuid,uuid,text) to hospital_app_runtime;
grant execute on function capacity_appointment_confirm(uuid) to hospital_app_runtime;
grant execute on function capacity_appointment_stage(uuid,text) to hospital_app_runtime;
grant execute on function capacity_appointment_complete(uuid) to hospital_app_runtime;
grant execute on function capacity_schedule_create(uuid,uuid,integer,time,time)
    to hospital_app_runtime;
grant execute on function capacity_generate_slot_created(uuid,date,time,time)
    to hospital_app_runtime;
grant execute on function capacity_lock_appointment(uuid) to hospital_app_runtime;
grant execute on function medical_lock_active_icd10(uuid) to hospital_app_runtime;
grant execute on function medical_lock_active_procedure(uuid) to hospital_app_runtime;
grant execute on function medical_lock_active_specialty(uuid) to hospital_app_runtime;
grant execute on function medical_lock_active_presentation(uuid,uuid) to hospital_app_runtime;
commit;
