package com.hospital.platform.appointments.dto;

import java.time.Instant;

public record AppointmentErrorResponseDTO(
        boolean success,
        String message,
        String errorCode,
        Instant timestamp
) {
}
