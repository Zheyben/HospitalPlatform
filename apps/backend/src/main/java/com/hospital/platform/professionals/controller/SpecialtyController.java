package com.hospital.platform.professionals.controller;

import com.hospital.platform.professionals.dto.SpecialtyOptionDTO;
import com.hospital.platform.professionals.repository.ProfessionalManagementRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/specialties")
public class SpecialtyController {
    private final ProfessionalManagementRepository management;

    public SpecialtyController(ProfessionalManagementRepository management) {
        this.management = management;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SpecialtyOptionDTO> activeSpecialties() {
        return management.activeSpecialties();
    }
}
