package com.hospital.platform.patients.contract;

import java.util.Optional;
import java.util.UUID;

public interface PatientLookupService {

    boolean existsPatient(UUID patientId);

    boolean existsActivePatient(UUID patientId);

    Optional<PatientReference> findPatientReference(UUID patientId);

    Optional<PatientReference> findActivePatientReferenceByUserId(UUID userId);
}
