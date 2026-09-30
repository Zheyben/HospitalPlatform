package com.hospital.platform.security.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hospital.security")
public record JwtProperties(
        String jwtSecret,
        Duration jwtExpiration,
        Duration refreshTokenExpiration
) {
}
