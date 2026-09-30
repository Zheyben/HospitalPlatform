package com.hospital.platform.agenda.contract;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AvailabilitySlotReference(
        UUID slotId,
        UUID professionalId,
        UUID specialtyId,
        LocalDate slotDate,
        LocalTime startTime,
        LocalTime endTime
) {
}
