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
        String insurance
) {
}
