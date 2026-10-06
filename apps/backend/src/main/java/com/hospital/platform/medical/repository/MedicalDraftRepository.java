package com.hospital.platform.medical.repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MedicalDraftRepository {

    private final JdbcTemplate jdbc;

    public MedicalDraftRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<DraftRow> findEditable(UUID encounterId, UUID professionalId, MedicalDraftSection section) {
        return jdbc.query("""
                select ce.appointment_id, d.version, d.content -> cast(? as text) as section_data, d.updated_at
                from clinical_encounters ce
                join appointments a on a.id = ce.appointment_id
                left join encounter_drafts d on d.encounter_id = ce.id
                where ce.id = ? and a.professional_id = ? and ce.status = 'OPEN'
                  and a.appointment_status = 'CONFIRMED' and a.flow_stage = 'IN_ATTENTION'
                """, (rs, rowNum) -> {
            OffsetDateTime updatedAt = rs.getObject("updated_at", OffsetDateTime.class);
            return new DraftRow(rs.getObject("appointment_id", UUID.class),
                    rs.getObject("version", Integer.class), rs.getString("section_data"),
                    updatedAt == null ? null : updatedAt.toInstant());
        }, section.key(), encounterId, professionalId).stream().findFirst();
    }

    public int insertSection(UUID encounterId, MedicalDraftSection section, String contentJson, Instant updatedAt) {
        return jdbc.update("""
                insert into encounter_drafts (encounter_id, version, content, updated_at)
                values (?, 1, jsonb_build_object(?, cast(? as jsonb)), ?)
                on conflict (encounter_id) do nothing
                """, encounterId, section.key(), contentJson, Timestamp.from(updatedAt));
    }

    public int updateSection(UUID encounterId, int expectedVersion, MedicalDraftSection section,
                             String contentJson, Instant updatedAt) {
        return jdbc.update("""
                update encounter_drafts
                set content = jsonb_set(content, array[cast(? as text)], cast(? as jsonb), true),
                    version = version + 1, updated_at = ?
                where encounter_id = ? and version = ?
                """, section.key(), contentJson, Timestamp.from(updatedAt), encounterId, expectedVersion);
    }

    public record DraftRow(UUID appointmentId, Integer version, String sectionJson, Instant updatedAt) {
    }
}
