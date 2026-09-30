package com.hospital.platform.appointments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hospital.platform.agenda.contract.AvailabilitySlotReleaseService;
import com.hospital.platform.agenda.contract.AvailabilitySlotReservationService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final UUID APPOINTMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111171");
    private static final UUID PATIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222271");
    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333371");
    private static final UUID SLOT_ID = UUID.fromString("44444444-4444-4444-4444-444444444471");
    private static final UUID USER_ID = UUID.fromString("55555555-5555-5555-5555-555555555571");

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PatientLookupService patientLookupService;
    @Mock
    private ProfessionalLookupService professionalLookupService;
    @Mock
    private AvailabilitySlotReservationService reservationService;
    @Mock
    private AvailabilitySlotReleaseService releaseService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private CurrentUserService currentUserService;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                appointmentRepository,
                patientLookupService,
                professionalLookupService,
                reservationService,
                releaseService,
                auditLogService,
                currentUserService,
                new AppointmentMapper()
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void checksInConfirmedAppointmentAndRecordsCompleteStateAudit() {
        Appointment appointment = lockedAppointment(FlowStage.CHECK_IN, false);
        authenticate("RECEPTIONIST");

        var response = appointmentService.checkInAppointment(APPOINTMENT_ID);

        assertThat(response.flowStage()).isEqualTo(FlowStage.CHECK_IN);
        verify(auditLogService).record(
                AuditEventType.APPOINTMENT_CHECKED_IN,
                "Appointment",
                APPOINTMENT_ID,
                state("CONFIRMED", null),
                state("CONFIRMED", "CHECK_IN")
        );
    }

    @Test
    void rejectsWaitingWhenCheckInWasSkipped() {
        lockedAppointment(null, false);
        authenticate("RECEPTIONIST");

        assertThatThrownBy(() -> appointmentService.moveAppointmentToWaiting(APPOINTMENT_ID))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void repeatedCheckInIsIdempotentWithoutTouchingOrAuditing() {
        Appointment appointment = lockedAppointment(FlowStage.CHECK_IN, true);
        authenticate("RECEPTIONIST");
        var updatedAt = appointment.getUpdatedAt();

        var response = appointmentService.checkInAppointment(APPOINTMENT_ID);

        assertThat(response.flowStage()).isEqualTo(FlowStage.CHECK_IN);
        assertThat(appointment.getUpdatedAt()).isEqualTo(updatedAt);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void startsAttentionOnlyForLinkedActiveProfessional() {
        lockedAppointment(FlowStage.WAITING, true);
        authorizeProfessional(true);

        var response = appointmentService.startAppointmentAttention(APPOINTMENT_ID);

        assertThat(response.flowStage()).isEqualTo(FlowStage.IN_ATTENTION);
        verify(professionalLookupService)
                .isActiveProfessionalLinkedToUser(PROFESSIONAL_ID, USER_ID);
        verify(auditLogService).record(
                AuditEventType.APPOINTMENT_ATTENTION_STARTED,
                "Appointment",
                APPOINTMENT_ID,
                state("CONFIRMED", "WAITING"),
                state("CONFIRMED", "IN_ATTENTION")
        );
    }

    @Test
    void rejectsProfessionalWithoutAppointmentOwnership() {
        lockedAppointment(FlowStage.WAITING, true);
        authorizeProfessional(false);

        assertThatThrownBy(() -> appointmentService.startAppointmentAttention(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void completesAppointmentWithoutReleasingConsumedSlot() {
        Appointment appointment = lockedAppointment(FlowStage.IN_ATTENTION, true);
        authorizeProfessional(true);

        var response = appointmentService.completeAppointment(APPOINTMENT_ID);

        assertThat(response.appointmentStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(response.flowStage()).isEqualTo(FlowStage.FINISHED);
        assertThat(appointment.getAppointmentStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        verify(releaseService, never()).releaseReservedSlot(SLOT_ID);
        verify(auditLogService).record(
                AuditEventType.APPOINTMENT_COMPLETED,
                "Appointment",
                APPOINTMENT_ID,
                state("CONFIRMED", "IN_ATTENTION"),
                state("COMPLETED", "FINISHED")
        );
    }

    @Test
    void rejectsAdminAndPatientFromOperationalFlow() {
        lockedAppointment(null, false);
        authenticate("ADMIN");
        assertThatThrownBy(() -> appointmentService.checkInAppointment(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);

        authenticate("PATIENT");
        assertThatThrownBy(() -> appointmentService.checkInAppointment(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(auditLogService);
    }

    private Appointment lockedAppointment(FlowStage targetStage, boolean applyStage) {
        Appointment appointment = new Appointment(
                APPOINTMENT_ID,
                PATIENT_ID,
                PROFESSIONAL_ID,
                SLOT_ID,
                "Operations test"
        );
        appointment.confirm();
        if (applyStage) {
            applyStage(appointment, targetStage);
        }
        when(appointmentRepository.findByIdForUpdate(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        return appointment;
    }

    private void applyStage(Appointment appointment, FlowStage stage) {
        appointment.checkIn();
        if (stage == FlowStage.CHECK_IN) {
            return;
        }
        appointment.moveToWaiting();
        if (stage == FlowStage.WAITING) {
            return;
        }
        appointment.startAttention();
        if (stage == FlowStage.IN_ATTENTION) {
            return;
        }
        appointment.complete();
    }

    private void authorizeProfessional(boolean linked) {
        authenticate("PROFESSIONAL");
        when(currentUserService.currentUserId()).thenReturn(USER_ID);
        when(professionalLookupService.isActiveProfessionalLinkedToUser(PROFESSIONAL_ID, USER_ID))
                .thenReturn(linked);
    }

    private void authenticate(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "test-user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        ));
    }

    private Map<String, Object> state(String status, String stage) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("appointmentStatus", status);
        values.put("flowStage", stage);
        return values;
    }
}
