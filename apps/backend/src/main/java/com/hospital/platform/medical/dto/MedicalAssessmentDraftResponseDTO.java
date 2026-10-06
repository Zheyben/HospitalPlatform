package com.hospital.platform.medical.dto;

import java.time.Instant;
import java.util.UUID;

public record MedicalAssessmentDraftResponseDTO(
        UUID encounterId,
        int version,
        MedicalAssessmentDraftDTO assessment,
        Instant updatedAt
) {
}
