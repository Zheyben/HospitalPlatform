package com.hospital.platform.medical.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.platform.medical.dto.MedicalPriorOrderDTO;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MedicalLongitudinalRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    public MedicalLongitudinalRepository(JdbcTemplate jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    public boolean belongsToPriorFinalizedAppointment(UUID currentAppointmentId, UUID patientId, UUID encounterId) {
        return !jdbc.query("""
                select 1
                from appointments current_appointment
                join availability_slots current_slot on current_slot.id = current_appointment.slot_id
                join clinical_final_records final_record on final_record.encounter_id = ?
                    and final_record.patient_id = current_appointment.patient_id
                join clinical_encounters prior_encounter on prior_encounter.id = final_record.encounter_id
                join appointments prior_appointment on prior_appointment.id = prior_encounter.appointment_id
                    and prior_appointment.patient_id = current_appointment.patient_id
                where current_appointment.id = ? and current_appointment.patient_id = ?
                  and prior_appointment.id <> current_appointment.id
                  and prior_encounter.status = 'FINALIZED'
                  and prior_appointment.appointment_status = 'COMPLETED'
                  and prior_appointment.flow_stage = 'FINISHED'
                  and (final_record.appointment_date, final_record.appointment_start_time)
                      < (current_slot.slot_date, current_slot.start_time)
                limit 1
                """, (rs, row) -> 1, encounterId, currentAppointmentId, patientId).isEmpty();
    }

    public Optional<JsonNode> history(UUID encounterId) {
        return jdbc.query("select (to_jsonb(h) - 'encounter_id')::text as content "
                        + "from clinical_history_entries h where h.encounter_id = ?",
                (rs, row) -> parse(rs.getString("content")), encounterId).stream().findFirst();
    }

    public Optional<MedicalPriorOrderDTO> order(UUID encounterId) {
        return jdbc.query("""
                select procedure_code_snapshot, procedure_name_snapshot, priority, status, requested_at
                from clinical_orders where encounter_id = ? limit 1
                """, (rs, row) -> new MedicalPriorOrderDTO(
                rs.getString("procedure_code_snapshot"), rs.getString("procedure_name_snapshot"),
                rs.getString("priority"), rs.getString("status"),
                rs.getObject("requested_at", OffsetDateTime.class).toInstant()), encounterId)
                .stream().findFirst();
    }

    public Optional<UUID> prescriptionId(UUID encounterId) {
        return jdbc.query("select id from clinical_prescriptions where encounter_id = ? limit 1",
                (rs, row) -> rs.getObject("id", UUID.class), encounterId).stream().findFirst();
    }

    private JsonNode parse(String value) {
        try {
            return json.readTree(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Invalid finalized clinical history", exception);
        }
    }
}
