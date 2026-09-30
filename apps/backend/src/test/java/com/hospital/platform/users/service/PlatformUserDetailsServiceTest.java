package com.hospital.platform.users.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.hospital.platform.users.entity.User;
import com.hospital.platform.users.mapper.UserSecurityMapper;
import com.hospital.platform.users.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlatformUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void disabledUserIsMappedAsDisabledSecurityPrincipal() {
        User disabledUser = new User(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                "disabled",
                "disabled@example.com",
                "hash",
                false
        );
        when(userRepository.findByEmailWithRolesAndPermissions("disabled@example.com"))
                .thenReturn(Optional.of(disabledUser));

        PlatformUserDetailsService service = new PlatformUserDetailsService(
                userRepository,
                new UserSecurityMapper()
        );

        AuthenticatedUser userDetails = service.loadUserByUsername("disabled@example.com");

        assertThat(userDetails.isEnabled()).isFalse();
    }
}
