package com.hospital.platform.users.dto;

import com.hospital.platform.users.entity.RoleName;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record AssignRoleRequestDTO(
        @NotEmpty
        Set<@NotNull RoleName> roles
) {
}
