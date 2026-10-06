package com.hospital.platform.patients.service;

import com.hospital.platform.patients.dto.InsuranceOptionDTO;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InsuranceCatalogService {

    private final JdbcTemplate jdbc;

    public InsuranceCatalogService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<InsuranceOptionDTO> activeOptions() {
        return jdbc.query("select id, code, display_name from insurance_providers where active order by code",
                (rs, row) -> new InsuranceOptionDTO(rs.getObject("id", UUID.class),
                        rs.getString("code"), rs.getString("display_name")));
    }

    @Transactional(readOnly = true)
    public InsuranceOptionDTO resolve(UUID id, String legacyName) {
        String normalized = legacyName == null ? null : legacyName.strip().toUpperCase(Locale.ROOT);
        if (normalized != null && normalized.isEmpty()) {
            normalized = null;
        }
        if (id == null && (normalized == null || normalized.isEmpty())) {
            throw new InvalidInsuranceException();
        }
        List<InsuranceOptionDTO> matches = jdbc.query("""
                select id, code, display_name from insurance_providers
                where active and (?::uuid is null or id = ?::uuid)
                  and (?::text is null or code = ?::text)
                """, (rs, row) -> new InsuranceOptionDTO(rs.getObject("id", UUID.class),
                rs.getString("code"), rs.getString("display_name")), id, id, normalized, normalized);
        if (matches.size() != 1) {
            throw new InvalidInsuranceException();
        }
        return matches.get(0);
    }
}
