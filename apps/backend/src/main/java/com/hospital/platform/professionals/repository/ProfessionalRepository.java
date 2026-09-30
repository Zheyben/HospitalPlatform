package com.hospital.platform.professionals.repository;

import com.hospital.platform.professionals.entity.Professional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessionalRepository extends JpaRepository<Professional, UUID> {

    List<Professional> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<Professional> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    boolean existsByLicenseNumberIgnoreCase(String licenseNumber);

    boolean existsByLicenseNumberIgnoreCaseAndIdNot(String licenseNumber, UUID id);
}
