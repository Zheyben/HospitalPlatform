package com.hospital.platform.users.dto;

import java.time.Instant;

public record UserErrorResponseDTO(
        boolean success,
        String message,
        String errorCode,
        Instant timestamp
) {
}
