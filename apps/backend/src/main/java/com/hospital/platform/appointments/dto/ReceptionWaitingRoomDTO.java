package com.hospital.platform.appointments.dto;

import com.hospital.platform.appointments.entity.FlowStage;
import java.time.LocalTime;
import java.util.UUID;

public record ReceptionWaitingRoomDTO(
        UUID appointmentId,
        String patientDisplay,
        LocalTime startTime,
        String professionalName,
        String specialtyName,
        FlowStage flowStage
) {
}
