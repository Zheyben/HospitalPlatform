-- Test-only runtime role. Flyway and synthetic catalog fixtures use the schema owner.
begin;
revoke create on schema public from public;
revoke all on all tables in schema public from hospital_app_runtime_it;
revoke all on all sequences in schema public from hospital_app_runtime_it;
revoke all on all functions in schema public from hospital_app_runtime_it;
grant usage on schema public to hospital_app_runtime_it;
grant select on all tables in schema public to hospital_app_runtime_it;

grant insert, update, delete on users, patients, user_roles, refresh_tokens, audit_logs
    to hospital_app_runtime_it;
grant insert, update on professionals, professional_specialties,
    clinical_encounters, encounter_drafts to hospital_app_runtime_it;
grant insert on clinical_final_records, clinical_history_entries, clinical_assessments,
    clinical_diagnoses, clinical_treatment_plans, clinical_orders,
    clinical_prescriptions, clinical_prescription_items to hospital_app_runtime_it;
grant usage on sequence clinical_prescription_number_seq to hospital_app_runtime_it;

grant execute on function hospital_business_now() to hospital_app_runtime_it;
grant execute on function capacity_reserve(uuid,uuid,text) to hospital_app_runtime_it;
grant execute on function capacity_appointment_confirm(uuid) to hospital_app_runtime_it;
grant execute on function capacity_appointment_stage(uuid,text) to hospital_app_runtime_it;
grant execute on function capacity_appointment_complete(uuid) to hospital_app_runtime_it;
grant execute on function capacity_schedule_create(uuid,uuid,integer,time,time)
    to hospital_app_runtime_it;
grant execute on function capacity_generate_slot_created(uuid,date,time,time)
    to hospital_app_runtime_it;
grant execute on function capacity_lock_appointment(uuid) to hospital_app_runtime_it;
grant execute on function medical_lock_active_icd10(uuid) to hospital_app_runtime_it;
grant execute on function medical_lock_active_procedure(uuid) to hospital_app_runtime_it;
grant execute on function medical_lock_active_specialty(uuid) to hospital_app_runtime_it;
grant execute on function medical_lock_active_presentation(uuid,uuid) to hospital_app_runtime_it;
commit;
