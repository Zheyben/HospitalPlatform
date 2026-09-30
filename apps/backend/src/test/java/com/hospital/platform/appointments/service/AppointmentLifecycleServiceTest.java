package com.hospital.platform.appointments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hospital.platform.agenda.contract.AvailabilitySlotReference;
import com.hospital.platform.agenda.contract.AvailabilitySlotReleaseService;
import com.hospital.platform.agenda.contract.AvailabilitySlotReservationService;
import com.hospital.platform.agenda.contract.SlotReservationRejectedException;
import com.hospital.platform.appointments.dto.AppointmentResponseDTO;
import com.hospital.platform.appointments.entity.Appointment;
import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.exception.AppointmentSuccessorExistsException;
import com.hospital.platform.appointments.exception.InvalidAppointmentTransitionException;
import com.hospital.platform.appointments.exception.PatientNotAvailableException;
import com.hospital.platform.appointments.exception.ProfessionalNotAvailableException;
import com.hospital.platform.appointments.exception.SlotUnavailableException;
import com.hospital.platform.appointments.mapper.AppointmentMapper;
import com.hospital.platform.appointments.repository.AppointmentRepository;
import com.hospital.platform.audit.contract.AuditEventType;
import com.hospital.platform.audit.contract.AuditLogService;
import com.hospital.platform.patients.contract.PatientLookupService;
import com.hospital.platform.patients.contract.PatientReference;
import com.hospital.platform.professionals.contract.ProfessionalLookupService;
import com.hospital.platform.users.service.CurrentUserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AppointmentLifecycleServiceTest {

    private static final UUID APPOINTMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111151");
    private static final UUID SUCCESSOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111152");
    private static final UUID OLD_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222261");
    private static final UUID NEW_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222262");
    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333361");
    private static final UUID NEW_PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333362");
    private static final UUID SPECIALTY_ID = UUID.fromString("44444444-4444-4444-4444-444444444471");
    private static final UUID PATIENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666681");
    private static final UUID OTHER_PATIENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666682");
    private static final UUID USER_ID = UUID.fromString("77777777-7777-7777-7777-777777777781");

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
    void confirmsScheduledAppointmentAndRecordsAudit() {
        Appointment appointment = lockedAppointment(AppointmentStatus.SCHEDULED);
        authorizeAdmin();

        AppointmentResponseDTO response = appointmentService.confirmAppointment(APPOINTMENT_ID);

        assertThat(response.appointmentStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(auditLogService).record(
                AuditEventType.APPOINTMENT_CONFIRMED,
                "Appointment",
                APPOINTMENT_ID,
                Map.of("appointmentStatus", "SCHEDULED"),
                Map.of("appointmentStatus", "CONFIRMED")
        );
        assertThat(appointment.getAppointmentStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    void treatsRepeatedConfirmationAsIdempotent() {
        lockedAppointment(AppointmentStatus.CONFIRMED);
        authorizeAdmin();

        AppointmentResponseDTO response = appointmentService.confirmAppointment(APPOINTMENT_ID);

        assertThat(response.appointmentStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verifyNoInteractions(auditLogService, releaseService);
    }

    @Test
    void rejectsConfirmationFromCancelledStatus() {
        lockedAppointment(AppointmentStatus.CANCELLED);
        authorizeAdmin();

        assertThatThrownBy(() -> appointmentService.confirmAppointment(APPOINTMENT_ID))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
    }

    @Test
    void rejectsConfirmationFromRescheduledStatus() {
        lockedAppointment(AppointmentStatus.RESCHEDULED);
        authorizeAdmin();

        assertThatThrownBy(() -> appointmentService.confirmAppointment(APPOINTMENT_ID))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
    }

    @Test
    void enforcesPatientOwnershipBeforeConfirmation() {
        Appointment appointment = appointment(AppointmentStatus.SCHEDULED, OTHER_PATIENT_ID);
        when(appointmentRepository.findByIdForUpdate(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        authenticate("PATIENT");
        when(currentUserService.currentUserId()).thenReturn(USER_ID);
        when(patientLookupService.findActivePatientReferenceByUserId(USER_ID))
                .thenReturn(Optional.of(new PatientReference(PATIENT_ID, true)));

        assertThatThrownBy(() -> appointmentService.confirmAppointment(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void allowsPatientToConfirmOwnAppointment() {
        lockedAppointment(AppointmentStatus.SCHEDULED);
        authorizePatient(PATIENT_ID);

        AppointmentResponseDTO response = appointmentService.confirmAppointment(APPOINTMENT_ID);

        assertThat(response.appointmentStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(auditLogService).record(
                AuditEventType.APPOINTMENT_CONFIRMED,
                "Appointment",
                APPOINTMENT_ID,
                Map.of("appointmentStatus", "SCHEDULED"),
                Map.of("appointmentStatus", "CONFIRMED")
        );
    }

    @Test
    void cancelsScheduledAppointmentReleasesSlotAndRecordsAudit() {
        Appointment appointment = lockedAppointment(AppointmentStatus.SCHEDULED);
        authorizeAdmin();
        when(currentUserService.currentUserId()).thenReturn(USER_ID);

        AppointmentResponseDTO response = appointmentService.cancelAppointment(APPOINTMENT_ID);

        assertThat(response.appointmentStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(response.cancelledAt()).isNotNull();
        assertThat(response.cancelledBy()).isEqualTo(USER_ID);
        verify(releaseService).releaseReservedSlot(OLD_SLOT_ID);
        verify(auditLogService).record(
                AuditEventType.APPOINTMENT_CANCELLED,
                "Appointment",
                APPOINTMENT_ID,
                Map.of("appointmentStatus", "SCHEDULED"),
                Map.of("appointmentStatus", "CANCELLED")
        );
        assertThat(appointment.getAppointmentStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancelsConfirmedAppointment() {
        lockedAppointment(AppointmentStatus.CONFIRMED);
        authorizeAdmin();
        when(currentUserService.currentUserId()).thenReturn(USER_ID);

        appointmentService.cancelAppointment(APPOINTMENT_ID);

        verify(auditLogService).record(
                AuditEventType.APPOINTMENT_CANCELLED,
                "Appointment",
                APPOINTMENT_ID,
                Map.of("appointmentStatus", "CONFIRMED"),
                Map.of("appointmentStatus", "CANCELLED")
        );
    }

    @Test
    void treatsRepeatedCancellationAsIdempotentWithoutChangingCancellationData() {
        Appointment appointment = lockedAppointment(AppointmentStatus.CANCELLED);
        authorizeAdmin();
        LocalDateTime cancelledAt = appointment.getCancelledAt();
        UUID cancelledBy = appointment.getCancelledBy();

        AppointmentResponseDTO response = appointmentService.cancelAppointment(APPOINTMENT_ID);

        assertThat(response.cancelledAt()).isEqualTo(cancelledAt);
        assertThat(response.cancelledBy()).isEqualTo(cancelledBy);
        verifyNoInteractions(releaseService, auditLogService, currentUserService);
    }

    @Test
    void rejectsProfessionalLifecycleOperation() {
        lockedAppointment(AppointmentStatus.SCHEDULED);
        authenticate("PROFESSIONAL");

        assertThatThrownBy(() -> appointmentService.cancelAppointment(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(releaseService, auditLogService);
    }

    @Test
    void allowsPatientToCancelOwnAppointment() {
        lockedAppointment(AppointmentStatus.SCHEDULED);
        authorizePatient(PATIENT_ID);

        AppointmentResponseDTO response = appointmentService.cancelAppointment(APPOINTMENT_ID);

        assertThat(response.appointmentStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(response.cancelledBy()).isEqualTo(USER_ID);
        verify(releaseService).releaseReservedSlot(OLD_SLOT_ID);
    }

    @Test
    void rejectsPatientCancellingAnotherPatientsAppointment() {
        when(appointmentRepository.findByIdForUpdate(APPOINTMENT_ID))
                .thenReturn(Optional.of(appointment(AppointmentStatus.SCHEDULED, OTHER_PATIENT_ID)));
        authorizePatient(PATIENT_ID);

        assertThatThrownBy(() -> appointmentService.cancelAppointment(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(releaseService, auditLogService);
    }

    @Test
    void allowsReceptionistToConfirmActivePatientsAppointment() {
        lockedAppointment(AppointmentStatus.SCHEDULED);
        authorizeReceptionist(true);

        AppointmentResponseDTO response = appointmentService.confirmAppointment(APPOINTMENT_ID);

        assertThat(response.appointmentStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    void allowsReceptionistToCancelActivePatientsAppointment() {
        lockedAppointment(AppointmentStatus.SCHEDULED);
        authorizeReceptionist(true);
        when(currentUserService.currentUserId()).thenReturn(USER_ID);

        AppointmentResponseDTO response = appointmentService.cancelAppointment(APPOINTMENT_ID);

        assertThat(response.appointmentStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(releaseService).releaseReservedSlot(OLD_SLOT_ID);
    }

    @Test
    void rejectsReceptionistOperatingOnInactivePatient() {
        lockedAppointment(AppointmentStatus.SCHEDULED);
        authorizeReceptionist(false);

        assertThatThrownBy(() -> appointmentService.confirmAppointment(APPOINTMENT_ID))
                .isInstanceOf(PatientNotAvailableException.class);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void reschedulesAppointmentUsingNewSlotReference() {
        Appointment original = prepareReschedule(AppointmentStatus.SCHEDULED);

        AppointmentResponseDTO response = appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID);

        ArgumentCaptor<Appointment> successorCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository, times(2)).saveAndFlush(successorCaptor.capture());
        Appointment successor = successorCaptor.getAllValues().stream()
                .filter(appointment -> APPOINTMENT_ID.equals(appointment.getRescheduledFromId()))
                .findFirst()
                .orElseThrow();
        assertThat(original.getAppointmentStatus()).isEqualTo(AppointmentStatus.RESCHEDULED);
        assertThat(successor.getAppointmentStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(successor.getPatientId()).isEqualTo(PATIENT_ID);
        assertThat(successor.getProfessionalId()).isEqualTo(NEW_PROFESSIONAL_ID);
        assertThat(successor.getSlotId()).isEqualTo(NEW_SLOT_ID);
        assertThat(successor.getRescheduledFromId()).isEqualTo(APPOINTMENT_ID);
        assertThat(successor.getFlowStage()).isNull();
        assertThat(response.id()).isEqualTo(successor.getId());
        verify(releaseService).releaseReservedSlot(OLD_SLOT_ID);
        verify(auditLogService).record(
                AuditEventType.APPOINTMENT_RESCHEDULED,
                "Appointment",
                APPOINTMENT_ID,
                Map.of("appointmentStatus", "SCHEDULED", "slotId", OLD_SLOT_ID.toString()),
                Map.of(
                        "appointmentStatus", "RESCHEDULED",
                        "successorAppointmentId", successor.getId().toString(),
                        "slotId", NEW_SLOT_ID.toString()
                )
        );
    }

    @Test
    void reschedulesConfirmedAppointment() {
        prepareReschedule(AppointmentStatus.CONFIRMED);

        appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID);

        verify(appointmentRepository, times(2)).saveAndFlush(any(Appointment.class));
        verify(releaseService).releaseReservedSlot(OLD_SLOT_ID);
    }

    @Test
    void allowsPatientToRescheduleOwnAppointment() {
        Appointment original = lockedAppointment(AppointmentStatus.SCHEDULED);
        authorizePatient(PATIENT_ID);
        prepareRescheduleDependencies();

        AppointmentResponseDTO response = appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID);

        assertThat(original.getAppointmentStatus()).isEqualTo(AppointmentStatus.RESCHEDULED);
        assertThat(response.patientId()).isEqualTo(PATIENT_ID);
        verify(releaseService).releaseReservedSlot(OLD_SLOT_ID);
    }

    @Test
    void rejectsPatientReschedulingAnotherPatientsAppointment() {
        when(appointmentRepository.findByIdForUpdate(APPOINTMENT_ID))
                .thenReturn(Optional.of(appointment(AppointmentStatus.SCHEDULED, OTHER_PATIENT_ID)));
        authorizePatient(PATIENT_ID);

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(reservationService, releaseService, auditLogService);
    }

    @Test
    void allowsReceptionistToRescheduleActivePatientsAppointment() {
        Appointment original = lockedAppointment(AppointmentStatus.SCHEDULED);
        authorizeReceptionist(true);
        prepareRescheduleDependencies();

        AppointmentResponseDTO response = appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID);

        assertThat(original.getAppointmentStatus()).isEqualTo(AppointmentStatus.RESCHEDULED);
        assertThat(response.appointmentStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void rejectsReschedulingCancelledAppointment() {
        lockedAppointment(AppointmentStatus.CANCELLED);
        authorizeAdmin();

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
    }

    @Test
    void rejectsReschedulingAlreadyRescheduledAppointment() {
        lockedAppointment(AppointmentStatus.RESCHEDULED);
        authorizeAdmin();

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
    }

    @Test
    void rejectsSecondDirectSuccessorBeforeReservingSlot() {
        Appointment original = lockedAppointment(AppointmentStatus.SCHEDULED);
        authorizeAdmin();
        when(appointmentRepository.findByRescheduledFromId(APPOINTMENT_ID))
                .thenReturn(Optional.of(new Appointment(
                        SUCCESSOR_ID,
                        PATIENT_ID,
                        NEW_PROFESSIONAL_ID,
                        NEW_SLOT_ID,
                        "Routine control",
                        original.getId()
                )));

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID))
                .isInstanceOf(AppointmentSuccessorExistsException.class);
        verifyNoInteractions(reservationService, releaseService, auditLogService);
    }

    @Test
    void mapsRejectedNewSlotReservationToSlotUnavailable() {
        lockedAppointment(AppointmentStatus.SCHEDULED);
        authorizeAdmin();
        when(appointmentRepository.findByRescheduledFromId(APPOINTMENT_ID)).thenReturn(Optional.empty());
        when(reservationService.reserveUsableSlot(NEW_SLOT_ID))
                .thenThrow(new SlotReservationRejectedException(NEW_SLOT_ID));

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID))
                .isInstanceOf(SlotUnavailableException.class);
        verify(releaseService, never()).releaseReservedSlot(any());
        verifyNoInteractions(auditLogService);
    }

    @Test
    void rejectsInactiveProfessionalDerivedFromNewSlot() {
        lockedAppointment(AppointmentStatus.SCHEDULED);
        authorizeAdmin();
        when(appointmentRepository.findByRescheduledFromId(APPOINTMENT_ID)).thenReturn(Optional.empty());
        when(reservationService.reserveUsableSlot(NEW_SLOT_ID)).thenReturn(newSlotReference());
        when(professionalLookupService.existsActiveProfessional(NEW_PROFESSIONAL_ID)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID))
                .isInstanceOf(ProfessionalNotAvailableException.class);
        verify(appointmentRepository, never()).saveAndFlush(any());
    }

    private Appointment prepareReschedule(AppointmentStatus status) {
        Appointment original = lockedAppointment(status);
        authorizeAdmin();
        prepareRescheduleDependencies();
        return original;
    }

    private void prepareRescheduleDependencies() {
        when(appointmentRepository.findByRescheduledFromId(APPOINTMENT_ID)).thenReturn(Optional.empty());
        when(reservationService.reserveUsableSlot(NEW_SLOT_ID)).thenReturn(newSlotReference());
        when(professionalLookupService.existsActiveProfessional(NEW_PROFESSIONAL_ID)).thenReturn(true);
        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Appointment lockedAppointment(AppointmentStatus status) {
        Appointment appointment = appointment(status, PATIENT_ID);
        when(appointmentRepository.findByIdForUpdate(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        return appointment;
    }

    private void authorizeAdmin() {
        authenticate("ADMIN");
        when(patientLookupService.existsActivePatient(PATIENT_ID)).thenReturn(true);
    }

    private void authorizePatient(UUID patientId) {
        authenticate("PATIENT");
        when(currentUserService.currentUserId()).thenReturn(USER_ID);
        when(patientLookupService.findActivePatientReferenceByUserId(USER_ID))
                .thenReturn(Optional.of(new PatientReference(patientId, true)));
    }

    private void authorizeReceptionist(boolean activePatient) {
        authenticate("RECEPTIONIST");
        when(patientLookupService.existsActivePatient(PATIENT_ID)).thenReturn(activePatient);
    }

    private Appointment appointment(AppointmentStatus status, UUID patientId) {
        Appointment appointment = new Appointment(
                APPOINTMENT_ID,
                patientId,
                PROFESSIONAL_ID,
                OLD_SLOT_ID,
                "Routine control"
        );
        if (status == AppointmentStatus.CONFIRMED) {
            appointment.confirm();
        } else if (status == AppointmentStatus.CANCELLED) {
            appointment.cancel(LocalDateTime.of(2026, 10, 1, 8, 0), USER_ID);
        } else if (status == AppointmentStatus.RESCHEDULED) {
            appointment.markRescheduled();
        }
        return appointment;
    }

    private AvailabilitySlotReference newSlotReference() {
        return new AvailabilitySlotReference(
                NEW_SLOT_ID,
                NEW_PROFESSIONAL_ID,
                SPECIALTY_ID,
                LocalDate.of(2030, 10, 1),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30)
        );
    }

    private void authenticate(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "test-user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        ));
    }
}
