package com.hospital.platform.medical.controller;

import com.hospital.platform.medical.dto.MedicalHistoryDraftResponseDTO;
import com.hospital.platform.medical.dto.MedicalHistoryDraftUpdateDTO;
import com.hospital.platform.medical.service.MedicalHistoryDraftService;
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
public class MedicalHistoryDraftController {

    private final MedicalHistoryDraftService service;

    public MedicalHistoryDraftController(MedicalHistoryDraftService service) {
        this.service = service;
    }

    @GetMapping("/{id}/draft/history")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public MedicalHistoryDraftResponseDTO read(@PathVariable UUID id) {
        return service.read(id);
    }

    @PutMapping("/{id}/draft/history")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public MedicalHistoryDraftResponseDTO save(@PathVariable UUID id,
                                               @Valid @RequestBody MedicalHistoryDraftUpdateDTO request) {
        return service.save(id, request);
    }
}
