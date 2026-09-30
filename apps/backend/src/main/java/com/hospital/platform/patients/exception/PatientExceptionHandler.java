package com.hospital.platform.patients.exception;

import com.hospital.platform.patients.controller.PatientController;
import com.hospital.platform.patients.dto.PatientErrorResponseDTO;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PatientController.class)
public class PatientExceptionHandler {

    @ExceptionHandler(PatientNotFoundException.class)
    ResponseEntity<PatientErrorResponseDTO> handlePatientNotFound(PatientNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "Patient not found", "PATIENT_NOT_FOUND");
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<PatientErrorResponseDTO> handleUserNotFound(UserNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "User not found", "USER_NOT_FOUND");
    }

    @ExceptionHandler(DuplicateDocumentException.class)
    ResponseEntity<PatientErrorResponseDTO> handleDuplicateDocument(DuplicateDocumentException exception) {
        return error(HttpStatus.CONFLICT, "Patient document already exists", "DUPLICATE_DOCUMENT");
    }

    @ExceptionHandler(PatientAlreadyLinkedException.class)
    ResponseEntity<PatientErrorResponseDTO> handlePatientAlreadyLinked(PatientAlreadyLinkedException exception) {
        return error(HttpStatus.CONFLICT, "Patient is already linked to a user", "PATIENT_ALREADY_LINKED");
    }

    @ExceptionHandler(UserAlreadyLinkedException.class)
    ResponseEntity<PatientErrorResponseDTO> handleUserAlreadyLinked(UserAlreadyLinkedException exception) {
        return error(HttpStatus.CONFLICT, "User is already linked to an active patient", "USER_ALREADY_LINKED");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<PatientErrorResponseDTO> handleAccessDenied(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "Access denied", "ACCESS_DENIED");
    }

    private ResponseEntity<PatientErrorResponseDTO> error(HttpStatus status, String message, String errorCode) {
        return ResponseEntity.status(status)
                .body(new PatientErrorResponseDTO(false, message, errorCode, Instant.now()));
    }
}
