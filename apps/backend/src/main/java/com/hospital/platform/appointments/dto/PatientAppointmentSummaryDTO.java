package com.hospital.platform.appointments.dto;

import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.entity.FlowStage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record PatientAppointmentSummaryDTO(
        UUID appointmentId,
        AppointmentStatus status,
        FlowStage flowStage,
        String reason,
        UUID specialtyId,
        String specialtyName,
        UUID professionalId,
        String professionalName,
        UUID slotId,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime
) {
}
