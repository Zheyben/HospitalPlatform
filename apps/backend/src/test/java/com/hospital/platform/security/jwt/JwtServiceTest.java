package com.hospital.platform.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.hospital.platform.security.config.JwtProperties;
import com.hospital.platform.users.service.AuthenticatedUser;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "12345678901234567890123456789012";
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-28T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void generatesValidJwtWithSubjectAndRoles() {
        JwtService jwtService = jwtService(SECRET);
        AuthenticatedUser user = user(true);

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUserId(token)).isEqualTo(user.id());
        assertThat(jwtService.extractRoles(token)).containsExactlyInAnyOrder("ADMIN", "PATIENT");
        assertThat(jwtService.isTokenValid(token, user.id())).isTrue();
    }

    @Test
    void rejectsInvalidJwt() {
        JwtService jwtService = jwtService(SECRET);

        assertThat(jwtService.isTokenValid("invalid-token", UUID.randomUUID())).isFalse();
    }

    private JwtService jwtService(String secret) {
        return new JwtService(new JwtProperties(secret, Duration.ofMinutes(15), Duration.ofDays(7)), CLOCK);
    }

    private AuthenticatedUser user(boolean enabled) {
        return new AuthenticatedUser(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "admin@example.com",
                "admin",
                "hash",
                enabled,
                Set.of("ADMIN", "PATIENT"),
                Set.of("USERS_MANAGE")
        );
    }
}
