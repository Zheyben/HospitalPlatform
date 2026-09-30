package com.hospital.platform.users.dto;

import java.util.Set;
import java.util.UUID;

public record UserAccountDTO(
        UUID id,
        String username,
        String email,
        boolean enabled,
        Set<String> roles,
        Set<String> permissions
) {
}
