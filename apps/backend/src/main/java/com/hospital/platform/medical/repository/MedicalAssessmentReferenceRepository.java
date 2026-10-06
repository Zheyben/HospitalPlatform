package com.hospital.platform.medical.repository;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MedicalAssessmentReferenceRepository {

    private final JdbcTemplate jdbc;

    public MedicalAssessmentReferenceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean activeIcd10(UUID id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "select exists(select 1 from icd10_codes where id = ? and active)", Boolean.class, id));
    }

    public boolean activeProcedure(UUID id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "select exists(select 1 from procedures where id = ? and active)", Boolean.class, id));
    }

    public boolean activeSpecialty(UUID id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "select exists(select 1 from specialties where id = ? and active and deleted_at is null)",
                Boolean.class, id));
    }
}
