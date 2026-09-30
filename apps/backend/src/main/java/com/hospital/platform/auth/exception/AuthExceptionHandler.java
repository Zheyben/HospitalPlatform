package com.hospital.platform.auth.exception;

import com.hospital.platform.auth.controller.AuthController;
import com.hospital.platform.auth.dto.AuthErrorResponseDTO;
import com.hospital.platform.security.exception.JwtConfigurationException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<AuthErrorResponseDTO> handleAuthenticationException(AuthenticationException exception) {
        return error(HttpStatus.UNAUTHORIZED, "Authentication failed", "AUTHENTICATION_FAILED");
    }

    @ExceptionHandler(DisabledException.class)
    ResponseEntity<AuthErrorResponseDTO> handleDisabledException(DisabledException exception) {
        return error(HttpStatus.UNAUTHORIZED, "User is disabled", "USER_DISABLED");
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    ResponseEntity<AuthErrorResponseDTO> handleInvalidRefreshTokenException(InvalidRefreshTokenException exception) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid refresh token", "INVALID_REFRESH_TOKEN");
    }

    @ExceptionHandler(ExpiredRefreshTokenException.class)
    ResponseEntity<AuthErrorResponseDTO> handleExpiredRefreshTokenException(ExpiredRefreshTokenException exception) {
        return error(HttpStatus.UNAUTHORIZED, "Expired refresh token", "EXPIRED_REFRESH_TOKEN");
    }

    @ExceptionHandler(JwtConfigurationException.class)
    ResponseEntity<AuthErrorResponseDTO> handleJwtConfigurationException(JwtConfigurationException exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "JWT is not configured", "JWT_CONFIGURATION_ERROR");
    }

    private ResponseEntity<AuthErrorResponseDTO> error(HttpStatus status, String message, String errorCode) {
        return ResponseEntity.status(status)
                .body(new AuthErrorResponseDTO(false, message, errorCode, Instant.now()));
    }
}
