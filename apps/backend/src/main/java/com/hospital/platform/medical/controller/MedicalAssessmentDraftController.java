package com.hospital.platform.medical.controller;

import com.hospital.platform.medical.dto.MedicalAssessmentDraftResponseDTO;
import com.hospital.platform.medical.dto.MedicalAssessmentDraftUpdateDTO;
import com.hospital.platform.medical.service.MedicalAssessmentDraftService;
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
public class MedicalAssessmentDraftController {

    private final MedicalAssessmentDraftService service;

    public MedicalAssessmentDraftController(MedicalAssessmentDraftService service) {
        this.service = service;
    }

    @GetMapping("/{id}/draft/assessment")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public MedicalAssessmentDraftResponseDTO read(@PathVariable UUID id) {
        return service.read(id);
    }

    @PutMapping("/{id}/draft/assessment")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public MedicalAssessmentDraftResponseDTO save(@PathVariable UUID id,
                                                  @Valid @RequestBody MedicalAssessmentDraftUpdateDTO request) {
        return service.save(id, request);
    }
}
