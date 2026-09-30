package com.hospital.platform.appointments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hospital.platform.agenda.contract.AvailabilitySlotReference;
import com.hospital.platform.agenda.contract.AvailabilitySlotReleaseService;
import com.hospital.platform.agenda.contract.AvailabilitySlotReservationService;
import com.hospital.platform.agenda.contract.SlotReservationRejectedException;
import com.hospital.platform.appointments.dto.AppointmentResponseDTO;
import com.hospital.platform.appointments.dto.CreateAppointmentRequestDTO;
import com.hospital.platform.appointments.entity.Appointment;
import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.exception.AppointmentNotFoundException;
import com.hospital.platform.appointments.exception.PatientNotAvailableException;
import com.hospital.platform.appointments.exception.ProfessionalNotAvailableException;
import com.hospital.platform.appointments.exception.SlotUnavailableException;
import com.hospital.platform.appointments.mapper.AppointmentMapper;
import com.hospital.platform.appointments.repository.AppointmentRepository;
import com.hospital.platform.audit.contract.AuditLogService;
import com.hospital.platform.patients.contract.PatientLookupService;
import com.hospital.platform.patients.contract.PatientReference;
import com.hospital.platform.professionals.contract.ProfessionalLookupService;
import com.hospital.platform.users.service.CurrentUserService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
class AppointmentServiceTest {

    private static final UUID APPOINTMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SPECIALTY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID PATIENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID OTHER_PATIENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666667");
    private static final UUID USER_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

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
    void createsScheduledAppointmentForAdministrativeActor() {
        authenticate("ADMIN");
        when(patientLookupService.existsActivePatient(PATIENT_ID)).thenReturn(true);
        when(reservationService.reserveUsableSlot(SLOT_ID)).thenReturn(slotReference());
        when(professionalLookupService.existsActiveProfessional(PROFESSIONAL_ID)).thenReturn(true);
        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponseDTO response = appointmentService.createAppointment(request(PATIENT_ID));
        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);

        verify(appointmentRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getProfessionalId()).isEqualTo(PROFESSIONAL_ID);
        assertThat(response.appointmentStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(response.flowStage()).isNull();
    }

    @Test
    void resolvesPatientIdentityForPatientActor() {
        authenticate("PATIENT");
        when(currentUserService.currentUserId()).thenReturn(USER_ID);
        when(patientLookupService.findActivePatientReferenceByUserId(USER_ID))
                .thenReturn(Optional.of(new PatientReference(PATIENT_ID, true)));
        when(reservationService.reserveUsableSlot(SLOT_ID)).thenReturn(slotReference());
        when(professionalLookupService.existsActiveProfessional(PROFESSIONAL_ID)).thenReturn(true);
        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponseDTO response = appointmentService.createAppointment(request(null));

        assertThat(response.patientId()).isEqualTo(PATIENT_ID);
    }

    @Test
    void rejectsPatientAttemptingToChooseAnotherPatientId() {
        authenticate("PATIENT");

        assertThatThrownBy(() -> appointmentService.createAppointment(request(OTHER_PATIENT_ID)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsInactiveOrMissingPatient() {
        authenticate("ADMIN");
        when(patientLookupService.existsActivePatient(PATIENT_ID)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.createAppointment(request(PATIENT_ID)))
                .isInstanceOf(PatientNotAvailableException.class);
    }

    @Test
    void mapsRejectedReservationToSlotUnavailable() {
        authenticate("ADMIN");
        when(patientLookupService.existsActivePatient(PATIENT_ID)).thenReturn(true);
        when(reservationService.reserveUsableSlot(SLOT_ID))
                .thenThrow(new SlotReservationRejectedException(SLOT_ID));

        assertThatThrownBy(() -> appointmentService.createAppointment(request(PATIENT_ID)))
                .isInstanceOf(SlotUnavailableException.class);
    }

    @Test
    void rejectsInactiveOrMissingProfessionalDerivedFromSlot() {
        authenticate("ADMIN");
        when(patientLookupService.existsActivePatient(PATIENT_ID)).thenReturn(true);
        when(reservationService.reserveUsableSlot(SLOT_ID)).thenReturn(slotReference());
        when(professionalLookupService.existsActiveProfessional(PROFESSIONAL_ID)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.createAppointment(request(PATIENT_ID)))
                .isInstanceOf(ProfessionalNotAvailableException.class);
    }

    @Test
    void limitsPatientListingToOwnAppointments() {
        authenticate("PATIENT");
        when(currentUserService.currentUserId()).thenReturn(USER_ID);
        when(patientLookupService.findActivePatientReferenceByUserId(USER_ID))
                .thenReturn(Optional.of(new PatientReference(PATIENT_ID, true)));
        when(appointmentRepository.findAllByPatientIdOrderByCreatedAtDesc(PATIENT_ID))
                .thenReturn(List.of(appointment(PATIENT_ID)));

        List<AppointmentResponseDTO> response = appointmentService.findAppointments();

        assertThat(response).hasSize(1);
        verify(appointmentRepository).findAllByPatientIdOrderByCreatedAtDesc(PATIENT_ID);
    }

    @Test
    void rejectsPatientAccessToAnotherPatientsAppointment() {
        authenticate("PATIENT");
        when(appointmentRepository.findById(APPOINTMENT_ID))
                .thenReturn(Optional.of(appointment(OTHER_PATIENT_ID)));
        when(currentUserService.currentUserId()).thenReturn(USER_ID);
        when(patientLookupService.findActivePatientReferenceByUserId(USER_ID))
                .thenReturn(Optional.of(new PatientReference(PATIENT_ID, true)));

        assertThatThrownBy(() -> appointmentService.findAppointmentById(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsUnknownAppointment() {
        authenticate("ADMIN");
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.findAppointmentById(APPOINTMENT_ID))
                .isInstanceOf(AppointmentNotFoundException.class);
    }

    private void authenticate(String role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "test-user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private CreateAppointmentRequestDTO request(UUID patientId) {
        return new CreateAppointmentRequestDTO(SLOT_ID, patientId, "Routine control");
    }

    private AvailabilitySlotReference slotReference() {
        return new AvailabilitySlotReference(
                SLOT_ID,
                PROFESSIONAL_ID,
                SPECIALTY_ID,
                LocalDate.of(2026, 10, 1),
                LocalTime.of(9, 0),
                LocalTime.of(9, 30)
        );
    }

    private Appointment appointment(UUID patientId) {
        return new Appointment(APPOINTMENT_ID, patientId, PROFESSIONAL_ID, SLOT_ID, "Routine control");
    }
}
