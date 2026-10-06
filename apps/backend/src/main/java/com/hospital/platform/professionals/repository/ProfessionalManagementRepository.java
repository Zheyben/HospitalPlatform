package com.hospital.platform.professionals.repository;

import com.hospital.platform.professionals.dto.ProfessionalResponseDTO;
import com.hospital.platform.professionals.dto.SpecialtyOptionDTO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProfessionalManagementRepository {

    private static final String PROFESSIONAL_VIEW = """
            select p.id, p.user_id, u.first_name, u.last_name, p.license_number,
                   ps.specialty_id, sp.name as specialty_name, p.deleted_at is null as active
            from professionals p
            left join users u on u.id = p.user_id
            left join professional_specialties ps on ps.professional_id = p.id
            left join specialties sp on sp.id = ps.specialty_id
            """;

    private final JdbcTemplate jdbc;

    public ProfessionalManagementRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isActiveSpecialty(UUID specialtyId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
                select exists(select 1 from specialties
                              where id = ? and active and deleted_at is null)
                """, Boolean.class, specialtyId));
    }

    public List<SpecialtyOptionDTO> activeSpecialties() {
        return jdbc.query("""
                select id, name from specialties
                where active and deleted_at is null
                order by name
                """, (rs, rowNum) -> new SpecialtyOptionDTO(
                rs.getObject("id", UUID.class), rs.getString("name")));
    }

    public boolean hasSchedules(UUID professionalId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "select exists(select 1 from schedules where professional_id = ?)", Boolean.class, professionalId));
    }

    public void assignSpecialty(UUID professionalId, UUID specialtyId) {
        jdbc.update("insert into professional_specialties(professional_id, specialty_id) values (?, ?)",
                professionalId, specialtyId);
    }

    public void replaceSpecialty(UUID professionalId, UUID specialtyId) {
        jdbc.update("delete from professional_specialties where professional_id = ?", professionalId);
        assignSpecialty(professionalId, specialtyId);
    }

    public Optional<UUID> assignedSpecialty(UUID professionalId) {
        return jdbc.query("select specialty_id from professional_specialties where professional_id = ?",
                (rs, rowNum) -> rs.getObject("specialty_id", UUID.class), professionalId).stream().findFirst();
    }

    public List<ProfessionalResponseDTO> findAll() {
        return jdbc.query(PROFESSIONAL_VIEW + " order by p.created_at desc", this::map);
    }

    public Optional<ProfessionalResponseDTO> findById(UUID professionalId) {
        return jdbc.query(PROFESSIONAL_VIEW + " where p.id = ?", this::map, professionalId)
                .stream().findFirst();
    }

    private ProfessionalResponseDTO map(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new ProfessionalResponseDTO(
                rs.getObject("id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("license_number"),
                rs.getObject("specialty_id", UUID.class),
                rs.getString("specialty_name"),
                rs.getBoolean("active")
        );
    }
}
