package com.hospital.platform.appointments.dto;

import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.entity.FlowStage;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponseDTO(
        UUID id,
        UUID patientId,
        UUID professionalId,
        UUID slotId,
        AppointmentStatus appointmentStatus,
        FlowStage flowStage,
        String reason,
        LocalDateTime cancelledAt,
        UUID cancelledBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
