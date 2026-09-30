package com.hospital.platform.security.jwt;

import com.hospital.platform.security.config.JwtProperties;
import com.hospital.platform.security.exception.JwtConfigurationException;
import com.hospital.platform.users.service.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JwtService {

    private static final String ROLES_CLAIM = "roles";
    private static final int MIN_HS256_SECRET_BYTES = 32;

    private final JwtProperties jwtProperties;
    private final Clock clock;

    public JwtService(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    public String generateAccessToken(AuthenticatedUser user) {
        Instant now = clock.instant();

        return Jwts.builder()
                .setSubject(user.id().toString())
                .claim(ROLES_CLAIM, user.roleNames())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(jwtProperties.jwtExpiration())))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = parseClaims(token).get(ROLES_CLAIM);
        if (roles instanceof List<?> roleList) {
            return roleList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }

    public boolean isTokenValid(String token, UUID expectedUserId) {
        try {
            Claims claims = parseClaims(token);
            return expectedUserId.toString().equals(claims.getSubject())
                    && claims.getExpiration().after(Date.from(clock.instant()));
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .setClock(() -> Date.from(clock.instant()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key signingKey() {
        String secret = jwtProperties.jwtSecret();
        if (!StringUtils.hasText(secret)) {
            throw new JwtConfigurationException("JWT_SECRET is required");
        }

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_HS256_SECRET_BYTES) {
            throw new JwtConfigurationException("JWT_SECRET must contain at least 32 bytes for HS256");
        }

        return Keys.hmacShaKeyFor(secretBytes);
    }
}
