package com.hospital.platform.appointments.dto;

import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.entity.FlowStage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReceptionAppointmentSummaryDTO(
        UUID appointmentId,
        UUID patientId,
        String documentType,
        String documentNumber,
        String patientName,
        String professionalName,
        String specialtyName,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,
        FlowStage flowStage
) {
}
