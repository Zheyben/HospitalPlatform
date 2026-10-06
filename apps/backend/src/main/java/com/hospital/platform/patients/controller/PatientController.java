package com.hospital.platform.patients.controller;

import com.hospital.platform.patients.dto.CreatePatientRequestDTO;
import com.hospital.platform.patients.dto.LinkUserRequestDTO;
import com.hospital.platform.patients.dto.PatientResponseDTO;
import com.hospital.platform.patients.dto.ReceptionPatientSearchDTO;
import com.hospital.platform.patients.dto.UpdatePatientRequestDTO;
import com.hospital.platform.patients.dto.UpdatePatientDemographicsRequestDTO;
import com.hospital.platform.patients.dto.UpdatePatientStatusRequestDTO;
import com.hospital.platform.patients.service.PatientService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody CreatePatientRequestDTO request) {
        PatientResponseDTO response = patientService.createPatient(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PatientResponseDTO> findPatients() {
        return patientService.findPatients();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<ReceptionPatientSearchDTO> findForReceptionByDocument(
            @RequestParam String documentType,
            @RequestParam String documentNumber
    ) {
        return ResponseEntity.of(patientService.findForReceptionByDocument(documentType, documentNumber));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientResponseDTO findCurrentPatient() {
        return patientService.findCurrentPatient();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PatientResponseDTO findPatientById(@PathVariable UUID id) {
        return patientService.findPatientById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PatientResponseDTO updatePatient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientRequestDTO request
    ) {
        return patientService.updatePatient(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public PatientResponseDTO updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientStatusRequestDTO request
    ) {
        return patientService.updateStatus(id, request);
    }

    @PatchMapping("/{id}/demographics")
    @PreAuthorize("hasRole('ADMIN')")
    public PatientResponseDTO updateDemographics(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientDemographicsRequestDTO request
    ) {
        return patientService.updateDemographics(id, request);
    }

    @PostMapping("/{id}/user")
    @PreAuthorize("hasRole('ADMIN')")
    public PatientResponseDTO linkUser(
            @PathVariable UUID id,
            @Valid @RequestBody LinkUserRequestDTO request
    ) {
        return patientService.linkUser(id, request);
    }
}
