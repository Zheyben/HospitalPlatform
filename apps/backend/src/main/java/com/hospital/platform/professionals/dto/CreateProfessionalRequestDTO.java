package com.hospital.platform.professionals.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateProfessionalRequestDTO(
        UUID userId,

        @NotBlank
        @Size(max = 100)
        String licenseNumber
) {
}
