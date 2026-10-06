package com.hospital.platform.medical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.hospital.platform.appointments.dto.AppointmentResponseDTO;
import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.entity.FlowStage;
import com.hospital.platform.appointments.exception.InvalidAppointmentTransitionException;
import com.hospital.platform.appointments.repository.AppointmentRepository;
import com.hospital.platform.appointments.service.AppointmentService;
import com.hospital.platform.audit.contract.AuditEventType;
import com.hospital.platform.audit.contract.AuditLogService;
import com.hospital.platform.medical.dto.MedicalEncounterStartDTO;
import com.hospital.platform.medical.repository.MedicalEncounterRepository;
import com.hospital.platform.medical.service.MedicalEncounterService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;

class MedicalEncounterServiceTest {

    private static final UUID APPOINTMENT = UUID.randomUUID();
    private static final UUID ENCOUNTER = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-10-05T12:00:00Z");
    private final AppointmentService appointments = Mockito.mock(AppointmentService.class);
    private final AppointmentRepository appointmentRepository = Mockito.mock(AppointmentRepository.class);
    private final MedicalEncounterRepository encounters = Mockito.mock(MedicalEncounterRepository.class);
    private final AuditLogService audit = Mockito.mock(AuditLogService.class);
    private final MedicalEncounterService service = new MedicalEncounterService(
            appointments, appointmentRepository, encounters, audit, Clock.fixed(NOW, ZoneId.of("UTC")));

    @Test
    void createsOneOpenEncounterAndAuditsOnlyIdentifiers() {
        readyAppointment();
        when(encounters.insertOpen(any(UUID.class), eq(APPOINTMENT), eq(NOW))).thenReturn(1);
        when(encounters.findByAppointmentId(APPOINTMENT))
                .thenReturn(Optional.empty(), Optional.of(encounter()));

        MedicalEncounterService.StartResult result = service.start(APPOINTMENT);

        assertThat(result.encounter().encounterId()).isEqualTo(ENCOUNTER);
        assertThat(result.appointment().flowStage()).isEqualTo(FlowStage.IN_ATTENTION);
        verify(audit).record(eq(AuditEventType.CLINICAL_ENCOUNTER_STARTED), eq("ClinicalEncounter"),
                eq(ENCOUNTER), eq(java.util.Map.of()),
                eq(java.util.Map.of("appointmentId", APPOINTMENT.toString())));
    }

    @Test
    void reusesExistingEncounterWithoutDuplicateAudit() {
        when(appointments.startAppointmentAttention(APPOINTMENT)).thenReturn(appointment());
        when(encounters.findByAppointmentId(APPOINTMENT)).thenReturn(Optional.of(encounter()));

        assertThat(service.start(APPOINTMENT).encounter().encounterId()).isEqualTo(ENCOUNTER);
        verify(encounters).findByAppointmentId(APPOINTMENT);
        verifyNoMoreInteractions(encounters);
        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(audit);
    }

    @Test
    void reusesStartedEncounterAfterItsAppointmentDate() {
        when(appointments.startAppointmentAttention(APPOINTMENT)).thenReturn(appointment());
        when(encounters.findByAppointmentId(APPOINTMENT)).thenReturn(Optional.of(encounter()));

        assertThat(service.start(APPOINTMENT).encounter().encounterId()).isEqualTo(ENCOUNTER);
        verifyNoInteractions(appointmentRepository, audit);
    }

    @Test
    void rejectsDifferentLimaDateBeforeWritingEncounter() {
        when(appointments.startAppointmentAttention(APPOINTMENT)).thenReturn(appointment());
        when(appointmentRepository.findSlotDateByAppointmentId(APPOINTMENT))
                .thenReturn(LocalDate.of(2026, 10, 6));

        assertThatThrownBy(() -> service.start(APPOINTMENT))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
        verify(encounters).findByAppointmentId(APPOINTMENT);
        verifyNoMoreInteractions(encounters);
        verifyNoInteractions(audit);
    }

    @Test
    void refusesUnassignedProfessionalWithoutReadingEncounter() {
        when(appointments.startAppointmentAttention(APPOINTMENT))
                .thenThrow(new AccessDeniedException("Not assigned"));

        assertThatThrownBy(() -> service.start(APPOINTMENT)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(appointmentRepository, encounters, audit);
    }

    private void readyAppointment() {
        when(appointments.startAppointmentAttention(APPOINTMENT)).thenReturn(appointment());
        when(appointmentRepository.findSlotDateByAppointmentId(APPOINTMENT))
                .thenReturn(LocalDate.of(2026, 10, 5));
    }

    private AppointmentResponseDTO appointment() {
        return new AppointmentResponseDTO(APPOINTMENT, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                AppointmentStatus.CONFIRMED, FlowStage.IN_ATTENTION, null, null, null, null, null);
    }

    private MedicalEncounterStartDTO encounter() {
        return new MedicalEncounterStartDTO(ENCOUNTER, APPOINTMENT, "OPEN", NOW, false,
                "SIM-RNE-000000000001", "Consulta externa (dato simulado)",
                "Servicio ambulatorio (dato simulado)");
    }
}
