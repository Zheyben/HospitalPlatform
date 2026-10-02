package com.hospital.platform.appointments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.appointments.dto.CreateAppointmentRequestDTO;
import com.hospital.platform.appointments.entity.Appointment;
import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.exception.AppointmentNotFoundException;
import com.hospital.platform.appointments.exception.SlotUnavailableException;
import com.hospital.platform.appointments.mapper.AppointmentMapper;
import com.hospital.platform.appointments.repository.AppointmentRepository;
import com.hospital.platform.audit.contract.AuditLogService;
import com.hospital.platform.patients.contract.PatientLookupService;
import com.hospital.platform.patients.contract.PatientReference;
import com.hospital.platform.professionals.contract.ProfessionalLookupService;
import com.hospital.platform.users.service.CurrentUserService;
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
                new AppointmentMapper(), capacity);
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

    private Appointment appointment() { return new Appointment(APPOINTMENT, PATIENT, PROFESSIONAL, SLOT, "Control"); }

    private void auth(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "test", null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}
