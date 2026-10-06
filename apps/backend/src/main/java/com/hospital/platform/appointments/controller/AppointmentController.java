package com.hospital.platform.appointments.controller;

import com.hospital.platform.appointments.dto.AppointmentResponseDTO;
import com.hospital.platform.appointments.dto.CreateAppointmentRequestDTO;
import com.hospital.platform.appointments.dto.PatientAppointmentSummaryDTO;
import com.hospital.platform.appointments.dto.ReceptionAppointmentSummaryDTO;
import com.hospital.platform.appointments.dto.ReceptionWaitingRoomDTO;
import com.hospital.platform.appointments.dto.RescheduleAppointmentRequestDTO;
import com.hospital.platform.appointments.service.AppointmentService;
import com.hospital.platform.medical.service.MedicalEncounterService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final MedicalEncounterService medicalEncounterService;

    public AppointmentController(AppointmentService appointmentService, MedicalEncounterService medicalEncounterService) {
        this.appointmentService = appointmentService;
        this.medicalEncounterService = medicalEncounterService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDTO> createAppointment(
            @Valid @RequestBody CreateAppointmentRequestDTO request
    ) {
        AppointmentResponseDTO response = appointmentService.createAppointment(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public List<AppointmentResponseDTO> findAppointments() {
        return appointmentService.findAppointments();
    }

    @GetMapping("/reception")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public List<ReceptionAppointmentSummaryDTO> findReceptionAppointments(
            @RequestParam UUID patientId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return appointmentService.findReceptionAppointments(patientId, limit);
    }

    @GetMapping("/reception/waiting-room")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public List<ReceptionWaitingRoomDTO> findReceptionWaitingRoom(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return appointmentService.findReceptionWaitingRoom(limit, offset);
    }

    @GetMapping("/me/summary")
    @PreAuthorize("hasRole('PATIENT')")
    public List<PatientAppointmentSummaryDTO> findCurrentPatientSummaries() {
        return appointmentService.findCurrentPatientSummaries();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public AppointmentResponseDTO findAppointmentById(@PathVariable UUID id) {
        return appointmentService.findAppointmentById(id);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN', 'RECEPTIONIST')")
    public AppointmentResponseDTO confirmAppointment(@PathVariable UUID id) {
        return appointmentService.confirmAppointment(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public AppointmentResponseDTO cancelAppointment(@PathVariable UUID id) {
        return appointmentService.cancelAppointment(id);
    }

    @PostMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentRequestDTO request
    ) {
        AppointmentResponseDTO response = appointmentService.rescheduleAppointment(id, request.slotId());
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/appointments/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public AppointmentResponseDTO checkInAppointment(@PathVariable UUID id) {
        return appointmentService.checkInAppointment(id);
    }

    @PostMapping("/{id}/waiting")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public AppointmentResponseDTO moveAppointmentToWaiting(@PathVariable UUID id) {
        return appointmentService.moveAppointmentToWaiting(id);
    }

    @PostMapping("/{id}/start-attention")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public AppointmentResponseDTO startAppointmentAttention(@PathVariable UUID id) {
        return medicalEncounterService.start(id).appointment();
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public AppointmentResponseDTO completeAppointment(@PathVariable UUID id) {
        return appointmentService.completeAppointment(id);
    }
}
