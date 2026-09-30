package com.hospital.platform.professionals.contract;

import java.util.UUID;

public interface ProfessionalLookupService {

    boolean existsActiveProfessional(UUID professionalId);

    boolean isActiveProfessionalLinkedToUser(UUID professionalId, UUID userId);
}
