package com.hospital.platform.appointments.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RescheduleAppointmentRequestDTO(
        @NotNull UUID slotId
) {
}
