package com.hospital.platform.auth.service;

import com.hospital.platform.auth.entity.RefreshToken;
import com.hospital.platform.auth.exception.ExpiredRefreshTokenException;
import com.hospital.platform.auth.exception.InvalidRefreshTokenException;
import com.hospital.platform.auth.repository.RefreshTokenRepository;
import com.hospital.platform.security.config.JwtProperties;
import com.hospital.platform.users.entity.User;
import com.hospital.platform.users.repository.UserRepository;
import com.hospital.platform.users.service.AuthenticatedUser;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_BYTES = 64;

    private final JwtProperties jwtProperties;
    private final TokenHashService tokenHashService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            JwtProperties jwtProperties,
            TokenHashService tokenHashService,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.jwtProperties = jwtProperties;
        this.tokenHashService = tokenHashService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public CreatedRefreshToken issueToken(AuthenticatedUser user) {
        byte[] tokenBytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        String tokenHash = tokenHashService.sha256(token);
        Instant now = clock.instant();
        Instant expiresAt = now.plus(jwtProperties.refreshTokenExpiration());
        User userReference = userRepository.getReferenceById(user.id());

        refreshTokenRepository.save(new RefreshToken(userReference, tokenHash, expiresAt, now));

        return new CreatedRefreshToken(token, tokenHash, expiresAt);
    }

    @Transactional
    public RefreshToken consumeToken(String token) {
        String tokenHash = tokenHashService.sha256(token);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token is invalid"));
        Instant now = clock.instant();

        if (refreshToken.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token is invalid");
        }

        if (refreshToken.isExpired(now)) {
            throw new ExpiredRefreshTokenException("Refresh token is expired");
        }

        if (refreshTokenRepository.consumeActiveByTokenHash(tokenHash, now) != 1) {
            throw new InvalidRefreshTokenException("Refresh token is invalid");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeActiveTokens(UUID userId) {
        refreshTokenRepository.revokeActiveByUserId(userId, clock.instant());
    }
}
