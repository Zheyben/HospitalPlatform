package com.hospital.platform.professionals.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record CreateProfessionalRequestDTO(
        UUID userId,

        @NotBlank
        @Pattern(regexp = "[0-9]{4,6}")
        String licenseNumber
) {
}
