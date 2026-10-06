package com.hospital.platform.professionals.dto;

import java.util.UUID;

public record ProfessionalResponseDTO(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String licenseNumber,
        UUID specialtyId,
        String specialtyName,
        boolean active
) {
}
