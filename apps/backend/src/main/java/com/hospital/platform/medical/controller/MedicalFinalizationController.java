package com.hospital.platform.medical.controller;

import com.hospital.platform.medical.dto.MedicalFinalizationRequestDTO;
import com.hospital.platform.medical.dto.MedicalFinalizationResponseDTO;
import com.hospital.platform.medical.service.MedicalFinalizationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medical/encounters")
public class MedicalFinalizationController {

    private final MedicalFinalizationService service;

    public MedicalFinalizationController(MedicalFinalizationService service) {
        this.service = service;
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public MedicalFinalizationResponseDTO finalizeEncounter(@PathVariable UUID id,
                                                             @Valid @RequestBody MedicalFinalizationRequestDTO request) {
        return service.finalizeEncounter(id, request.version());
    }
}
