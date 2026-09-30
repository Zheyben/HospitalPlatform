package com.hospital.platform.professionals.dto;

import java.util.UUID;

public record ProfessionalResponseDTO(
        UUID id,
        UUID userId,
        String licenseNumber,
        boolean active
) {
}
