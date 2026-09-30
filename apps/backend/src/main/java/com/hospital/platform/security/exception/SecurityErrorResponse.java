package com.hospital.platform.security.exception;

import java.time.Instant;

public record SecurityErrorResponse(
        boolean success,
        String message,
        String errorCode,
        Instant timestamp
) {

    public static SecurityErrorResponse unauthorized() {
        return new SecurityErrorResponse(false, "Authentication required", "AUTHENTICATION_REQUIRED", Instant.now());
    }

    public static SecurityErrorResponse forbidden() {
        return new SecurityErrorResponse(false, "Access denied", "ACCESS_DENIED", Instant.now());
    }
}
