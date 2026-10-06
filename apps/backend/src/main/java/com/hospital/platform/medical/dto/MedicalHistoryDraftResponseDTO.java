package com.hospital.platform.medical.dto;

import java.time.Instant;
import java.util.UUID;

public record MedicalHistoryDraftResponseDTO(
        UUID encounterId,
        int version,
        MedicalHistoryDraftDTO history,
        Instant updatedAt
) {
}
