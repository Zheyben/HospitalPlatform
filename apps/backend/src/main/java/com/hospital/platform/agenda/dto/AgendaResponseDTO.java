package com.hospital.platform.agenda.dto;

import java.time.LocalTime;
import java.util.UUID;

public record AgendaResponseDTO(
        UUID id,
        UUID professionalId,
        UUID specialtyId,
        Integer dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean active
) {
}
