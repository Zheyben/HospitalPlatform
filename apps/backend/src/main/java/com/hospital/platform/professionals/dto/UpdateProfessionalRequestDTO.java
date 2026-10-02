package com.hospital.platform.professionals.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UpdateProfessionalRequestDTO(
        @NotBlank
        @Pattern(regexp = "[0-9]{4,6}")
        String licenseNumber
) {
}
