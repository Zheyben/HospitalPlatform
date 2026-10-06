package com.hospital.platform.catalogs.service;

import com.hospital.platform.catalogs.dto.Icd10OptionDTO;
import com.hospital.platform.catalogs.dto.MedicationOptionDTO;
import com.hospital.platform.catalogs.dto.MedicationPresentationDTO;
import com.hospital.platform.catalogs.dto.ProcedureOptionDTO;
import com.hospital.platform.catalogs.repository.MedicalCatalogRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicalCatalogService {

    private final MedicalCatalogRepository repository;

    public MedicalCatalogService(MedicalCatalogRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Icd10OptionDTO> searchIcd10(String query, int limit) {
        return repository.searchIcd10(normalizeQuery(query), validateLimit(limit));
    }

    @Transactional(readOnly = true)
    public List<MedicationOptionDTO> searchMedications(String query, int limit) {
        return repository.searchMedications(normalizeQuery(query), validateLimit(limit));
    }

    @Transactional(readOnly = true)
    public List<ProcedureOptionDTO> searchProcedures(String query, int limit) {
        return repository.searchProcedures(normalizeQuery(query), validateLimit(limit));
    }

    @Transactional(readOnly = true)
    public List<MedicationPresentationDTO> findPresentations(UUID medicationId, int limit) {
        return repository.findPresentations(medicationId, validateLimit(limit));
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            throw new InvalidMedicalCatalogQueryException();
        }
        String normalized = query.strip().toLowerCase(Locale.ROOT);
        if (normalized.length() < 2 || normalized.length() > 100) {
            throw new InvalidMedicalCatalogQueryException();
        }
        return normalized;
    }

    private int validateLimit(int limit) {
        if (limit < 1 || limit > 50) {
            throw new InvalidMedicalCatalogQueryException();
        }
        return limit;
    }
}
