package com.hospital.platform.appointments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.appointments.dto.CreateAppointmentRequestDTO;
import com.hospital.platform.appointments.entity.Appointment;
import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.exception.AppointmentNotFoundException;
import com.hospital.platform.appointments.exception.InvalidAppointmentRequestException;
import com.hospital.platform.appointments.exception.SlotUnavailableException;
import com.hospital.platform.appointments.mapper.AppointmentMapper;
import com.hospital.platform.appointments.repository.AppointmentRepository;
import com.hospital.platform.appointments.repository.ReceptionWaitingRoomRow;
import com.hospital.platform.audit.contract.AuditLogService;
import com.hospital.platform.patients.contract.PatientLookupService;
import com.hospital.platform.patients.contract.PatientReference;
import com.hospital.platform.professionals.contract.ProfessionalLookupService;
import com.hospital.platform.users.service.CurrentUserService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    private static final UUID APPOINTMENT = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SLOT = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PROFESSIONAL = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID PATIENT = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID USER = UUID.fromString("55555555-5555-5555-5555-555555555555");

    @Mock AppointmentRepository appointments;
    @Mock PatientLookupService patients;
    @Mock ProfessionalLookupService professionals;
    @Mock AuditLogService audit;
    @Mock CurrentUserService currentUser;
    @Mock CapacityGateway capacity;
    AppointmentService service;

    @BeforeEach void setUp() {
        service = new AppointmentService(appointments, patients, professionals, audit, currentUser,
                new AppointmentMapper(), capacity,
                Clock.fixed(Instant.parse("2026-10-06T04:59:59Z"), ZoneOffset.UTC));
    }

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void adminCreatesScheduledAppointmentThroughGateway() {
        auth("ADMIN");
        when(patients.existsActivePatient(PATIENT)).thenReturn(true);
        when(capacity.reserve(SLOT, PATIENT, "Control")).thenReturn(APPOINTMENT);
        when(appointments.findById(APPOINTMENT)).thenReturn(Optional.of(appointment()));
        var response = service.createAppointment(new CreateAppointmentRequestDTO(SLOT, PATIENT, "Control"));
        assertThat(response.appointmentStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(capacity).reserve(SLOT, PATIENT, "Control");
    }

    @Test void patientUsesOwnProfileWithoutPassingPatientId() {
        auth("PATIENT");
        when(currentUser.currentUserId()).thenReturn(USER);
        when(patients.findActivePatientReferenceByUserId(USER))
                .thenReturn(Optional.of(new PatientReference(PATIENT, true)));
        when(capacity.reserve(SLOT, PATIENT, null)).thenReturn(APPOINTMENT);
        when(appointments.findById(APPOINTMENT)).thenReturn(Optional.of(appointment()));
        assertThat(service.createAppointment(new CreateAppointmentRequestDTO(SLOT, null, null)).patientId())
                .isEqualTo(PATIENT);
        verify(capacity).reserve(SLOT, PATIENT, null);
    }

    @Test void patientCannotForgePatientId() {
        auth("PATIENT");
        assertThatThrownBy(() -> service.createAppointment(new CreateAppointmentRequestDTO(SLOT, PATIENT, null)))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(capacity);
    }

    @Test void slotConflictReturnsExistingContract() {
        auth("ADMIN");
        when(patients.existsActivePatient(PATIENT)).thenReturn(true);
        when(capacity.reserve(SLOT, PATIENT, null))
                .thenThrow(new DataIntegrityViolationException("SLOT_UNAVAILABLE"));
        assertThatThrownBy(() -> service.createAppointment(new CreateAppointmentRequestDTO(SLOT, PATIENT, null)))
                .isInstanceOf(SlotUnavailableException.class);
    }

    @Test void missingAppointmentReturnsNotFound() {
        auth("ADMIN");
        assertThatThrownBy(() -> service.findAppointmentById(APPOINTMENT))
                .isInstanceOf(AppointmentNotFoundException.class);
    }

    @Test void patientListsOnlyOwnAppointments() {
        auth("PATIENT");
        when(currentUser.currentUserId()).thenReturn(USER);
        when(patients.findActivePatientReferenceByUserId(USER))
                .thenReturn(Optional.of(new PatientReference(PATIENT, true)));
        when(appointments.findAllByPatientIdOrderByCreatedAtDesc(PATIENT)).thenReturn(List.of(appointment()));
        assertThat(service.findAppointments()).hasSize(1);
        verify(appointments).findAllByPatientIdOrderByCreatedAtDesc(PATIENT);
    }

    @Test void receptionistCannotReadGlobalOrDetailedAppointment() {
        auth("RECEPTIONIST");

        assertThatThrownBy(() -> service.findAppointments()).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.findAppointmentById(APPOINTMENT))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(appointments);
    }

    @Test void receptionistQueryRequiresPatientAndBoundedLimit() {
        auth("RECEPTIONIST");

        assertThatThrownBy(() -> service.findReceptionAppointments(null, 50))
                .isInstanceOf(InvalidAppointmentRequestException.class);
        assertThatThrownBy(() -> service.findReceptionAppointments(PATIENT, 0))
                .isInstanceOf(InvalidAppointmentRequestException.class);
        assertThatThrownBy(() -> service.findReceptionAppointments(PATIENT, 101))
                .isInstanceOf(InvalidAppointmentRequestException.class);
        assertThat(service.findReceptionAppointments(PATIENT, 100)).isEmpty();
        verify(appointments).findReceptionSummaries(PATIENT, 100);
    }

    @Test void patientRoleDoesNotGrantReceptionQuery() {
        auth("PATIENT");

        assertThatThrownBy(() -> service.findReceptionAppointments(PATIENT, 50))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(appointments);
    }

    @Test void waitingRoomUsesLimaDateAndMinimalProjection() {
        auth("RECEPTIONIST");
        ReceptionWaitingRoomRow row = mock(ReceptionWaitingRoomRow.class);
        when(row.getAppointmentId()).thenReturn(APPOINTMENT);
        when(row.getPatientDisplay()).thenReturn("Paciente Ejemplo");
        when(row.getStartTime()).thenReturn(LocalTime.of(8, 30));
        when(row.getProfessionalName()).thenReturn("Profesional Ejemplo");
        when(row.getSpecialtyName()).thenReturn("Medicina General");
        when(row.getFlowStage()).thenReturn("WAITING");
        when(appointments.findReceptionWaitingRoom(LocalDate.of(2026, 10, 5), 20, 40))
                .thenReturn(List.of(row));

        var result = service.findReceptionWaitingRoom(20, 40);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().appointmentId()).isEqualTo(APPOINTMENT);
        assertThat(result.getFirst().patientDisplay()).isEqualTo("Paciente Ejemplo");
        assertThat(result.getFirst().flowStage().name()).isEqualTo("WAITING");
        verify(appointments).findReceptionWaitingRoom(LocalDate.of(2026, 10, 5), 20, 40);
    }

    @Test void waitingRoomRejectsUnboundedPagesAndOtherRoles() {
        auth("RECEPTIONIST");
        assertThatThrownBy(() -> service.findReceptionWaitingRoom(0, 0))
                .isInstanceOf(InvalidAppointmentRequestException.class);
        assertThatThrownBy(() -> service.findReceptionWaitingRoom(101, 0))
                .isInstanceOf(InvalidAppointmentRequestException.class);
        assertThatThrownBy(() -> service.findReceptionWaitingRoom(50, -1))
                .isInstanceOf(InvalidAppointmentRequestException.class);
        auth("PATIENT");
        assertThatThrownBy(() -> service.findReceptionWaitingRoom(50, 0))
                .isInstanceOf(AccessDeniedException.class);
        auth("ADMIN");
        assertThatThrownBy(() -> service.findReceptionWaitingRoom(50, 0))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(appointments);
    }

    @Test void receptionistWithPatientRoleStillListsOnlyOwnAppointments() {
        auth("RECEPTIONIST", "PATIENT");
        when(currentUser.currentUserId()).thenReturn(USER);
        when(patients.findActivePatientReferenceByUserId(USER))
                .thenReturn(Optional.of(new PatientReference(PATIENT, true)));
        when(appointments.findAllByPatientIdOrderByCreatedAtDesc(PATIENT)).thenReturn(List.of(appointment()));

        assertThat(service.findAppointments()).hasSize(1);
        verify(appointments).findAllByPatientIdOrderByCreatedAtDesc(PATIENT);
    }

    @Test void multiRolePatientCanReadOnlyOwnAppointmentDetail() {
        auth("RECEPTIONIST", "PATIENT");
        when(appointments.findById(APPOINTMENT)).thenReturn(Optional.of(appointment()));
        when(currentUser.currentUserId()).thenReturn(USER);
        when(patients.findActivePatientReferenceByUserId(USER))
                .thenReturn(Optional.of(new PatientReference(PATIENT, true)));

        assertThat(service.findAppointmentById(APPOINTMENT).id()).isEqualTo(APPOINTMENT);

        UUID anotherPatient = UUID.fromString("44444444-4444-4444-4444-444444444445");
        when(patients.findActivePatientReferenceByUserId(USER))
                .thenReturn(Optional.of(new PatientReference(anotherPatient, true)));
        assertThatThrownBy(() -> service.findAppointmentById(APPOINTMENT))
                .isInstanceOf(AccessDeniedException.class);
    }

    private Appointment appointment() { return new Appointment(APPOINTMENT, PATIENT, PROFESSIONAL, SLOT, "Control"); }

    private void auth(String... roles) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "test", null, java.util.Arrays.stream(roles)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList()));
    }
}
