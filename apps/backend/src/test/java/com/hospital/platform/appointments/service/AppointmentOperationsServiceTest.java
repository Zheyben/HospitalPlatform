package com.hospital.platform.appointments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.appointments.entity.Appointment;
import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.entity.FlowStage;
import com.hospital.platform.appointments.exception.InvalidAppointmentTransitionException;
import com.hospital.platform.appointments.mapper.AppointmentMapper;
import com.hospital.platform.appointments.repository.AppointmentRepository;
import com.hospital.platform.audit.contract.AuditEventType;
import com.hospital.platform.audit.contract.AuditLogService;
import com.hospital.platform.patients.contract.PatientLookupService;
import com.hospital.platform.professionals.contract.ProfessionalLookupService;
import com.hospital.platform.users.service.CurrentUserService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AppointmentOperationsServiceTest {
    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111171");
    private static final UUID PATIENT = UUID.fromString("22222222-2222-2222-2222-222222222271");
    private static final UUID PROFESSIONAL = UUID.fromString("33333333-3333-3333-3333-333333333371");
    private static final UUID SLOT = UUID.fromString("44444444-4444-4444-4444-444444444471");
    private static final UUID USER = UUID.fromString("55555555-5555-5555-5555-555555555571");

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

    @Test void receptionChecksInThroughGateway() {
        Appointment appointment = appointment();
        auth("RECEPTIONIST");
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment));
        when(appointments.findSlotDateByAppointmentId(ID)).thenReturn(LocalDate.of(2026, 10, 5));
        doAnswer(call -> { appointment.checkIn(); return null; }).when(capacity).stage(ID, "CHECK_IN");
        assertThat(service.checkInAppointment(ID).flowStage()).isEqualTo(FlowStage.CHECK_IN);
        verify(capacity).stage(ID, "CHECK_IN");
        verify(audit).record(org.mockito.ArgumentMatchers.eq(AuditEventType.APPOINTMENT_CHECKED_IN),
                org.mockito.ArgumentMatchers.eq("Appointment"), org.mockito.ArgumentMatchers.eq(ID),
                org.mockito.ArgumentMatchers.anyMap(), org.mockito.ArgumentMatchers.anyMap());
    }

    @Test void rejectsEarlyAndLateCheckInWithoutChangingStageOrAudit() {
        Appointment appointment = appointment();
        auth("RECEPTIONIST");
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment));
        when(appointments.findSlotDateByAppointmentId(ID))
                .thenReturn(LocalDate.of(2026, 10, 4), LocalDate.of(2026, 10, 6));

        assertThatThrownBy(() -> service.checkInAppointment(ID))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
        assertThatThrownBy(() -> service.checkInAppointment(ID))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
        assertThat(appointment.getFlowStage()).isNull();
        verify(capacity, org.mockito.Mockito.times(2)).lockAppointment(ID);
        verifyNoMoreInteractions(capacity);
        org.mockito.Mockito.verifyNoInteractions(audit);
    }

    @Test void limaMidnightRejectsPreviousDateEvenWhenUtcDateHasNotChanged() {
        auth("RECEPTIONIST");
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment()));
        when(appointments.findSlotDateByAppointmentId(ID)).thenReturn(LocalDate.of(2026, 10, 5));
        service = new AppointmentService(appointments, patients, professionals, audit, currentUser,
                new AppointmentMapper(), capacity,
                Clock.fixed(Instant.parse("2026-10-06T05:00:00Z"), ZoneOffset.UTC));

        assertThatThrownBy(() -> service.checkInAppointment(ID))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
        verify(capacity).lockAppointment(ID);
        verifyNoMoreInteractions(capacity);
    }

    @Test void repeatedCheckInKeepsItsOriginalResultWithoutNewAudit() {
        Appointment appointment = appointment();
        appointment.checkIn();
        auth("RECEPTIONIST");
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment));

        assertThat(service.checkInAppointment(ID).flowStage()).isEqualTo(FlowStage.CHECK_IN);
        verify(capacity).lockAppointment(ID);
        verifyNoMoreInteractions(capacity);
        org.mockito.Mockito.verifyNoInteractions(audit);
    }

    @Test void waitingRequiresCheckIn() {
        auth("RECEPTIONIST");
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment()));
        assertThatThrownBy(() -> service.moveAppointmentToWaiting(ID))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
        verify(capacity).lockAppointment(ID);
        verifyNoMoreInteractions(capacity);
    }

    @Test void receptionMovesCheckedInPatientToWaiting() {
        Appointment appointment = appointment();
        appointment.checkIn();
        auth("RECEPTIONIST");
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment));
        doAnswer(call -> { appointment.moveToWaiting(); return null; }).when(capacity).stage(ID, "WAITING");
        assertThat(service.moveAppointmentToWaiting(ID).flowStage()).isEqualTo(FlowStage.WAITING);
    }

    @Test void professionalStartsOwnAttention() {
        Appointment appointment = appointment();
        appointment.checkIn();
        appointment.moveToWaiting();
        auth("PROFESSIONAL");
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment));
        when(currentUser.currentUserId()).thenReturn(USER);
        when(professionals.isActiveProfessionalLinkedToUser(PROFESSIONAL, USER)).thenReturn(true);
        doAnswer(call -> { appointment.startAttention(); return null; }).when(capacity).stage(ID, "IN_ATTENTION");
        assertThat(service.startAppointmentAttention(ID).flowStage()).isEqualTo(FlowStage.IN_ATTENTION);
        verify(capacity).stage(ID, "IN_ATTENTION");
    }

    @Test void otherProfessionalCannotStartAttention() {
        Appointment appointment = appointment();
        appointment.checkIn();
        appointment.moveToWaiting();
        auth("PROFESSIONAL");
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment));
        when(currentUser.currentUserId()).thenReturn(USER);
        assertThatThrownBy(() -> service.startAppointmentAttention(ID))
                .isInstanceOf(AccessDeniedException.class);
        verify(capacity).lockAppointment(ID);
        verifyNoMoreInteractions(capacity);
    }

    @Test void onlyMedicalFinalizationCompletesWithoutReleasingSlot() {
        Appointment appointment = appointment();
        appointment.checkIn();
        appointment.moveToWaiting();
        appointment.startAttention();
        auth("PROFESSIONAL");
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment));
        when(currentUser.currentUserId()).thenReturn(USER);
        when(professionals.isActiveProfessionalLinkedToUser(PROFESSIONAL, USER)).thenReturn(true);
        doAnswer(call -> { appointment.complete(); return null; }).when(capacity).complete(ID);
        assertThatThrownBy(() -> service.completeAppointment(ID))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
        var result = service.completeMedicalAppointment(ID);
        assertThat(result.appointmentStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(result.flowStage()).isEqualTo(FlowStage.FINISHED);
        verify(capacity).complete(ID);
    }

    @Test void adminCannotOperateReceptionFlow() {
        auth("ADMIN");
        when(appointments.findById(ID)).thenReturn(Optional.of(appointment()));
        assertThatThrownBy(() -> service.checkInAppointment(ID)).isInstanceOf(AccessDeniedException.class);
        verify(capacity).lockAppointment(ID);
        verifyNoMoreInteractions(capacity);
    }

    private Appointment appointment() {
        Appointment appointment = new Appointment(ID, PATIENT, PROFESSIONAL, SLOT, "Operations");
        appointment.confirm();
        return appointment;
    }
    private void auth(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "test", null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}
