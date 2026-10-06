package com.hospital.platform.patients.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hospital.platform.patients.dto.CreatePatientRequestDTO;
import com.hospital.platform.patients.dto.LinkUserRequestDTO;
import com.hospital.platform.patients.dto.PatientResponseDTO;
import com.hospital.platform.patients.dto.PatientStatus;
import com.hospital.platform.patients.dto.UpdatePatientRequestDTO;
import com.hospital.platform.patients.dto.UpdatePatientStatusRequestDTO;
import com.hospital.platform.patients.entity.Patient;
import com.hospital.platform.patients.exception.DuplicateDocumentException;
import com.hospital.platform.patients.exception.PatientAlreadyLinkedException;
import com.hospital.platform.patients.exception.PatientNotFoundException;
import com.hospital.platform.patients.exception.UserNotFoundException;
import com.hospital.platform.patients.mapper.PatientMapper;
import com.hospital.platform.patients.repository.PatientRepository;
import com.hospital.platform.users.service.CurrentUserService;
import com.hospital.platform.users.service.UserLookupService;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    private static final UUID PATIENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID USER_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID INSURANCE_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserLookupService userLookupService;

    @Mock
    private CurrentUserService currentUserService;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository, userLookupService, currentUserService, new PatientMapper());
    }

    @Test
    void createsPatientWithUniqueDocument() {
        CreatePatientRequestDTO request = new CreatePatientRequestDTO(
                "dni",
                "12345678",
                LocalDate.of(1990, 1, 1),
                "999999999",
                "Main street"
        );
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponseDTO response = patientService.createPatient(request);
        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);

        verify(patientRepository).save(patientCaptor.capture());
        assertThat(patientCaptor.getValue().getDocumentType()).isEqualTo("DNI");
        assertThat(patientCaptor.getValue().getDocumentNumber()).isEqualTo("12345678");
        assertThat(response.documentNumber()).isEqualTo("12345678");
    }

    @Test
    void rejectsDuplicatedDocument() {
        when(patientRepository.existsByDocumentTypeAndDocumentNumber("DNI", "12345678")).thenReturn(true);

        assertThatThrownBy(() -> patientService.createPatient(new CreatePatientRequestDTO(
                "DNI",
                "12345678",
                LocalDate.of(1990, 1, 1),
                null,
                null
        ))).isInstanceOf(DuplicateDocumentException.class);
    }

    @Test
    void registrationLinksPatientAndPersistsInsurance() {
        when(patientRepository.saveAndFlush(any(Patient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponseDTO response = patientService.registerPatient(
                USER_ID, "dni", "12345678", LocalDate.of(1990, 1, 1), " +57 300 1234567 ",
                "SIS", INSURANCE_ID, "  Avenida Lima 123  ", "  Femenino  "
        );

        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.insurance()).isEqualTo("SIS");
        assertThat(response.insuranceId()).isEqualTo(INSURANCE_ID);
        assertThat(response.documentType()).isEqualTo("DNI");
        assertThat(response.address()).isEqualTo("Avenida Lima 123");
        assertThat(response.sex()).isEqualTo("Femenino");
    }

    @Test
    void registrationRejectsDuplicateDocumentBeforePersisting() {
        when(patientRepository.existsByDocumentTypeAndDocumentNumber("DNI", "12345678")).thenReturn(true);

        assertThatThrownBy(() -> patientService.registerPatient(
                USER_ID, "DNI", "12345678", LocalDate.of(1990, 1, 1), "3001234567", "SIS", INSURANCE_ID,
                null, null
        )).isInstanceOf(DuplicateDocumentException.class);
        verify(patientRepository, org.mockito.Mockito.never()).saveAndFlush(any(Patient.class));
    }

    @Test
    void rejectsUnknownPatient() {
        when(patientRepository.findByIdAndDeletedAtIsNull(PATIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.findPatientById(PATIENT_ID))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void findsCurrentPatientFromCurrentUserContract() {
        Patient patient = patient();
        patient.linkUser(USER_ID);
        when(currentUserService.currentUserId()).thenReturn(USER_ID);
        when(patientRepository.findByUserIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(patient));

        PatientResponseDTO response = patientService.findCurrentPatient();

        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.id()).isEqualTo(PATIENT_ID);
    }

    @Test
    void updatesAdministrativePatientData() {
        Patient patient = patient();
        when(patientRepository.findByIdAndDeletedAtIsNull(PATIENT_ID)).thenReturn(Optional.of(patient));

        PatientResponseDTO response = patientService.updatePatient(
                PATIENT_ID,
                new UpdatePatientRequestDTO("DNI", "87654321", LocalDate.of(1988, 2, 2), "988888888", "New address")
        );

        assertThat(response.documentNumber()).isEqualTo("87654321");
        assertThat(response.phone()).isEqualTo("988888888");
        assertThat(response.address()).isEqualTo("New address");
    }

    @Test
    void appliesSoftDeleteWhenStatusIsUpdated() {
        Patient patient = patient();
        when(patientRepository.findByIdAndDeletedAtIsNull(PATIENT_ID)).thenReturn(Optional.of(patient));

        PatientResponseDTO response = patientService.updateStatus(
                PATIENT_ID,
                new UpdatePatientStatusRequestDTO(PatientStatus.INACTIVE)
        );

        assertThat(patient.getDeletedAt()).isNotNull();
        assertThat(response.active()).isFalse();
    }

    @Test
    void linksExistingUser() {
        Patient patient = patient();
        when(patientRepository.findByIdAndDeletedAtIsNull(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(userLookupService.existsActiveUser(USER_ID)).thenReturn(true);

        PatientResponseDTO response = patientService.linkUser(PATIENT_ID, new LinkUserRequestDTO(USER_ID));

        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(patient.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void rejectsUnknownUserWhenLinking() {
        Patient patient = patient();
        when(patientRepository.findByIdAndDeletedAtIsNull(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(userLookupService.existsActiveUser(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> patientService.linkUser(PATIENT_ID, new LinkUserRequestDTO(USER_ID)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void rejectsLinkingPatientThatAlreadyHasUser() {
        Patient patient = patient();
        patient.linkUser(USER_ID);
        UUID anotherUserId = UUID.fromString("88888888-8888-8888-8888-888888888888");
        when(patientRepository.findByIdAndDeletedAtIsNull(PATIENT_ID)).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> patientService.linkUser(PATIENT_ID, new LinkUserRequestDTO(anotherUserId)))
                .isInstanceOf(PatientAlreadyLinkedException.class);
    }

    private Patient patient() {
        return new Patient(
                PATIENT_ID,
                "DNI",
                "12345678",
                LocalDate.of(1990, 1, 1),
                "999999999",
                "Main street"
        );
    }
}
