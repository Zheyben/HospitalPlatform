package com.hospital.platform.auth.exception;

import com.hospital.platform.auth.controller.AuthController;
import com.hospital.platform.auth.dto.AuthErrorResponseDTO;
import com.hospital.platform.security.exception.JwtConfigurationException;
import com.hospital.platform.patients.exception.DuplicateDocumentException;
import com.hospital.platform.patients.exception.InvalidDocumentException;
import com.hospital.platform.patients.domain.InvalidPatientDemographicsException;
import com.hospital.platform.patients.service.InvalidInsuranceException;
import com.hospital.platform.users.exception.DuplicateEmailException;
import com.hospital.platform.users.exception.RoleNotFoundException;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    ResponseEntity<AuthErrorResponseDTO> handleDuplicateEmail(DuplicateEmailException exception) {
        return error(HttpStatus.CONFLICT, "Email already exists", "EMAIL_ALREADY_EXISTS");
    }

    @ExceptionHandler(DuplicateDocumentException.class)
    ResponseEntity<AuthErrorResponseDTO> handleDuplicateDocument(DuplicateDocumentException exception) {
        return error(HttpStatus.CONFLICT, "Patient document already exists", "DUPLICATE_DOCUMENT");
    }

    @ExceptionHandler(InvalidDocumentException.class)
    ResponseEntity<AuthErrorResponseDTO> handleInvalidDocument(InvalidDocumentException exception) {
        return error(HttpStatus.BAD_REQUEST, "Document type or number is invalid", "VALIDATION_ERROR");
    }

    @ExceptionHandler(InvalidInsuranceException.class)
    ResponseEntity<AuthErrorResponseDTO> handleInvalidInsurance(InvalidInsuranceException exception) {
        return error(HttpStatus.BAD_REQUEST, "Insurance selection is invalid", "VALIDATION_ERROR");
    }

    @ExceptionHandler(InvalidPatientDemographicsException.class)
    ResponseEntity<AuthErrorResponseDTO> handleInvalidDemographics(InvalidPatientDemographicsException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), "VALIDATION_ERROR");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<AuthErrorResponseDTO> handleInvalidRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "Request validation failed", "VALIDATION_ERROR");
    }

    @ExceptionHandler(RoleNotFoundException.class)
    ResponseEntity<AuthErrorResponseDTO> handleMissingRole(RoleNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "Role not found", "ROLE_NOT_FOUND");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<AuthErrorResponseDTO> handleIntegrityViolation(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                String constraint = violation.getConstraintName();
                if ("users_email_key".equals(constraint)) {
                    return error(HttpStatus.CONFLICT, "Email already exists", "EMAIL_ALREADY_EXISTS");
                }
                if ("uq_patients_document_identity".equals(constraint)) {
                    return error(HttpStatus.CONFLICT, "Patient document already exists", "DUPLICATE_DOCUMENT");
                }
            }
            cause = cause.getCause();
        }
        return error(HttpStatus.CONFLICT, "Registration conflicts with existing data", "REGISTRATION_CONFLICT");
    }

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
