package com.hospital.platform.medical.controller;

import com.hospital.platform.medical.dto.MedicalEncounterStartDTO;
import com.hospital.platform.medical.service.MedicalEncounterService;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medical/appointments")
public class MedicalEncounterController {

    private final MedicalEncounterService service;

    public MedicalEncounterController(MedicalEncounterService service) {
        this.service = service;
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public MedicalEncounterStartDTO start(@PathVariable UUID id) {
        return service.start(id).encounter();
    }
}
