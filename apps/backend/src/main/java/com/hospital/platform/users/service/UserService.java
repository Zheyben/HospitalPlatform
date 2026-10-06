package com.hospital.platform.users.service;

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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CapacityGateway capacityGateway;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            CapacityGateway capacityGateway
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.capacityGateway = capacityGateway;
    }

    @Transactional
    public UserResponseDTO createUser(CreateUserRequestDTO request) {
        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());

        assertUsernameAvailable(username);
        assertEmailAvailable(email);

        return userMapper.toResponse(userRepository.save(
                newUser(username, email, request.password(), request.roles())
        ));
    }

    @Transactional
    public User registerPatientUser(String email, String password, String firstName, String lastName) {
        String normalizedEmail = normalizeEmail(email);
        assertEmailAvailable(normalizedEmail);

        User user = newUser(
                "patient-" + UUID.randomUUID(), normalizedEmail, password, Set.of(RoleName.PATIENT)
        );
        user.setNames(firstName.trim(), lastName.trim());
        return userRepository.saveAndFlush(user);
    }

    @Transactional
    public User registerProfessionalUser(String email, String password, String firstName, String lastName) {
        String normalizedEmail = normalizeEmail(email);
        assertEmailAvailable(normalizedEmail);
        User user = newUser(
                "professional-" + UUID.randomUUID(), normalizedEmail, password, Set.of(RoleName.PROFESSIONAL)
        );
        user.setNames(firstName.trim(), lastName.trim());
        return userRepository.saveAndFlush(user);
    }

    private User newUser(String username, String email, String password, Set<RoleName> roleNames) {
        User user = new User(null, username, email, passwordEncoder.encode(password), true);
        user.replaceRoles(resolveRoles(roleNames));
        return user;
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findUsers() {
        return userMapper.toResponseList(userRepository.findAllActiveWithRoles());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findUserById(UUID userId) {
        return userMapper.toResponse(findActiveUser(userId));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findCurrentUser() {
        return userMapper.toResponse(findActiveUser(currentUser().id()));
    }

    @Transactional
    public UserResponseDTO updateUser(UUID userId, UpdateUserRequestDTO request) {
        User user = findActiveUser(userId);
        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());

        assertUsernameAvailableForUpdate(user, username);
        assertEmailAvailableForUpdate(user, email);

        user.updateBasicInfo(username, email);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponseDTO updateStatus(UUID userId, UpdateUserStatusRequestDTO request) {
        findActiveUser(userId);
        capacityGateway.setUserEnabled(userId, request.enabled());
        return userMapper.toResponse(findActiveUser(userId));
    }

    @Transactional
    public UserResponseDTO assignRoles(UUID userId, AssignRoleRequestDTO request) {
        capacityGateway.lockUser(userId);
        User user = findActiveUser(userId);

        user.replaceRoles(resolveRoles(request.roles()));
        return userMapper.toResponse(user);
    }

    private User findActiveUser(UUID userId) {
        return userRepository.findActiveByIdWithRoles(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Set<Role> resolveRoles(Set<RoleName> roleNames) {
        Set<String> requestedRoleNames = roleNames.stream()
                .map(RoleName::name)
                .collect(Collectors.toUnmodifiableSet());
        List<Role> roles = roleRepository.findByNameIn(requestedRoleNames);
        Set<String> foundRoleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toUnmodifiableSet());

        requestedRoleNames.stream()
                .filter(roleName -> !foundRoleNames.contains(roleName))
                .findFirst()
                .ifPresent(missingRole -> {
                    throw new RoleNotFoundException(missingRole);
                });

        return new HashSet<>(roles);
    }

    private void assertUsernameAvailable(String username) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateUsernameException(username);
        }
    }

    private void assertEmailAvailable(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException(email);
        }
    }

    private void assertUsernameAvailableForUpdate(User user, String username) {
        if (!user.getUsername().equalsIgnoreCase(username)
                && userRepository.existsByUsernameIgnoreCaseAndIdNot(username, user.getId())) {
            throw new DuplicateUsernameException(username);
        }
    }

    private void assertEmailAvailableForUpdate(User user, String email) {
        if (!user.getEmail().equalsIgnoreCase(email)
                && userRepository.existsByEmailIgnoreCaseAndIdNot(email, user.getId())) {
            throw new DuplicateEmailException(email);
        }
    }

    private AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new AccessDeniedException("Authenticated user required");
        }
        return user;
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
