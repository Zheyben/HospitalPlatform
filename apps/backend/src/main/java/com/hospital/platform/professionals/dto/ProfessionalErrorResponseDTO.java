package com.hospital.platform.professionals.dto;

import java.time.Instant;

public record ProfessionalErrorResponseDTO(
        boolean success,
        String message,
        String errorCode,
        Instant timestamp
) {
}
