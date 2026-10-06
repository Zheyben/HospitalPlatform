package com.hospital.platform.auth.dto;

import java.time.LocalDate;
import java.util.UUID;

public record RegisterPatientResponseDTO(
        UUID userId,
        UUID patientId,
        String email,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        LocalDate birthDate,
        String phone,
        String insurance,
        UUID insuranceId,
        String address,
        String sex,
        String maritalStatus,
        String occupation,
        String district,
        String educationLevel,
        String affiliationNumber,
        String emergencyContactName,
        String emergencyContactRelationship,
        String emergencyContactPhone
) {
    public RegisterPatientResponseDTO(UUID userId, UUID patientId, String email, String firstName,
                                      String lastName, String documentType, String documentNumber,
                                      LocalDate birthDate, String phone, String insurance, UUID insuranceId,
                                      String address, String sex) {
        this(userId, patientId, email, firstName, lastName, documentType, documentNumber,
                birthDate, phone, insurance, insuranceId, address, sex,
                null, null, null, null, null, null, null, null);
    }
}
