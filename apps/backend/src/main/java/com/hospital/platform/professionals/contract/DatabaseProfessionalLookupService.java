package com.hospital.platform.professionals.contract;

import com.hospital.platform.professionals.repository.ProfessionalRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DatabaseProfessionalLookupService implements ProfessionalLookupService {

    private final ProfessionalRepository professionalRepository;

    DatabaseProfessionalLookupService(ProfessionalRepository professionalRepository) {
        this.professionalRepository = professionalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveProfessional(UUID professionalId) {
        return professionalRepository.findByIdAndDeletedAtIsNull(professionalId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActiveProfessionalLinkedToUser(UUID professionalId, UUID userId) {
        return professionalRepository.existsByIdAndUserIdAndDeletedAtIsNull(professionalId, userId);
    }
}
