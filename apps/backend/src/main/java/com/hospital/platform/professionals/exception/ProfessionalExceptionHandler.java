package com.hospital.platform.professionals.exception;

import com.hospital.platform.professionals.controller.ProfessionalController;
import com.hospital.platform.professionals.dto.ProfessionalErrorResponseDTO;
import com.hospital.platform.users.exception.DuplicateEmailException;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice(assignableTypes = ProfessionalController.class)
public class ProfessionalExceptionHandler {

    @ExceptionHandler(ProfessionalNotFoundException.class)
    ResponseEntity<ProfessionalErrorResponseDTO> handleProfessionalNotFound(ProfessionalNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "Professional not found", "PROFESSIONAL_NOT_FOUND");
    }

    @ExceptionHandler(DuplicateProfessionalException.class)
    ResponseEntity<ProfessionalErrorResponseDTO> handleDuplicateProfessional(DuplicateProfessionalException exception) {
        return error(HttpStatus.CONFLICT, "Professional license number already exists", "DUPLICATE_PROFESSIONAL");
    }

    @ExceptionHandler(DuplicateEmailException.class)
    ResponseEntity<ProfessionalErrorResponseDTO> handleDuplicateEmail(DuplicateEmailException exception) {
        return error(HttpStatus.CONFLICT, "Email already exists", "EMAIL_ALREADY_EXISTS");
    }

    @ExceptionHandler(SpecialtyNotAvailableException.class)
    ResponseEntity<ProfessionalErrorResponseDTO> handleSpecialtyNotAvailable(SpecialtyNotAvailableException exception) {
        return error(HttpStatus.BAD_REQUEST, "Specialty is not active or does not exist", "SPECIALTY_NOT_AVAILABLE");
    }

    @ExceptionHandler(ProfessionalDomainConflictException.class)
    ResponseEntity<ProfessionalErrorResponseDTO> handleProfessionalDomainConflict(
            ProfessionalDomainConflictException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), "PROFESSIONAL_DOMAIN_CONFLICT");
    }

    @ExceptionHandler({InvalidProfessionalLicenseException.class, MethodArgumentNotValidException.class,
            IllegalArgumentException.class})
    ResponseEntity<ProfessionalErrorResponseDTO> handleValidation(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "Invalid professional details", "VALIDATION_ERROR");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProfessionalErrorResponseDTO> handleAccessDenied(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "Access denied", "ACCESS_DENIED");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProfessionalErrorResponseDTO> handleDomainConflict(DataIntegrityViolationException exception) {
        return error(HttpStatus.CONFLICT, "Professional has care in progress or conflicting capacity",
                "PROFESSIONAL_DOMAIN_CONFLICT");
    }

    private ResponseEntity<ProfessionalErrorResponseDTO> error(HttpStatus status, String message, String errorCode) {
        return ResponseEntity.status(status)
                .body(new ProfessionalErrorResponseDTO(false, message, errorCode, Instant.now()));
    }
}
