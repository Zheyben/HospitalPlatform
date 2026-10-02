package com.hospital.platform.users.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.users.dto.AssignRoleRequestDTO;
import com.hospital.platform.users.dto.CreateUserRequestDTO;
import com.hospital.platform.users.dto.UpdateUserRequestDTO;
import com.hospital.platform.users.dto.UpdateUserStatusRequestDTO;
import com.hospital.platform.users.dto.UserResponseDTO;
import com.hospital.platform.users.entity.Role;
import com.hospital.platform.users.entity.RoleName;
import com.hospital.platform.users.entity.User;
import com.hospital.platform.users.exception.DuplicateEmailException;
import com.hospital.platform.users.exception.DuplicateUsernameException;
import com.hospital.platform.users.exception.RoleNotFoundException;
import com.hospital.platform.users.exception.UserNotFoundException;
import com.hospital.platform.users.mapper.UserMapper;
import com.hospital.platform.users.repository.RoleRepository;
import com.hospital.platform.users.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final UUID USER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CapacityGateway capacityGateway;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, passwordEncoder, new UserMapper(), capacityGateway);
    }

    @Test
    void createsUserWithEncodedPasswordAndExistingRoles() {
        Role adminRole = role(RoleName.ADMIN);
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "Admin",
                "Admin@Example.com",
                "plain-password",
                Set.of(RoleName.ADMIN)
        );
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(roleRepository.findByNameIn(Set.of("ADMIN"))).thenReturn(List.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = userService.createUser(request);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(userCaptor.getValue().getPasswordHash()).isNotEqualTo("plain-password");
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("admin@example.com");
        assertThat(response.roles()).containsExactly("ADMIN");
    }

    @Test
    void registersPatientWithOnlyPatientRoleAndEncodedPassword() {
        Role patientRole = role(RoleName.PATIENT);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(roleRepository.findByNameIn(Set.of("PATIENT"))).thenReturn(List.of(patientRole));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.registerPatientUser(
                " PATIENT@Example.com ", "plain-password", " Ana ", " Pérez "
        );

        assertThat(user.getEmail()).isEqualTo("patient@example.com");
        assertThat(user.getUsername()).startsWith("patient-");
        assertThat(user.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(user.getFirstName()).isEqualTo("Ana");
        assertThat(user.getLastName()).isEqualTo("Pérez");
        assertThat(user.getRoles()).extracting(Role::getName).containsExactly("PATIENT");
    }

    @Test
    void registrationRejectsDuplicateEmailBeforeEncodingPassword() {
        when(userRepository.existsByEmailIgnoreCase("patient@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerPatientUser(
                "patient@example.com", "plain-password", "Ana", "Pérez"
        )).isInstanceOf(DuplicateEmailException.class);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rejectsDuplicatedEmail() {
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "admin",
                "admin@example.com",
                "plain-password",
                Set.of(RoleName.ADMIN)
        );
        when(userRepository.existsByEmailIgnoreCase("admin@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateEmailException.class);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rejectsDuplicatedUsername() {
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "admin",
                "admin@example.com",
                "plain-password",
                Set.of(RoleName.ADMIN)
        );
        when(userRepository.existsByUsernameIgnoreCase("admin")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateUsernameException.class);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rejectsUnknownUser() {
        when(userRepository.findActiveByIdWithRoles(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findsCurrentUserFromSecurityContext() {
        AuthenticatedUser currentUser = new AuthenticatedUser(
                USER_ID,
                "admin@example.com",
                "admin",
                "encoded-password",
                true,
                Set.of("ADMIN"),
                Set.of()
        );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                currentUser.getAuthorities()
        ));
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findActiveByIdWithRoles(USER_ID)).thenReturn(Optional.of(user()));

        try {
            UserResponseDTO response = userService.findCurrentUser();

            assertThat(response.id()).isEqualTo(USER_ID);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void updatesBasicUserDataWithoutChangingPassword() {
        User user = user();
        when(userRepository.findActiveByIdWithRoles(USER_ID)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.updateUser(
                USER_ID,
                new UpdateUserRequestDTO("updated", "Updated@Example.com")
        );

        assertThat(response.username()).isEqualTo("updated");
        assertThat(response.email()).isEqualTo("updated@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("encoded-password");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void disablesUserAccount() {
        User user = user();
        when(userRepository.findActiveByIdWithRoles(USER_ID)).thenReturn(Optional.of(user));
        org.mockito.Mockito.doAnswer(call -> { user.changeStatus(false); return null; })
                .when(capacityGateway).setUserEnabled(USER_ID, false);

        UserResponseDTO response = userService.updateStatus(
                USER_ID,
                new UpdateUserStatusRequestDTO(false)
        );

        assertThat(user.isEnabled()).isFalse();
        assertThat(response.enabled()).isFalse();
        verify(capacityGateway).setUserEnabled(USER_ID, false);
    }

    @Test
    void assignsExistingRoles() {
        User user = user();
        when(userRepository.findActiveByIdWithRoles(USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findByNameIn(Set.of("ADMIN", "TRIAGE")))
                .thenReturn(List.of(role(RoleName.ADMIN), role(RoleName.TRIAGE)));

        UserResponseDTO response = userService.assignRoles(
                USER_ID,
                new AssignRoleRequestDTO(Set.of(RoleName.ADMIN, RoleName.TRIAGE))
        );

        assertThat(response.roles()).containsExactlyInAnyOrder("ADMIN", "TRIAGE");
    }

    @Test
    void rejectsMissingRole() {
        User user = user();
        when(userRepository.findActiveByIdWithRoles(USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findByNameIn(Set.of("SYSTEM"))).thenReturn(List.of());

        assertThatThrownBy(() -> userService.assignRoles(USER_ID, new AssignRoleRequestDTO(Set.of(RoleName.SYSTEM))))
                .isInstanceOf(RoleNotFoundException.class);
    }

    private User user() {
        User user = new User(USER_ID, "admin", "admin@example.com", "encoded-password", true);
        user.replaceRoles(Set.of(role(RoleName.ADMIN)));
        return user;
    }

    private Role role(RoleName roleName) {
        return new Role(UUID.nameUUIDFromBytes(roleName.name().getBytes()), roleName.name(), roleName.name());
    }
}
