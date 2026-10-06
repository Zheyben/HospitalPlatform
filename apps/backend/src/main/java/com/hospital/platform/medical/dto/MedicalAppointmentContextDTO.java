package com.hospital.platform.medical.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record MedicalAppointmentContextDTO(
        UUID appointmentId,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        String flowStage,
        String reason,
        UUID patientId,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        LocalDate birthDate,
        String phone,
        String insurance,
        String address,
        String sex,
        String maritalStatus,
        String occupation,
        String district,
        String educationLevel,
        String affiliationNumber,
        String emergencyContactName,
        String emergencyContactRelationship,
        String emergencyContactPhone,
        String clinicalRecordNumber,
        LocalDateTime clinicalRecordOpenedAt
) {
}
