package com.hospital.platform.professionals.exception;

import com.hospital.platform.professionals.controller.ProfessionalController;
import com.hospital.platform.professionals.dto.ProfessionalErrorResponseDTO;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProfessionalErrorResponseDTO> handleAccessDenied(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "Access denied", "ACCESS_DENIED");
    }

    private ResponseEntity<ProfessionalErrorResponseDTO> error(HttpStatus status, String message, String errorCode) {
        return ResponseEntity.status(status)
                .body(new ProfessionalErrorResponseDTO(false, message, errorCode, Instant.now()));
    }
}
