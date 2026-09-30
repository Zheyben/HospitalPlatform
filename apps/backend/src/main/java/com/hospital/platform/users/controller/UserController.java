package com.hospital.platform.users.controller;

import com.hospital.platform.users.dto.AssignRoleRequestDTO;
import com.hospital.platform.users.dto.CreateUserRequestDTO;
import com.hospital.platform.users.dto.UpdateUserRequestDTO;
import com.hospital.platform.users.dto.UpdateUserStatusRequestDTO;
import com.hospital.platform.users.dto.UserResponseDTO;
import com.hospital.platform.users.service.UserService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        UserResponseDTO response = userService.createUser(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDTO> findUsers() {
        return userService.findUsers();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponseDTO findCurrentUser() {
        return userService.findCurrentUser();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO findUserById(@PathVariable UUID id) {
        return userService.findUserById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequestDTO request
    ) {
        return userService.updateUser(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequestDTO request
    ) {
        return userService.updateStatus(id, request);
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO assignRoles(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRoleRequestDTO request
    ) {
        return userService.assignRoles(id, request);
    }
}
