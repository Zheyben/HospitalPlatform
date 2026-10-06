package com.hospital.platform.patients.repository;

import com.hospital.platform.patients.entity.Patient;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    List<Patient> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<Patient> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Patient> findByUserIdAndDeletedAtIsNull(UUID userId);

    @Query(value = """
            select p.id as patientId, p.document_type as documentType,
                   p.document_number as documentNumber,
                   nullif(trim(concat(coalesce(u.first_name, ''), ' ',
                       coalesce(u.last_name, ''))), '') as patientName
            from patients p
            left join users u on u.id = p.user_id
            where p.document_type = :documentType
              and p.document_number = :documentNumber
              and p.deleted_at is null
            """, nativeQuery = true)
    Optional<ReceptionPatientSearchRow> findActiveForReceptionByDocument(
            @Param("documentType") String documentType,
            @Param("documentNumber") String documentNumber
    );

    boolean existsByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);

    boolean existsByDocumentTypeAndDocumentNumberAndIdNot(String documentType, String documentNumber, UUID id);

    boolean existsByUserIdAndIdNot(UUID userId, UUID id);
}
