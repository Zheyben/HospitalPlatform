package com.hospital.platform.patients.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.hospital.platform.patients.entity.Patient;
import com.hospital.platform.patients.repository.PatientRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabasePatientLookupServiceTest {

    private static final UUID PATIENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID USER_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

    @Mock
    private PatientRepository patientRepository;

    private PatientLookupService patientLookupService;

    @BeforeEach
    void setUp() {
        patientLookupService = new DatabasePatientLookupService(patientRepository);
    }

    @Test
    void detectsExistingPatient() {
        when(patientRepository.existsById(PATIENT_ID)).thenReturn(true);

        assertThat(patientLookupService.existsPatient(PATIENT_ID)).isTrue();
    }

    @Test
    void detectsActivePatient() {
        Patient patient = patient();
        when(patientRepository.findByIdAndDeletedAtIsNull(PATIENT_ID)).thenReturn(Optional.of(patient));

        assertThat(patientLookupService.existsActivePatient(PATIENT_ID)).isTrue();
    }

    @Test
    void returnsMinimalPatientReference() {
        Patient patient = patient();
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));

        Optional<PatientReference> reference = patientLookupService.findPatientReference(PATIENT_ID);

        assertThat(reference).isPresent();
        assertThat(reference.get().id()).isEqualTo(PATIENT_ID);
        assertThat(reference.get().active()).isTrue();
    }

    @Test
    void returnsInactivePatientReferenceWhenPatientIsSoftDeleted() {
        Patient patient = patient();
        patient.deactivate();
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));

        Optional<PatientReference> reference = patientLookupService.findPatientReference(PATIENT_ID);

        assertThat(reference).isPresent();
        assertThat(reference.get().active()).isFalse();
    }

    @Test
    void resolvesActivePatientReferenceByUserId() {
        Patient patient = patient();
        patient.linkUser(USER_ID);
        when(patientRepository.findByUserIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(patient));

        Optional<PatientReference> reference = patientLookupService.findActivePatientReferenceByUserId(USER_ID);

        assertThat(reference).contains(new PatientReference(PATIENT_ID, true));
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
