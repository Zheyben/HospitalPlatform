package com.hospital.platform.agenda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        boolean usable,
        UUID professionalId,
        String professionalName,
        UUID specialtyId,
        String specialtyName
) {
    public AvailabilitySlotResponseDTO(UUID id, UUID scheduleId, LocalDate slotDate,
            LocalTime startTime, LocalTime endTime, AvailabilitySlotStatus status, boolean usable) {
        this(id, scheduleId, slotDate, startTime, endTime, status, usable, null, null, null, null);
    }

    @JsonProperty("slotId")
    public UUID slotId() {
        return id;
    }
}
