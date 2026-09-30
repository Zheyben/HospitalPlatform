package com.hospital.platform.users.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequestDTO(
        @NotNull
        Boolean enabled
) {
}
