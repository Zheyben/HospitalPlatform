package com.hospital.platform.medical.repository;

import com.hospital.platform.medical.dto.PatientEncounterDetailDTO;
import com.hospital.platform.medical.dto.PatientEncounterSummaryDTO;
import com.hospital.platform.medical.dto.PatientPrescriptionDetailDTO;
import com.hospital.platform.medical.dto.PatientPrescriptionDetailDTO.MedicationItem;
import com.hospital.platform.medical.dto.PatientPrescriptionSummaryDTO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PatientClinicalReadRepository {

    private final JdbcTemplate jdbc;

    public PatientClinicalReadRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<PatientEncounterSummaryDTO> encounters(UUID patientId, int limit, int offset) {
        return jdbc.query("""
                select f.encounter_id, f.appointment_date, f.finalized_at,
                       coalesce(nullif(btrim(concat_ws(' ', f.professional_first_name,
                           f.professional_last_name)), ''), f.professional_license_number) as doctor_name,
                       f.specialty_name, ass.reason, d.primary_diagnosis, d.icd10_code_snapshot
                from clinical_final_records f
                join clinical_encounters ce on ce.id = f.encounter_id
                join appointments a on a.id = ce.appointment_id
                join clinical_assessments ass on ass.encounter_id = f.encounter_id
                join clinical_diagnoses d on d.encounter_id = f.encounter_id
                where f.patient_id = ? and a.patient_id = ?
                  and ce.status = 'FINALIZED' and a.appointment_status = 'COMPLETED'
                  and a.flow_stage = 'FINISHED'
                order by f.finalized_at desc, f.encounter_id desc
                limit ? offset ?
                """, (rs, row) -> new PatientEncounterSummaryDTO(
                rs.getObject("encounter_id", UUID.class), rs.getDate("appointment_date").toLocalDate(),
                instant(rs, "finalized_at"), rs.getString("doctor_name"),
                rs.getString("specialty_name"), rs.getString("reason"),
                rs.getString("primary_diagnosis"), rs.getString("icd10_code_snapshot")),
                patientId, patientId, limit, offset);
    }

    public Optional<PatientEncounterDetailDTO> encounter(UUID patientId, UUID encounterId) {
        return jdbc.query("""
                select f.encounter_id, f.appointment_date, f.finalized_at,
                       coalesce(nullif(btrim(concat_ws(' ', f.professional_first_name,
                           f.professional_last_name)), ''), f.professional_license_number) as doctor_name,
                       f.specialty_name, ass.reason, ass.symptoms_and_current_illness,
                       d.primary_diagnosis, d.icd10_code_snapshot, d.icd10_description_snapshot,
                       d.diagnosis_type, t.therapeutic_plan, t.general_indications,
                       t.patient_education, t.warning_signs, t.suggested_follow_up_date,
                       t.follow_up_reason, t.pending_results_and_plan, t.complementary_observations,
                       f.patient_sex, f.patient_marital_status, f.patient_occupation,
                       f.patient_district, f.patient_education_level, f.patient_affiliation_number,
                       f.patient_emergency_contact_name, f.patient_emergency_contact_relationship,
                       f.patient_emergency_contact_phone
                from clinical_final_records f
                join clinical_encounters ce on ce.id = f.encounter_id
                join appointments a on a.id = ce.appointment_id
                join clinical_assessments ass on ass.encounter_id = f.encounter_id
                join clinical_diagnoses d on d.encounter_id = f.encounter_id
                join clinical_treatment_plans t on t.encounter_id = f.encounter_id
                where f.encounter_id = ? and f.patient_id = ? and a.patient_id = ?
                  and ce.status = 'FINALIZED' and a.appointment_status = 'COMPLETED'
                  and a.flow_stage = 'FINISHED'
                """, (rs, row) -> new PatientEncounterDetailDTO(
                rs.getObject("encounter_id", UUID.class), rs.getDate("appointment_date").toLocalDate(),
                instant(rs, "finalized_at"), rs.getString("doctor_name"),
                rs.getString("specialty_name"), rs.getString("reason"),
                rs.getString("symptoms_and_current_illness"), rs.getString("primary_diagnosis"),
                rs.getString("icd10_code_snapshot"), rs.getString("icd10_description_snapshot"),
                rs.getString("diagnosis_type"), rs.getString("therapeutic_plan"),
                rs.getString("general_indications"), rs.getString("patient_education"),
                rs.getString("warning_signs"), date(rs, "suggested_follow_up_date"),
                rs.getString("follow_up_reason"), rs.getString("pending_results_and_plan"),
                rs.getString("complementary_observations"), rs.getString("patient_sex"),
                rs.getString("patient_marital_status"), rs.getString("patient_occupation"),
                rs.getString("patient_district"), rs.getString("patient_education_level"),
                rs.getString("patient_affiliation_number"), rs.getString("patient_emergency_contact_name"),
                rs.getString("patient_emergency_contact_relationship"),
                rs.getString("patient_emergency_contact_phone")),
                encounterId, patientId, patientId).stream().findFirst();
    }

    public List<PatientPrescriptionSummaryDTO> prescriptions(UUID patientId, int limit, int offset) {
        return jdbc.query("""
                select rx.id, f.encounter_id, rx.prescription_number, rx.issued_at,
                       coalesce(nullif(btrim(concat_ws(' ', f.professional_first_name,
                           f.professional_last_name)), ''), f.professional_license_number) as doctor_name,
                       f.specialty_name, d.primary_diagnosis, d.icd10_code_snapshot
                from clinical_prescriptions rx
                join clinical_final_records f on f.encounter_id = rx.encounter_id
                join clinical_encounters ce on ce.id = f.encounter_id
                join appointments a on a.id = ce.appointment_id
                join clinical_diagnoses d on d.encounter_id = f.encounter_id
                where f.patient_id = ? and a.patient_id = ?
                  and ce.status = 'FINALIZED' and a.appointment_status = 'COMPLETED'
                  and a.flow_stage = 'FINISHED'
                  and exists (select 1 from clinical_prescription_items item where item.prescription_id = rx.id)
                order by rx.issued_at desc, rx.id desc
                limit ? offset ?
                """, (rs, row) -> new PatientPrescriptionSummaryDTO(
                rs.getObject("id", UUID.class), rs.getObject("encounter_id", UUID.class),
                rs.getString("prescription_number"), instant(rs, "issued_at"),
                rs.getString("doctor_name"), rs.getString("specialty_name"),
                rs.getString("primary_diagnosis"), rs.getString("icd10_code_snapshot")),
                patientId, patientId, limit, offset);
    }

    public Optional<PatientPrescriptionDetailDTO> prescription(UUID patientId, UUID prescriptionId) {
        return jdbc.query("""
                select rx.id, f.encounter_id, rx.prescription_number, rx.issued_at,
                       coalesce(nullif(btrim(concat_ws(' ', f.patient_first_name,
                           f.patient_last_name)), ''), f.patient_document_number) as patient_name,
                       f.patient_document_type, f.patient_document_number,
                       coalesce(nullif(btrim(concat_ws(' ', f.professional_first_name,
                           f.professional_last_name)), ''), f.professional_license_number) as doctor_name,
                       f.specialty_name, d.primary_diagnosis, d.icd10_code_snapshot,
                       rx.additional_precautions, rx.non_pharmacological_recommendations,
                       rx.warning_signs, rx.additional_care, rx.follow_up_observations
                from clinical_prescriptions rx
                join clinical_final_records f on f.encounter_id = rx.encounter_id
                join clinical_encounters ce on ce.id = f.encounter_id
                join appointments a on a.id = ce.appointment_id
                join clinical_diagnoses d on d.encounter_id = f.encounter_id
                where rx.id = ? and f.patient_id = ? and a.patient_id = ?
                  and ce.status = 'FINALIZED' and a.appointment_status = 'COMPLETED'
                  and a.flow_stage = 'FINISHED'
                  and exists (select 1 from clinical_prescription_items item where item.prescription_id = rx.id)
                """, (rs, row) -> new PatientPrescriptionDetailDTO(
                rs.getObject("id", UUID.class), rs.getObject("encounter_id", UUID.class),
                rs.getString("prescription_number"), instant(rs, "issued_at"),
                rs.getString("patient_name"), rs.getString("patient_document_type"),
                rs.getString("patient_document_number"), rs.getString("doctor_name"),
                rs.getString("specialty_name"), rs.getString("primary_diagnosis"),
                rs.getString("icd10_code_snapshot"), rs.getString("additional_precautions"),
                rs.getString("non_pharmacological_recommendations"), rs.getString("warning_signs"),
                rs.getString("additional_care"), rs.getString("follow_up_observations"),
                prescriptionItems(patientId, prescriptionId)),
                prescriptionId, patientId, patientId).stream().findFirst();
    }

    private List<MedicationItem> prescriptionItems(UUID patientId, UUID prescriptionId) {
        return jdbc.query("""
                select item.item_number, item.medication_name_snapshot, item.commercial_name_snapshot,
                       item.presentation_name_snapshot, item.concentration_snapshot,
                       item.pharmaceutical_form_snapshot, item.dose, item.dose_unit,
                       item.frequency, item.route, item.duration, item.duration_unit,
                       item.quantity, item.usage_instructions
                from clinical_prescription_items item
                join clinical_prescriptions rx on rx.id = item.prescription_id
                join clinical_final_records f on f.encounter_id = rx.encounter_id
                join clinical_encounters ce on ce.id = f.encounter_id
                join appointments a on a.id = ce.appointment_id
                where rx.id = ? and f.patient_id = ? and a.patient_id = ?
                  and ce.status = 'FINALIZED' and a.appointment_status = 'COMPLETED'
                  and a.flow_stage = 'FINISHED'
                order by item.item_number
                """, (rs, row) -> new MedicationItem(
                rs.getInt("item_number"), rs.getString("medication_name_snapshot"),
                rs.getString("commercial_name_snapshot"), rs.getString("presentation_name_snapshot"),
                rs.getString("concentration_snapshot"), rs.getString("pharmaceutical_form_snapshot"),
                rs.getBigDecimal("dose"), rs.getString("dose_unit"), rs.getString("frequency"),
                rs.getString("route"), rs.getBigDecimal("duration"), rs.getString("duration_unit"),
                rs.getInt("quantity"), rs.getString("usage_instructions")),
                prescriptionId, patientId, patientId);
    }

    private java.time.Instant instant(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, OffsetDateTime.class).toInstant();
    }

    private java.time.LocalDate date(ResultSet rs, String column) throws SQLException {
        java.sql.Date value = rs.getDate(column);
        return value == null ? null : value.toLocalDate();
    }
}
