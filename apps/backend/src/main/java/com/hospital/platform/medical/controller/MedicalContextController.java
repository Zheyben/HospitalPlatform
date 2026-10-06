package com.hospital.platform.medical.controller;

import com.hospital.platform.medical.dto.MedicalAppointmentContextDTO;
import com.hospital.platform.medical.dto.MedicalPriorEncounterDTO;
import com.hospital.platform.medical.dto.MedicalProfessionalContextDTO;
import com.hospital.platform.medical.dto.PatientClinicalPageDTO;
import com.hospital.platform.medical.service.MedicalContextService;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medical")
public class MedicalContextController {

    private final MedicalContextService service;

    public MedicalContextController(MedicalContextService service) {
        this.service = service;
    }

    @GetMapping("/me/context")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public MedicalProfessionalContextDTO ownContext() {
        return service.ownContext();
    }

    @GetMapping("/appointments/{id}/context")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public MedicalAppointmentContextDTO appointmentContext(@PathVariable UUID id) {
        return service.appointmentContext(id);
    }

    @GetMapping("/appointments/{id}/history")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public PatientClinicalPageDTO<MedicalPriorEncounterDTO> priorEncounters(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return service.priorEncounters(id, limit, offset);
    }
}
