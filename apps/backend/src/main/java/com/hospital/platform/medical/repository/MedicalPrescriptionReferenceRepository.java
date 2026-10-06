package com.hospital.platform.medical.repository;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MedicalPrescriptionReferenceRepository {

    private final JdbcTemplate jdbc;

    public MedicalPrescriptionReferenceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean activePresentationForMedication(UUID medicationId, UUID presentationId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
                select exists (
                    select 1 from medication_presentations p
                    join medications m on m.id = p.medication_id
                    where m.id = ? and p.id = ? and m.active and p.active
                )
                """, Boolean.class, medicationId, presentationId));
    }
}
