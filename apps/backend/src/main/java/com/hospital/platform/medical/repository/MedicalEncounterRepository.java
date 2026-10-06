package com.hospital.platform.medical.repository;

import com.hospital.platform.medical.dto.MedicalEncounterStartDTO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MedicalEncounterRepository {

    private final JdbcTemplate jdbc;

    public MedicalEncounterRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int insertOpen(UUID encounterId, UUID appointmentId, Instant startedAt) {
        return jdbc.update("""
                insert into clinical_encounters (id, appointment_id, started_at)
                select ?, a.id, ? from appointments a
                where a.id = ? and a.appointment_status = 'CONFIRMED'
                  and a.flow_stage = 'IN_ATTENTION'
                on conflict (appointment_id) do nothing
                """, encounterId, java.sql.Timestamp.from(startedAt), appointmentId);
    }

    public Optional<MedicalEncounterStartDTO> findByAppointmentId(UUID appointmentId) {
        return jdbc.query("""
                select ce.id, ce.appointment_id, ce.status, ce.started_at, ce.legacy_start,
                       p.simulated_rne, ce.simulated_care_type, ce.simulated_service
                from clinical_encounters ce
                join appointments a on a.id = ce.appointment_id
                join professionals p on p.id = a.professional_id
                where ce.appointment_id = ?
                """, this::map, appointmentId).stream().findFirst();
    }

    private MedicalEncounterStartDTO map(ResultSet rs, int rowNum) throws SQLException {
        OffsetDateTime startedAt = rs.getObject("started_at", OffsetDateTime.class);
        return new MedicalEncounterStartDTO(
                rs.getObject("id", UUID.class), rs.getObject("appointment_id", UUID.class),
                rs.getString("status"), startedAt == null ? null : startedAt.toInstant(),
                rs.getBoolean("legacy_start"), rs.getString("simulated_rne"),
                rs.getString("simulated_care_type"), rs.getString("simulated_service"));
    }
}
