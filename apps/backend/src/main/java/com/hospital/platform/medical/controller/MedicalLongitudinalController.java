package com.hospital.platform.medical.controller;

import com.hospital.platform.medical.dto.MedicalPriorEncounterDetailDTO;
import com.hospital.platform.medical.service.MedicalLongitudinalService;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medical/appointments")
public class MedicalLongitudinalController {

    private final MedicalLongitudinalService service;

    public MedicalLongitudinalController(MedicalLongitudinalService service) {
        this.service = service;
    }

    @GetMapping("/{appointmentId}/history/{encounterId}")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public MedicalPriorEncounterDetailDTO detail(@PathVariable UUID appointmentId, @PathVariable UUID encounterId) {
        return service.detail(appointmentId, encounterId);
    }
}
