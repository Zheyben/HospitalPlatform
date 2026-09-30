package com.hospital.platform.patients.dto;

import java.time.Instant;

public record PatientErrorResponseDTO(
        boolean success,
        String message,
        String errorCode,
        Instant timestamp
) {
}
