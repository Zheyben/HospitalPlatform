package com.hospital.platform.professionals.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateProfessionalStatusRequestDTO(@NotNull Boolean active) {
}
