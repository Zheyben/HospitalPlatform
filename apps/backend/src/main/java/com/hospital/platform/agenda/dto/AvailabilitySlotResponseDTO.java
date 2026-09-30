package com.hospital.platform.agenda.dto;

import com.hospital.platform.agenda.entity.AvailabilitySlotStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AvailabilitySlotResponseDTO(
        UUID id,
        UUID scheduleId,
        LocalDate slotDate,
        LocalTime startTime,
        LocalTime endTime,
        AvailabilitySlotStatus status,
        boolean usable
) {
}
