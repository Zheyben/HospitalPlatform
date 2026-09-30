package com.hospital.platform.agenda.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.UUID;

public record UpdateAgendaRequestDTO(
        @NotNull
        UUID professionalId,

        @NotNull
        UUID specialtyId,

        @NotNull
        @Min(0)
        @Max(6)
        Integer dayOfWeek,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime
) {
}
