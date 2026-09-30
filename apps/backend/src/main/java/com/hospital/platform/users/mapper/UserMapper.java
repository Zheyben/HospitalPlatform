package com.hospital.platform.users.mapper;

import com.hospital.platform.users.dto.UserAccountDTO;
import com.hospital.platform.users.dto.UserResponseDTO;
import com.hospital.platform.users.entity.Permission;
import com.hospital.platform.users.entity.Role;
import com.hospital.platform.users.entity.User;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled() && user.getDeletedAt() == null,
                roleNames(user),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public List<UserResponseDTO> toResponseList(List<User> users) {
        return users.stream()
                .map(this::toResponse)
                .toList();
    }

    public UserAccountDTO toDto(User user) {
        return new UserAccountDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled() && user.getDeletedAt() == null,
                roleNames(user),
                permissionNames(user)
        );
    }

    private Set<String> roleNames(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<String> permissionNames(User user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toUnmodifiableSet());
    }
}
