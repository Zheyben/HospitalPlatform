package com.hospital.platform.appointments.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAppointmentRequestDTO(
        @NotNull UUID slotId,
        UUID patientId,
        String reason
) {
}
