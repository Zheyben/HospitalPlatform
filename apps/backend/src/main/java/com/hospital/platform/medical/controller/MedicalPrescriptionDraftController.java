package com.hospital.platform.medical.controller;

import com.hospital.platform.medical.dto.MedicalPrescriptionDraftResponseDTO;
import com.hospital.platform.medical.dto.MedicalPrescriptionDraftUpdateDTO;
import com.hospital.platform.medical.service.MedicalPrescriptionDraftService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medical/encounters")
public class MedicalPrescriptionDraftController {

    private final MedicalPrescriptionDraftService service;

    public MedicalPrescriptionDraftController(MedicalPrescriptionDraftService service) {
        this.service = service;
    }

    @GetMapping("/{id}/draft/prescription")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public MedicalPrescriptionDraftResponseDTO read(@PathVariable UUID id) {
        return service.read(id);
    }

    @PutMapping("/{id}/draft/prescription")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public MedicalPrescriptionDraftResponseDTO save(@PathVariable UUID id,
                                                    @Valid @RequestBody MedicalPrescriptionDraftUpdateDTO request) {
        return service.save(id, request);
    }
}
