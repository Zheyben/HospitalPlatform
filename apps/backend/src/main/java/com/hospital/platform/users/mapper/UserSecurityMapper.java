package com.hospital.platform.users.mapper;

import com.hospital.platform.users.entity.Permission;
import com.hospital.platform.users.entity.Role;
import com.hospital.platform.users.entity.User;
import com.hospital.platform.users.service.AuthenticatedUser;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserSecurityMapper {

    public AuthenticatedUser toAuthenticatedUser(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toUnmodifiableSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toUnmodifiableSet());

        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPasswordHash(),
                user.isEnabled() && user.getDeletedAt() == null,
                roles,
                permissions
        );
    }
}
