package com.hospital.platform.users.exception;

import com.hospital.platform.users.controller.UserController;
import com.hospital.platform.users.dto.UserErrorResponseDTO;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = UserController.class)
public class UserExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<UserErrorResponseDTO> handleUserNotFound(UserNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "User not found", "USER_NOT_FOUND");
    }

    @ExceptionHandler(RoleNotFoundException.class)
    ResponseEntity<UserErrorResponseDTO> handleRoleNotFound(RoleNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "Role not found", "ROLE_NOT_FOUND");
    }

    @ExceptionHandler(DuplicateEmailException.class)
    ResponseEntity<UserErrorResponseDTO> handleDuplicateEmail(DuplicateEmailException exception) {
        return error(HttpStatus.CONFLICT, "Email already exists", "EMAIL_ALREADY_EXISTS");
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    ResponseEntity<UserErrorResponseDTO> handleDuplicateUsername(DuplicateUsernameException exception) {
        return error(HttpStatus.CONFLICT, "Username already exists", "USERNAME_ALREADY_EXISTS");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<UserErrorResponseDTO> handleAccessDenied(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "Access denied", "ACCESS_DENIED");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<UserErrorResponseDTO> handleDomainConflict(DataIntegrityViolationException exception) {
        return error(HttpStatus.CONFLICT, "User change conflicts with a linked role or active care",
                "USER_DOMAIN_CONFLICT");
    }

    private ResponseEntity<UserErrorResponseDTO> error(HttpStatus status, String message, String errorCode) {
        return ResponseEntity.status(status)
                .body(new UserErrorResponseDTO(false, message, errorCode, Instant.now()));
    }
}
