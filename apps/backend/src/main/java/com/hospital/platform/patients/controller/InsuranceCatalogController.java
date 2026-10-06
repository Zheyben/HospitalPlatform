package com.hospital.platform.patients.controller;

import com.hospital.platform.patients.dto.InsuranceOptionDTO;
import com.hospital.platform.patients.service.InsuranceCatalogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/insurance-providers")
public class InsuranceCatalogController {

    private final InsuranceCatalogService service;

    public InsuranceCatalogController(InsuranceCatalogService service) {
        this.service = service;
    }

    @GetMapping
    public List<InsuranceOptionDTO> activeOptions() {
        return service.activeOptions();
    }
}
