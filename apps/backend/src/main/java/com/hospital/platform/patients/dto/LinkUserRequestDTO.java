package com.hospital.platform.patients.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LinkUserRequestDTO(
        @NotNull
        UUID userId
) {
}
