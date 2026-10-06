package com.hospital.platform.patients.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PatientResponseDTO(
        UUID id,
        UUID userId,
        String documentType,
        String documentNumber,
        LocalDate birthDate,
        String phone,
        String insurance,
        UUID insuranceId,
        String address,
        String sex,
        boolean active,
        String maritalStatus,
        String occupation,
        String district,
        String educationLevel,
        String affiliationNumber,
        String emergencyContactName,
        String emergencyContactRelationship,
        String emergencyContactPhone
) {
    public PatientResponseDTO(UUID id, UUID userId, String documentType, String documentNumber,
                              LocalDate birthDate, String phone, String insurance, UUID insuranceId,
                              String address, String sex, boolean active) {
        this(id, userId, documentType, documentNumber, birthDate, phone, insurance,
                insuranceId, address, sex, active, null, null, null, null, null, null, null, null);
    }

    public PatientResponseDTO(UUID id, UUID userId, String documentType, String documentNumber,
                              LocalDate birthDate, String phone, String insurance,
                              String address, String sex, boolean active) {
        this(id, userId, documentType, documentNumber, birthDate, phone, insurance,
                null, address, sex, active, null, null, null, null, null, null, null, null);
    }
}
