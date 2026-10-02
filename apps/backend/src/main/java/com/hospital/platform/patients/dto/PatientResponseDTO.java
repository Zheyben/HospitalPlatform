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
        String address,
        boolean active
) {
}
