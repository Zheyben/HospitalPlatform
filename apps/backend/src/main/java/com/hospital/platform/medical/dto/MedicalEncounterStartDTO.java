package com.hospital.platform.medical.dto;

import java.time.Instant;
import java.util.UUID;

public record MedicalEncounterStartDTO(
        UUID encounterId,
        UUID appointmentId,
        String status,
        Instant startedAt,
        boolean legacyStart,
        String simulatedRne,
        String simulatedCareType,
        String simulatedService
) {
}
