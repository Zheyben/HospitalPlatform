package com.hospital.platform.catalogs.repository;

import com.hospital.platform.catalogs.dto.Icd10OptionDTO;
import com.hospital.platform.catalogs.dto.MedicationOptionDTO;
import com.hospital.platform.catalogs.dto.MedicationPresentationDTO;
import com.hospital.platform.catalogs.dto.ProcedureOptionDTO;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MedicalCatalogRepository {

    private final JdbcTemplate jdbc;

    public MedicalCatalogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Icd10OptionDTO> searchIcd10(String query, int limit) {
        return jdbc.query("""
                select id, code, description from icd10_codes
                where active and (strpos(lower(code), ?) > 0 or strpos(lower(description), ?) > 0)
                order by code, id limit ?
                """, (rs, rowNum) -> new Icd10OptionDTO(
                rs.getObject("id", UUID.class), rs.getString("code"), rs.getString("description")),
                query, query, limit);
    }

    public List<MedicationOptionDTO> searchMedications(String query, int limit) {
        return jdbc.query("""
                select id, generic_name, commercial_name from medications
                where active and (strpos(lower(generic_name), ?) > 0
                                  or strpos(lower(commercial_name), ?) > 0)
                order by generic_name, commercial_name nulls last, id limit ?
                """, (rs, rowNum) -> new MedicationOptionDTO(
                rs.getObject("id", UUID.class), rs.getString("generic_name"),
                rs.getString("commercial_name")), query, query, limit);
    }

    public List<MedicationPresentationDTO> findPresentations(UUID medicationId, int limit) {
        return jdbc.query("""
                select mp.id, mp.medication_id, mp.name, mp.concentration, mp.pharmaceutical_form
                from medication_presentations mp
                join medications m on m.id = mp.medication_id
                where m.id = ? and m.active and mp.active
                order by mp.name, mp.concentration, mp.id limit ?
                """, (rs, rowNum) -> new MedicationPresentationDTO(
                rs.getObject("id", UUID.class), rs.getObject("medication_id", UUID.class),
                rs.getString("name"), rs.getString("concentration"),
                rs.getString("pharmaceutical_form")), medicationId, limit);
    }

    public List<ProcedureOptionDTO> searchProcedures(String query, int limit) {
        return jdbc.query("""
                select id, code, name from procedures
                where active and (strpos(lower(code), ?) > 0 or strpos(lower(name), ?) > 0)
                order by name, code, id limit ?
                """, (rs, rowNum) -> new ProcedureOptionDTO(
                rs.getObject("id", UUID.class), rs.getString("code"), rs.getString("name")),
                query, query, limit);
    }
}
