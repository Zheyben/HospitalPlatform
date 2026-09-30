package com.hospital.platform.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hospital.platform.auth.dto.LoginRequestDTO;
import com.hospital.platform.auth.dto.LoginResponseDTO;
import com.hospital.platform.auth.dto.RefreshTokenRequestDTO;
import com.hospital.platform.auth.entity.RefreshToken;
import com.hospital.platform.auth.mapper.AuthTokenMapper;
import com.hospital.platform.security.config.JwtProperties;
import com.hospital.platform.security.jwt.JwtService;
import com.hospital.platform.users.entity.User;
import com.hospital.platform.users.service.AuthenticatedUser;
import com.hospital.platform.users.service.PlatformUserDetailsService;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PlatformUserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
                "12345678901234567890123456789012",
                Duration.ofMinutes(15),
                Duration.ofDays(7)
        );
        authService = new AuthService(
                authenticationManager,
                userDetailsService,
                jwtService,
                refreshTokenService,
                new AuthTokenMapper(),
                jwtProperties
        );
    }

    @Test
    void loginReturnsAccessTokenRefreshTokenAndExpiration() {
        AuthenticatedUser user = user(true);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(refreshTokenService.issueToken(user)).thenReturn(refreshToken());

        LoginResponseDTO response = authService.login(new LoginRequestDTO("admin@example.com", "password"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.expiresIn()).isEqualTo(900);
    }

    @Test
    void loginRejectsInvalidCredentials() {
        LoginRequestDTO request = new LoginRequestDTO("admin@example.com", "bad-password");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refreshRotatesValidRefreshToken() {
        AuthenticatedUser user = user(true);
        UUID userId = user.id();
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("current-refresh-token");

        when(refreshTokenService.consumeToken("current-refresh-token"))
                .thenReturn(refreshToken(userId));
        when(userDetailsService.loadUserById(userId)).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(refreshTokenService.issueToken(user)).thenReturn(refreshToken());

        LoginResponseDTO response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenService).consumeToken("current-refresh-token");
    }

    @Test
    void refreshRejectsDisabledUser() {
        AuthenticatedUser user = user(false);
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("current-refresh-token");

        when(refreshTokenService.consumeToken("current-refresh-token"))
                .thenReturn(refreshToken(user.id()));
        when(userDetailsService.loadUserById(user.id())).thenReturn(user);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    void logoutRevokesActiveUserTokens() {
        AuthenticatedUser user = user(true);

        authService.logout(user);

        verify(refreshTokenService).revokeActiveTokens(user.id());
    }

    private CreatedRefreshToken refreshToken() {
        return new CreatedRefreshToken("refresh-token", "refresh-token-hash", Instant.now().plusSeconds(3600));
    }

    private RefreshToken refreshToken(UUID userId) {
        User user = new User(userId, "admin", "admin@example.com", "hash", true);
        return new RefreshToken(user, "hash", Instant.now().plusSeconds(60), Instant.now());
    }

    private AuthenticatedUser user(boolean enabled) {
        return new AuthenticatedUser(
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                "admin@example.com",
                "admin",
                "hash",
                enabled,
                Set.of("ADMIN"),
                Set.of("USERS_MANAGE")
        );
    }
}
