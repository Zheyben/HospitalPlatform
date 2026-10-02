package com.hospital.platform.patients.repository;

import com.hospital.platform.patients.entity.Patient;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    List<Patient> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<Patient> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Patient> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);

    boolean existsByDocumentTypeAndDocumentNumberAndIdNot(String documentType, String documentNumber, UUID id);

    boolean existsByUserIdAndIdNot(UUID userId, UUID id);
}
