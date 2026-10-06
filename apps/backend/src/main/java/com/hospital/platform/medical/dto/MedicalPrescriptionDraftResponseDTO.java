package com.hospital.platform.medical.dto;

import java.time.Instant;
import java.util.UUID;

public record MedicalPrescriptionDraftResponseDTO(
        UUID encounterId,
        int version,
        MedicalPrescriptionDraftDTO prescription,
        Instant updatedAt
) {
}
