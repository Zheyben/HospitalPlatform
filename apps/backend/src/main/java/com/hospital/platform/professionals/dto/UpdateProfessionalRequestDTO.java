package com.hospital.platform.professionals.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfessionalRequestDTO(
        @NotBlank
        @Size(max = 100)
        String licenseNumber
) {
}
