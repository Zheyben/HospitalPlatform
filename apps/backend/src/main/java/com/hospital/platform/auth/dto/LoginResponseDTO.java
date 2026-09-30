package com.hospital.platform.auth.dto;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
