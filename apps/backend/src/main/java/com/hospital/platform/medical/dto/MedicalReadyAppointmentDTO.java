package com.hospital.platform.medical.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record MedicalReadyAppointmentDTO(
        UUID appointmentId,
        LocalDate appointmentDate,
        LocalTime startTime,
        String flowStage
) {
}
