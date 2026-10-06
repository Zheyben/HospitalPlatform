package com.hospital.platform.appointments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.appointments.entity.Appointment;
import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.exception.AppointmentSuccessorExistsException;
import com.hospital.platform.appointments.exception.InvalidAppointmentTransitionException;
import com.hospital.platform.appointments.mapper.AppointmentMapper;
import com.hospital.platform.appointments.repository.AppointmentRepository;
import com.hospital.platform.audit.contract.AuditEventType;
import com.hospital.platform.audit.contract.AuditLogService;
import com.hospital.platform.patients.contract.PatientLookupService;
import com.hospital.platform.patients.contract.PatientReference;
import com.hospital.platform.professionals.contract.ProfessionalLookupService;
import com.hospital.platform.users.service.CurrentUserService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AppointmentLifecycleServiceTest {
    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111151");
    private static final UUID NEW_ID = UUID.fromString("11111111-1111-1111-1111-111111111152");
    private static final UUID SLOT = UUID.fromString("22222222-2222-2222-2222-222222222261");
    private static final UUID NEW_SLOT = UUID.fromString("22222222-2222-2222-2222-222222222262");
    private static final UUID PROFESSIONAL = UUID.fromString("33333333-3333-3333-3333-333333333361");
    private static final UUID PATIENT = UUID.fromString("44444444-4444-4444-4444-444444444461");
    private static final UUID OTHER = UUID.fromString("44444444-4444-4444-4444-444444444462");
    private static final UUID USER = UUID.fromString("55555555-5555-5555-5555-555555555561");

    @Mock AppointmentRepository appointments;
    @Mock PatientLookupService patients;
    @Mock ProfessionalLookupService professionals;
    @Mock AuditLogService audit;
    @Mock CurrentUserService currentUser;
    @Mock CapacityGateway capacity;
    AppointmentService service;

    @BeforeEach void setUp() {
        service = new AppointmentService(appointments, patients, professionals, audit, currentUser,
                new AppointmentMapper(), capacity, Clock.systemUTC());
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void confirmsScheduledThroughGatewayAndAudits() {
        Appointment original = appointment(PATIENT);
        admin();
        when(appointments.findById(ID)).thenReturn(Optional.of(original));
        when(capacity.isFutureSlot(SLOT)).thenReturn(true);
        doAnswer(call -> { original.confirm(); return null; }).when(capacity).confirm(ID);
        assertThat(service.confirmAppointment(ID).appointmentStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(capacity).confirm(ID);
        verify(audit).record(org.mockito.ArgumentMatchers.eq(AuditEventType.APPOINTMENT_CONFIRMED),
                org.mockito.ArgumentMatchers.eq("Appointment"), org.mockito.ArgumentMatchers.eq(ID),
                org.mockito.ArgumentMatchers.anyMap(), org.mockito.ArgumentMatchers.anyMap());
    }

    @Test void pastAppointmentCannotBeConfirmed() {
        admin();
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment(PATIENT)));
        assertThatThrownBy(() -> service.confirmAppointment(ID))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
        verifyNoInteractions(audit);
    }

    @Test void cancellationUsesGatewayWithActorAndPreservesMetadata() {
        Appointment original = appointment(PATIENT);
        admin();
        when(appointments.findById(ID)).thenReturn(Optional.of(original));
        when(capacity.isFutureSlot(SLOT)).thenReturn(true);
        when(currentUser.currentUserId()).thenReturn(USER);
        doAnswer(call -> { original.cancel(LocalDateTime.of(2026, 10, 1, 8, 0), USER); return null; })
                .when(capacity).cancel(ID, USER);
        var result = service.cancelAppointment(ID);
        assertThat(result.appointmentStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(result.cancelledBy()).isEqualTo(USER);
        verify(capacity).cancel(ID, USER);
    }

    @Test void inProgressAppointmentCannotBeCancelledOrRescheduled() {
        Appointment original = appointment(PATIENT);
        original.confirm();
        original.checkIn();
        admin();
        when(appointments.findById(ID)).thenReturn(Optional.of(original));
        when(capacity.isFutureSlot(SLOT)).thenReturn(true);
        assertThatThrownBy(() -> service.cancelAppointment(ID))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
        assertThatThrownBy(() -> service.rescheduleAppointment(ID, NEW_SLOT))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
        verifyNoInteractions(audit);
    }

    @Test void pastAppointmentCannotBeCancelledOrRescheduled() {
        admin();
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment(PATIENT)));
        assertThatThrownBy(() -> service.cancelAppointment(ID))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
        assertThatThrownBy(() -> service.rescheduleAppointment(ID, NEW_SLOT))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
    }

    @Test void rescheduleDelegatesAtomicSwapAndReturnsSuccessor() {
        Appointment original = appointment(PATIENT);
        Appointment successor = new Appointment(NEW_ID, PATIENT, PROFESSIONAL, NEW_SLOT, "Control", ID);
        admin();
        when(appointments.findById(ID)).thenReturn(Optional.of(original));
        when(appointments.findById(NEW_ID)).thenReturn(Optional.of(successor));
        when(capacity.isFutureSlot(SLOT)).thenReturn(true);
        when(capacity.reschedule(ID, NEW_SLOT)).thenReturn(NEW_ID);
        assertThat(service.rescheduleAppointment(ID, NEW_SLOT).id()).isEqualTo(NEW_ID);
        verify(capacity).reschedule(ID, NEW_SLOT);
    }

    @Test void secondSuccessorRejectedBeforeGateway() {
        admin();
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment(PATIENT)));
        when(capacity.isFutureSlot(SLOT)).thenReturn(true);
        when(appointments.findByRescheduledFromId(ID)).thenReturn(Optional.of(
                new Appointment(NEW_ID, PATIENT, PROFESSIONAL, NEW_SLOT, "Control", ID)));
        assertThatThrownBy(() -> service.rescheduleAppointment(ID, NEW_SLOT))
                .isInstanceOf(AppointmentSuccessorExistsException.class);
    }

    @Test void patientCannotChangeAnotherPatientsAppointment() {
        auth("PATIENT");
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment(OTHER)));
        when(currentUser.currentUserId()).thenReturn(USER);
        when(patients.findActivePatientReferenceByUserId(USER))
                .thenReturn(Optional.of(new PatientReference(PATIENT, true)));
        assertThatThrownBy(() -> service.cancelAppointment(ID)).isInstanceOf(AccessDeniedException.class);
        verify(capacity).lockAppointment(ID);
        verifyNoMoreInteractions(capacity);
    }

    @Test void patientMayConfirmOwnAppointment() {
        Appointment original = appointment(PATIENT);
        auth("PATIENT");
        when(appointments.findById(ID)).thenReturn(Optional.of(original));
        when(currentUser.currentUserId()).thenReturn(USER);
        when(patients.findActivePatientReferenceByUserId(USER))
                .thenReturn(Optional.of(new PatientReference(PATIENT, true)));
        when(capacity.isFutureSlot(SLOT)).thenReturn(true);
        doAnswer(call -> { original.confirm(); return null; }).when(capacity).confirm(ID);
        assertThat(service.confirmAppointment(ID).appointmentStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    private Appointment appointment(UUID patient) {
        return new Appointment(ID, patient, PROFESSIONAL, SLOT, "Control");
    }
    private void admin() {
        auth("ADMIN");
        when(patients.existsActivePatient(PATIENT)).thenReturn(true);
    }
    private void auth(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "test", null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}
