package com.hospital.platform.medical.repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MedicalFinalizationRepository {

    private final JdbcTemplate jdbc;

    public MedicalFinalizationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<UUID> appointmentId(UUID encounterId) {
        return jdbc.query("select appointment_id from clinical_encounters where id = ?",
                (rs, row) -> rs.getObject(1, UUID.class), encounterId).stream().findFirst();
    }

    public Optional<FinalizationRow> find(UUID encounterId, UUID professionalId) {
        return jdbc.query("""
                select ce.appointment_id, ce.status, a.appointment_status, a.flow_stage,
                       d.version, d.content::text as content, f.draft_version,
                       f.content_sha256, f.finalized_at, rx.prescription_number
                from clinical_encounters ce
                join appointments a on a.id = ce.appointment_id
                left join encounter_drafts d on d.encounter_id = ce.id
                left join clinical_final_records f on f.encounter_id = ce.id
                left join clinical_prescriptions rx on rx.encounter_id = ce.id
                where ce.id = ? and a.professional_id = ?
                """, (rs, row) -> {
            OffsetDateTime finalizedAt = rs.getObject("finalized_at", OffsetDateTime.class);
            return new FinalizationRow(rs.getObject("appointment_id", UUID.class), rs.getString("status"),
                    rs.getString("appointment_status"), rs.getString("flow_stage"),
                    rs.getObject("version", Integer.class), rs.getString("content"),
                    rs.getObject("draft_version", Integer.class), rs.getString("content_sha256"),
                    finalizedAt == null ? null : finalizedAt.toInstant(),
                    rs.getString("prescription_number"));
        }, encounterId, professionalId).stream().findFirst();
    }

    public boolean lockActiveIcd10(UUID id) {
        return lockedActive("select medical_lock_active_icd10(?)", id);
    }

    public boolean lockActiveProcedure(UUID id) {
        return lockedActive("select medical_lock_active_procedure(?)", id);
    }

    public boolean lockActiveSpecialty(UUID id) {
        return lockedActive("select medical_lock_active_specialty(?)", id);
    }

    public boolean lockActivePresentation(UUID medicationId, UUID presentationId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "select medical_lock_active_presentation(?, ?)", Boolean.class,
                medicationId, presentationId));
    }

    private boolean lockedActive(String sql, UUID id) {
        return jdbc.query(sql, (rs, row) -> rs.getBoolean(1), id)
                .stream().findFirst().orElse(false);
    }

    public int insertRecord(UUID encounterId, Instant finalizedAt, int version, String hash) {
        return jdbc.update("""
                insert into clinical_final_records (
                    encounter_id, patient_id, professional_id, clinical_record_number,
                    patient_first_name, patient_last_name, patient_document_type, patient_document_number,
                    patient_birth_date, patient_insurance, patient_address, patient_phone, patient_sex,
                    patient_marital_status, patient_occupation, patient_district, patient_education_level,
                    patient_affiliation_number, patient_emergency_contact_name,
                    patient_emergency_contact_relationship, patient_emergency_contact_phone,
                    professional_first_name, professional_last_name, professional_license_number,
                    professional_simulated_rne, specialty_name, appointment_date, appointment_start_time,
                    finalized_at, draft_version, content_sha256)
                select ce.id, pt.id, p.id, cr.record_number, pu.first_name, pu.last_name,
                       pt.document_type, pt.document_number, pt.birth_date, pt.insurance,
                       pt.address, pt.phone, pt.sex, pt.marital_status, pt.occupation, pt.district,
                       pt.education_level, pt.affiliation_number, pt.emergency_contact_name,
                       pt.emergency_contact_relationship, pt.emergency_contact_phone,
                       u.first_name, u.last_name, p.license_number,
                       p.simulated_rne, sp.name, sl.slot_date, sl.start_time, ?, ?, ?
                from clinical_encounters ce
                join appointments a on a.id = ce.appointment_id
                join patients pt on pt.id = a.patient_id
                left join users pu on pu.id = pt.user_id
                join clinical_records cr on cr.patient_id = pt.id
                join professionals p on p.id = a.professional_id
                join users u on u.id = p.user_id
                join professional_specialties ps on ps.professional_id = p.id
                join specialties sp on sp.id = ps.specialty_id
                join availability_slots sl on sl.id = a.slot_id
                where ce.id = ? and ce.status = 'OPEN'
                """, Timestamp.from(finalizedAt), version, hash, encounterId);
    }

    public void insertHistory(UUID encounterId) {
        jdbc.update("""
                insert into clinical_history_entries (
                    encounter_id, pathological, surgical, allergies_and_reactions, usual_medication,
                    transfusions, relevant_habits, hospitalizations, other_personal, father_history,
                    mother_history, siblings_history, children_history, grandparents_history,
                    other_family, family_source, family_source_date, family_observation, pregnancies,
                    births, miscarriages, cesareans, last_menstrual_period, other_gynecologic, other_alerts)
                select encounter_id,
                       content #>> '{history,personal,pathological}',
                       content #>> '{history,personal,surgical}',
                       content #>> '{history,personal,allergiesAndReactions}',
                       content #>> '{history,personal,usualMedication}',
                       content #>> '{history,personal,transfusions}',
                       content #>> '{history,personal,relevantHabits}',
                       content #>> '{history,personal,hospitalizations}',
                       content #>> '{history,personal,other}',
                       content #>> '{history,family,father}', content #>> '{history,family,mother}',
                       content #>> '{history,family,siblings}', content #>> '{history,family,children}',
                       content #>> '{history,family,grandparents}', content #>> '{history,family,other}',
                       content #>> '{history,family,source}',
                       (content #>> '{history,family,sourceDate}')::date,
                       content #>> '{history,family,observation}',
                       (content #>> '{history,gynecologic,pregnancies}')::integer,
                       (content #>> '{history,gynecologic,births}')::integer,
                       (content #>> '{history,gynecologic,miscarriages}')::integer,
                       (content #>> '{history,gynecologic,cesareans}')::integer,
                       (content #>> '{history,gynecologic,lastMenstrualPeriod}')::date,
                       content #>> '{history,gynecologic,other}', content #>> '{history,otherAlerts}'
                from encounter_drafts where encounter_id = ?
                """, encounterId);
    }

    public void insertAssessment(UUID encounterId) {
        jdbc.update("""
                insert into clinical_assessments (
                    encounter_id, reason, symptoms_and_current_illness, illness_duration,
                    illness_duration_unit, biological_functions, reviewed_history_and_allergies,
                    prior_treatment_and_response, systolic_blood_pressure, diastolic_blood_pressure,
                    heart_rate, respiratory_rate, temperature_celsius, oxygen_saturation_percent,
                    weight_kg, height_cm, general_condition, head_and_neck,
                    cardiopulmonary_and_abdomen, extremities_and_neurologic, other_findings)
                select encounter_id,
                       content #>> '{assessment,presentation,reason}',
                       content #>> '{assessment,presentation,symptomsAndCurrentIllness}',
                       (content #>> '{assessment,presentation,illnessDuration}')::numeric,
                       content #>> '{assessment,presentation,illnessDurationUnit}',
                       content #>> '{assessment,presentation,biologicalFunctions}',
                       content #>> '{assessment,presentation,reviewedHistoryAndAllergies}',
                       content #>> '{assessment,presentation,priorTreatmentAndResponse}',
                       (content #>> '{assessment,vitalSigns,systolicBloodPressure}')::integer,
                       (content #>> '{assessment,vitalSigns,diastolicBloodPressure}')::integer,
                       (content #>> '{assessment,vitalSigns,heartRate}')::integer,
                       (content #>> '{assessment,vitalSigns,respiratoryRate}')::integer,
                       (content #>> '{assessment,vitalSigns,temperatureCelsius}')::numeric,
                       (content #>> '{assessment,vitalSigns,oxygenSaturationPercent}')::integer,
                       (content #>> '{assessment,vitalSigns,weightKg}')::numeric,
                       (content #>> '{assessment,vitalSigns,heightCm}')::numeric,
                       content #>> '{assessment,examination,generalCondition}',
                       content #>> '{assessment,examination,headAndNeck}',
                       content #>> '{assessment,examination,cardiopulmonaryAndAbdomen}',
                       content #>> '{assessment,examination,extremitiesAndNeurologic}',
                       content #>> '{assessment,examination,otherFindings}'
                from encounter_drafts where encounter_id = ?
                """, encounterId);
    }

    public void insertDiagnosis(UUID encounterId) {
        jdbc.update("""
                insert into clinical_diagnoses (
                    encounter_id, primary_diagnosis, icd10_code_id, icd10_code_snapshot,
                    icd10_description_snapshot, diagnosis_type, observations)
                select d.encounter_id, d.content #>> '{assessment,diagnosis,primaryDiagnosis}',
                       c.id, c.code, c.description,
                       d.content #>> '{assessment,diagnosis,diagnosisType}',
                       d.content #>> '{assessment,diagnosis,observations}'
                from encounter_drafts d
                join icd10_codes c on c.id = (d.content #>> '{assessment,diagnosis,icd10CodeId}')::uuid
                where d.encounter_id = ? and c.active
                """, encounterId);
    }

    public void insertTreatment(UUID encounterId) {
        jdbc.update("""
                insert into clinical_treatment_plans (
                    encounter_id, therapeutic_plan, general_indications, patient_education,
                    warning_signs, referral_type, referral_specialty_id, referral_specialty_snapshot,
                    suggested_follow_up_date, follow_up_reason, pending_results_and_plan,
                    complementary_observations)
                select d.encounter_id,
                       d.content #>> '{assessment,treatmentPlan,therapeuticPlan}',
                       d.content #>> '{assessment,treatmentPlan,generalIndications}',
                       d.content #>> '{assessment,treatmentPlan,patientEducation}',
                       d.content #>> '{assessment,treatmentPlan,warningSigns}',
                       d.content #>> '{assessment,treatmentPlan,referralType}', sp.id, sp.name,
                       (d.content #>> '{assessment,treatmentPlan,suggestedFollowUpDate}')::date,
                       d.content #>> '{assessment,treatmentPlan,followUpReason}',
                       d.content #>> '{assessment,treatmentPlan,pendingResultsAndPlan}',
                       d.content #>> '{assessment,treatmentPlan,complementaryObservations}'
                from encounter_drafts d
                left join specialties sp on sp.id =
                    (d.content #>> '{assessment,treatmentPlan,referralSpecialtyId}')::uuid
                where d.encounter_id = ?
                """, encounterId);
    }

    public void insertOrder(UUID encounterId, Instant requestedAt) {
        jdbc.update("""
                insert into clinical_orders (
                    encounter_id, procedure_id, procedure_code_snapshot, procedure_name_snapshot,
                    priority, requested_at)
                select d.encounter_id, p.id, p.code, p.name,
                       d.content #>> '{assessment,diagnosis,priority}', ?
                from encounter_drafts d
                join procedures p on p.id = (d.content #>> '{assessment,diagnosis,procedureId}')::uuid
                where d.encounter_id = ? and p.active
                """, Timestamp.from(requestedAt), encounterId);
    }

    public UUID insertPrescription(UUID encounterId, Instant issuedAt) {
        return jdbc.queryForObject("""
                insert into clinical_prescriptions (
                    encounter_id, issued_at, additional_precautions,
                    non_pharmacological_recommendations, warning_signs, additional_care,
                    follow_up_observations)
                select encounter_id, ?, content #>> '{prescription,additionalPrecautions}',
                       content #>> '{prescription,nonPharmacologicalRecommendations}',
                       content #>> '{prescription,warningSigns}',
                       content #>> '{prescription,additionalCare}',
                       content #>> '{prescription,followUpObservations}'
                from encounter_drafts where encounter_id = ? returning id
                """, UUID.class, Timestamp.from(issuedAt), encounterId);
    }

    public int insertPrescriptionItem(UUID prescriptionId, int itemNumber, UUID medicationId,
                                      UUID presentationId, UUID encounterId) {
        return jdbc.update("""
                insert into clinical_prescription_items (
                    prescription_id, item_number, medication_id, presentation_id,
                    medication_name_snapshot, commercial_name_snapshot, presentation_name_snapshot,
                    concentration_snapshot, pharmaceutical_form_snapshot, dose, dose_unit,
                    frequency, route, duration, duration_unit, quantity, usage_instructions)
                select ?, ?, m.id, p.id, m.generic_name, m.commercial_name, p.name,
                       p.concentration, p.pharmaceutical_form,
                       (item.value ->> 'dose')::numeric, item.value ->> 'doseUnit',
                       item.value ->> 'frequency', item.value ->> 'route',
                       (item.value ->> 'duration')::numeric, item.value ->> 'durationUnit',
                       (item.value ->> 'quantity')::integer, item.value ->> 'usageInstructions'
                from encounter_drafts d,
                     jsonb_array_elements(d.content #> '{prescription,items}') with ordinality item(value, position)
                join medications m on m.id = ?
                join medication_presentations p on p.id = ? and p.medication_id = m.id
                where d.encounter_id = ? and item.position = ? and m.active and p.active
                """, prescriptionId, itemNumber, medicationId, presentationId, encounterId, itemNumber);
    }

    public int markFinalized(UUID encounterId, Instant finalizedAt) {
        return jdbc.update("""
                update clinical_encounters set status = 'FINALIZED', updated_at = ?
                where id = ? and status = 'OPEN'
                """, Timestamp.from(finalizedAt), encounterId);
    }

    public record FinalizationRow(UUID appointmentId, String status, String appointmentStatus,
                                  String flowStage, Integer version, String content,
                                  Integer finalVersion, String finalHash, Instant finalizedAt,
                                  String prescriptionNumber) {
    }
}
