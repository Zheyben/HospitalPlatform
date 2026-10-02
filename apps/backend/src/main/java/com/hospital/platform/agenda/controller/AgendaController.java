package com.hospital.platform.agenda.controller;

import com.hospital.platform.agenda.dto.AgendaResponseDTO;
import com.hospital.platform.agenda.dto.AvailabilitySlotResponseDTO;
import com.hospital.platform.agenda.dto.CreateAgendaRequestDTO;
import com.hospital.platform.agenda.dto.UpdateAgendaRequestDTO;
import com.hospital.platform.agenda.dto.UpdateAgendaStatusRequestDTO;
import com.hospital.platform.agenda.entity.AvailabilitySlotStatus;
import com.hospital.platform.agenda.service.AgendaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class AgendaController {

    private final AgendaService agendaService;

    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @PostMapping("/agendas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgendaResponseDTO> createAgenda(
            @Valid @RequestBody CreateAgendaRequestDTO request
    ) {
        AgendaResponseDTO response = agendaService.createAgenda(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/agendas")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AgendaResponseDTO> findAgendas(
            @RequestParam(required = false) UUID professionalId,
            @RequestParam(required = false) UUID specialtyId
    ) {
        return agendaService.findAgendas(professionalId, specialtyId);
    }

    @GetMapping("/agendas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AgendaResponseDTO findAgendaById(@PathVariable UUID id) {
        return agendaService.findAgendaById(id);
    }

    @PutMapping("/agendas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AgendaResponseDTO updateAgenda(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAgendaRequestDTO request
    ) {
        return agendaService.updateAgenda(id, request);
    }

    @PatchMapping("/agendas/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public AgendaResponseDTO changeAgendaStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAgendaStatusRequestDTO request
    ) {
        return agendaService.changeAgendaStatus(id, request.active());
    }

    @GetMapping("/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public List<AvailabilitySlotResponseDTO> findAvailability(
            @RequestParam(required = false) UUID scheduleId,
            @RequestParam(required = false) UUID professionalId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate slotDate,
            @RequestParam(required = false) AvailabilitySlotStatus status
    ) {
        return agendaService.findAvailability(scheduleId, professionalId, slotDate, status);
    }

    @GetMapping("/availability/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AvailabilitySlotResponseDTO findAvailabilitySlotById(@PathVariable UUID id) {
        return agendaService.findAvailabilitySlotById(id);
    }
}
