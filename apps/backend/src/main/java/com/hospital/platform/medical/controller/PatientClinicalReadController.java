package com.hospital.platform.medical.controller;

import com.hospital.platform.medical.dto.PatientClinicalPageDTO;
import com.hospital.platform.medical.dto.PatientEncounterDetailDTO;
import com.hospital.platform.medical.dto.PatientEncounterSummaryDTO;
import com.hospital.platform.medical.dto.PatientPrescriptionDetailDTO;
import com.hospital.platform.medical.dto.PatientPrescriptionSummaryDTO;
import com.hospital.platform.medical.service.PatientClinicalReadService;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medical/me")
public class PatientClinicalReadController {

    private final PatientClinicalReadService service;

    public PatientClinicalReadController(PatientClinicalReadService service) {
        this.service = service;
    }

    @GetMapping("/encounters")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientClinicalPageDTO<PatientEncounterSummaryDTO> encounters(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return service.encounters(limit, offset);
    }

    @GetMapping("/encounters/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientEncounterDetailDTO encounter(@PathVariable UUID id) {
        return service.encounter(id);
    }

    @GetMapping("/prescriptions")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientClinicalPageDTO<PatientPrescriptionSummaryDTO> prescriptions(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return service.prescriptions(limit, offset);
    }

    @GetMapping("/prescriptions/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientPrescriptionDetailDTO prescription(@PathVariable UUID id) {
        return service.prescription(id);
    }
}
