package com.hospital.platform.catalogs.controller;

import com.hospital.platform.catalogs.dto.Icd10OptionDTO;
import com.hospital.platform.catalogs.dto.MedicationOptionDTO;
import com.hospital.platform.catalogs.dto.MedicationPresentationDTO;
import com.hospital.platform.catalogs.dto.ProcedureOptionDTO;
import com.hospital.platform.catalogs.service.MedicalCatalogService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medical/catalogs")
public class MedicalCatalogController {

    private final MedicalCatalogService service;

    public MedicalCatalogController(MedicalCatalogService service) {
        this.service = service;
    }

    @GetMapping("/icd10")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public List<Icd10OptionDTO> searchIcd10(@RequestParam String q,
                                              @RequestParam(defaultValue = "20") int limit) {
        return service.searchIcd10(q, limit);
    }

    @GetMapping("/medications")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public List<MedicationOptionDTO> searchMedications(@RequestParam String q,
                                                        @RequestParam(defaultValue = "20") int limit) {
        return service.searchMedications(q, limit);
    }

    @GetMapping("/medications/{medicationId}/presentations")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public List<MedicationPresentationDTO> findPresentations(@PathVariable UUID medicationId,
                                                               @RequestParam(defaultValue = "50") int limit) {
        return service.findPresentations(medicationId, limit);
    }

    @GetMapping("/procedures")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public List<ProcedureOptionDTO> searchProcedures(@RequestParam String q,
                                                      @RequestParam(defaultValue = "20") int limit) {
        return service.searchProcedures(q, limit);
    }
}
