package com.hospital.platform.auth.service;

import java.time.Instant;

public record CreatedRefreshToken(
        String token,
        String tokenHash,
        Instant expiresAt
) {
}
