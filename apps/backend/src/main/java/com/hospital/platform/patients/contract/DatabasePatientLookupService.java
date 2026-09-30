package com.hospital.platform.patients.contract;

import com.hospital.platform.patients.entity.Patient;
import com.hospital.platform.patients.repository.PatientRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DatabasePatientLookupService implements PatientLookupService {

    private final PatientRepository patientRepository;

    DatabasePatientLookupService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsPatient(UUID patientId) {
        return patientRepository.existsById(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActivePatient(UUID patientId) {
        return patientRepository.findByIdAndDeletedAtIsNull(patientId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PatientReference> findPatientReference(UUID patientId) {
        return patientRepository.findById(patientId)
                .map(this::toReference);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PatientReference> findActivePatientReferenceByUserId(UUID userId) {
        return patientRepository.findByUserIdAndDeletedAtIsNull(userId)
                .map(this::toReference);
    }

    private PatientReference toReference(Patient patient) {
        return new PatientReference(patient.getId(), patient.isActive());
    }
}
