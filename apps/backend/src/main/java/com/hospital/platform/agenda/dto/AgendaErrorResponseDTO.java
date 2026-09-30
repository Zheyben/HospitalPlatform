package com.hospital.platform.agenda.dto;

import java.time.Instant;

public record AgendaErrorResponseDTO(
        boolean success,
        String message,
        String errorCode,
        Instant timestamp
) {
}
