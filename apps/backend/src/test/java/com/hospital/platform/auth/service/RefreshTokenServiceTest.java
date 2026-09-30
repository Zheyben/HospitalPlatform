package com.hospital.platform.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hospital.platform.auth.entity.RefreshToken;
import com.hospital.platform.auth.exception.ExpiredRefreshTokenException;
import com.hospital.platform.auth.exception.InvalidRefreshTokenException;
import com.hospital.platform.auth.repository.RefreshTokenRepository;
import com.hospital.platform.security.config.JwtProperties;
import com.hospital.platform.users.entity.User;
import com.hospital.platform.users.repository.UserRepository;
import com.hospital.platform.users.service.AuthenticatedUser;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-28T00:00:00Z"), ZoneOffset.UTC);
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void createsRefreshTokenWithStoredHashOnly() {
        RefreshTokenService refreshTokenService = refreshTokenService(Duration.ofDays(7));
        AuthenticatedUser user = user();
        User userEntity = userEntity();
        when(userRepository.getReferenceById(user.id())).thenReturn(userEntity);

        CreatedRefreshToken createdToken = refreshTokenService.issueToken(user);
        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        assertThat(createdToken.tokenHash()).isNotEqualTo(createdToken.token());
        assertThat(refreshTokenCaptor.getValue().getUser()).isSameAs(userEntity);
        assertThat(refreshTokenCaptor.getValue().getTokenHash()).isEqualTo(createdToken.tokenHash());
        assertThat(refreshTokenCaptor.getValue().getExpiresAt()).isEqualTo(CLOCK.instant().plus(Duration.ofDays(7)));
        assertThat(refreshTokenCaptor.getValue().getCreatedAt()).isEqualTo(CLOCK.instant());
    }

    @Test
    void consumesValidRefreshTokenAtomically() {
        RefreshTokenService refreshTokenService = refreshTokenService(Duration.ofDays(7));
        String token = "valid-refresh-token";
        String tokenHash = new TokenHashService().sha256(token);
        RefreshToken storedToken = refreshToken(tokenHash, CLOCK.instant().plus(Duration.ofDays(1)));
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(storedToken));
        when(refreshTokenRepository.consumeActiveByTokenHash(tokenHash, CLOCK.instant())).thenReturn(1);

        RefreshToken validatedToken = refreshTokenService.consumeToken(token);

        assertThat(validatedToken).isSameAs(storedToken);
        assertThat(validatedToken.getUser().getId()).isEqualTo(USER_ID);
        verify(refreshTokenRepository).consumeActiveByTokenHash(tokenHash, CLOCK.instant());
    }

    @Test
    void rejectsTokenWhenAtomicConsumptionLosesRace() {
        RefreshTokenService refreshTokenService = refreshTokenService(Duration.ofDays(7));
        String token = "concurrently-consumed-refresh-token";
        String tokenHash = new TokenHashService().sha256(token);
        RefreshToken storedToken = refreshToken(tokenHash, CLOCK.instant().plus(Duration.ofDays(1)));
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(storedToken));
        when(refreshTokenRepository.consumeActiveByTokenHash(tokenHash, CLOCK.instant())).thenReturn(0);

        assertThatThrownBy(() -> refreshTokenService.consumeToken(token))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void rejectsExpiredRefreshToken() {
        RefreshTokenService refreshTokenService = refreshTokenService(Duration.ZERO);
        String token = "expired-refresh-token";
        String tokenHash = new TokenHashService().sha256(token);
        when(refreshTokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(refreshToken(tokenHash, CLOCK.instant())));

        assertThatThrownBy(() -> refreshTokenService.consumeToken(token))
                .isInstanceOf(ExpiredRefreshTokenException.class);
    }

    @Test
    void rejectsRevokedRefreshToken() {
        RefreshTokenService refreshTokenService = refreshTokenService(Duration.ofDays(7));
        String token = "revoked-refresh-token";
        String tokenHash = new TokenHashService().sha256(token);
        RefreshToken storedToken = refreshToken(tokenHash, CLOCK.instant().plus(Duration.ofDays(1)));
        storedToken.revoke(CLOCK.instant());
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> refreshTokenService.consumeToken(token))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void logoutRevokesActiveTokensForUser() {
        RefreshTokenService refreshTokenService = refreshTokenService(Duration.ofDays(7));

        refreshTokenService.revokeActiveTokens(USER_ID);

        verify(refreshTokenRepository).revokeActiveByUserId(USER_ID, CLOCK.instant());
    }

    private RefreshTokenService refreshTokenService(Duration refreshExpiration) {
        JwtProperties properties = new JwtProperties(
                "12345678901234567890123456789012",
                Duration.ofMinutes(15),
                refreshExpiration
        );
        return new RefreshTokenService(
                properties,
                new TokenHashService(),
                refreshTokenRepository,
                userRepository,
                CLOCK
        );
    }

    private AuthenticatedUser user() {
        return new AuthenticatedUser(
                USER_ID,
                "patient@example.com",
                "patient",
                "hash",
                true,
                Set.of("PATIENT"),
                Set.of()
        );
    }

    private User userEntity() {
        return new User(USER_ID, "patient", "patient@example.com", "hash", true);
    }

    private RefreshToken refreshToken(String tokenHash, Instant expiresAt) {
        return new RefreshToken(userEntity(), tokenHash, expiresAt, CLOCK.instant());
    }
}
