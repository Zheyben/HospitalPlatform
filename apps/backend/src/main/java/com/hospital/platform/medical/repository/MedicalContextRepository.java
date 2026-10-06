package com.hospital.platform.medical.repository;

import com.hospital.platform.medical.dto.MedicalAppointmentContextDTO;
import com.hospital.platform.medical.dto.MedicalPriorEncounterDTO;
import com.hospital.platform.medical.dto.MedicalReadyAppointmentDTO;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MedicalContextRepository {

    private final JdbcTemplate jdbc;

    public MedicalContextRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ProfessionalRow> findActiveProfessional(UUID userId) {
        return jdbc.query("""
                select p.id, u.first_name, u.last_name, p.license_number, p.simulated_rne,
                       sp.id as specialty_id, sp.name as specialty_name
                from professionals p
                join users u on u.id = p.user_id and u.enabled and u.deleted_at is null
                join professional_specialties ps on ps.professional_id = p.id
                join specialties sp on sp.id = ps.specialty_id and sp.active and sp.deleted_at is null
                where p.user_id = ? and p.deleted_at is null
                """, (rs, rowNum) -> new ProfessionalRow(
                rs.getObject("id", UUID.class), rs.getString("first_name"), rs.getString("last_name"),
                rs.getString("license_number"), rs.getString("simulated_rne"),
                rs.getObject("specialty_id", UUID.class),
                rs.getString("specialty_name")), userId).stream().findFirst();
    }

    public List<MedicalReadyAppointmentDTO> findReadyAppointments(UUID professionalId, LocalDate today) {
        return jdbc.query("""
                select a.id, sl.slot_date, sl.start_time, a.flow_stage
                from appointments a
                join availability_slots sl on sl.id = a.slot_id
                where a.professional_id = ? and sl.slot_date = ?
                  and a.appointment_status = 'CONFIRMED'
                  and a.flow_stage in ('WAITING', 'IN_ATTENTION')
                order by case when a.flow_stage = 'IN_ATTENTION' then 0 else 1 end,
                         sl.start_time, a.id
                limit 50
                """, (rs, rowNum) -> new MedicalReadyAppointmentDTO(
                rs.getObject("id", UUID.class), rs.getDate("slot_date").toLocalDate(),
                rs.getTime("start_time").toLocalTime(), rs.getString("flow_stage")),
                professionalId, today);
    }

    public Optional<MedicalAppointmentContextDTO> findAssignedAppointment(
            UUID professionalId, UUID appointmentId, LocalDate today
    ) {
        return jdbc.query("""
                select a.id, sl.slot_date, sl.start_time, sl.end_time, a.flow_stage, a.reason,
                       pt.id as patient_id, pu.first_name, pu.last_name, pt.document_type,
                       pt.document_number, pt.birth_date, pt.phone, pt.insurance, pt.address, pt.sex,
                       pt.marital_status, pt.occupation, pt.district, pt.education_level,
                       pt.affiliation_number, pt.emergency_contact_name,
                       pt.emergency_contact_relationship, pt.emergency_contact_phone,
                       cr.record_number, cr.opened_at
                from appointments a
                join availability_slots sl on sl.id = a.slot_id
                join patients pt on pt.id = a.patient_id
                left join users pu on pu.id = pt.user_id
                join clinical_records cr on cr.patient_id = pt.id
                where a.id = ? and a.professional_id = ? and sl.slot_date = ?
                  and a.appointment_status = 'CONFIRMED'
                  and a.flow_stage in ('WAITING', 'IN_ATTENTION')
                """, (rs, rowNum) -> new MedicalAppointmentContextDTO(
                rs.getObject("id", UUID.class), rs.getDate("slot_date").toLocalDate(),
                rs.getTime("start_time").toLocalTime(), rs.getTime("end_time").toLocalTime(),
                rs.getString("flow_stage"), rs.getString("reason"),
                rs.getObject("patient_id", UUID.class), rs.getString("first_name"),
                rs.getString("last_name"), rs.getString("document_type"),
                rs.getString("document_number"),
                rs.getDate("birth_date") == null ? null : rs.getDate("birth_date").toLocalDate(),
                rs.getString("phone"), rs.getString("insurance"), rs.getString("address"),
                rs.getString("sex"),
                rs.getString("marital_status"), rs.getString("occupation"), rs.getString("district"),
                rs.getString("education_level"), rs.getString("affiliation_number"),
                rs.getString("emergency_contact_name"), rs.getString("emergency_contact_relationship"),
                rs.getString("emergency_contact_phone"),
                rs.getString("record_number"), rs.getTimestamp("opened_at").toLocalDateTime()),
                appointmentId, professionalId, today).stream().findFirst();
    }

    public List<MedicalPriorEncounterDTO> findPriorEncounters(
            UUID appointmentId, UUID professionalId, LocalDate today, int limit, int offset
    ) {
        return jdbc.query("""
                select f.encounter_id, f.appointment_date, f.finalized_at,
                       ce.simulated_service,
                       coalesce(nullif(btrim(concat_ws(' ', f.professional_first_name,
                           f.professional_last_name)), ''), f.professional_license_number) as doctor_name,
                       f.specialty_name, ass.reason, d.primary_diagnosis, d.icd10_code_snapshot
                from appointments target
                join availability_slots target_slot on target_slot.id = target.slot_id
                join clinical_final_records f on f.patient_id = target.patient_id
                join clinical_encounters ce on ce.id = f.encounter_id
                join appointments previous on previous.id = ce.appointment_id
                    and previous.patient_id = target.patient_id
                join clinical_assessments ass on ass.encounter_id = f.encounter_id
                join clinical_diagnoses d on d.encounter_id = f.encounter_id
                where target.id = ? and target.professional_id = ? and target_slot.slot_date = ?
                  and target.appointment_status = 'CONFIRMED'
                  and target.flow_stage in ('WAITING', 'IN_ATTENTION')
                  and ce.status = 'FINALIZED' and previous.appointment_status = 'COMPLETED'
                  and previous.flow_stage = 'FINISHED'
                  and (f.appointment_date, f.appointment_start_time)
                      < (target_slot.slot_date, target_slot.start_time)
                order by f.finalized_at desc, f.encounter_id desc
                limit ? offset ?
                """, (rs, row) -> new MedicalPriorEncounterDTO(
                rs.getObject("encounter_id", UUID.class), rs.getDate("appointment_date").toLocalDate(),
                rs.getObject("finalized_at", OffsetDateTime.class).toInstant(),
                rs.getString("simulated_service"), rs.getString("doctor_name"),
                rs.getString("specialty_name"), rs.getString("reason"),
                rs.getString("primary_diagnosis"), rs.getString("icd10_code_snapshot")),
                appointmentId, professionalId, today, limit, offset);
    }

    public record ProfessionalRow(UUID id, String firstName, String lastName, String licenseNumber,
                                  String simulatedRne,
                                  UUID specialtyId, String specialtyName) {
    }
}
