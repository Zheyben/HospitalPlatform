package com.hospital.platform.auth.dto;

import java.time.Instant;

public record AuthErrorResponseDTO(
        boolean success,
        String message,
        String errorCode,
        Instant timestamp
) {
}
