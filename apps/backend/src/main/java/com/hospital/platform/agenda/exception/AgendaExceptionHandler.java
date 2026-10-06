package com.hospital.platform.agenda.exception;

import com.hospital.platform.agenda.controller.AgendaController;
import com.hospital.platform.agenda.dto.AgendaErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AgendaController.class)
public class AgendaExceptionHandler {

    private static final String SCHEDULE_SPECIALTY_FOREIGN_KEY = "schedules_specialty_id_fkey";

    @ExceptionHandler(AgendaNotFoundException.class)
    ResponseEntity<AgendaErrorResponseDTO> handleAgendaNotFound(AgendaNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "Agenda not found", "AGENDA_NOT_FOUND");
    }

    @ExceptionHandler(AvailabilitySlotNotFoundException.class)
    ResponseEntity<AgendaErrorResponseDTO> handleAvailabilitySlotNotFound(
            AvailabilitySlotNotFoundException exception
    ) {
        return error(HttpStatus.NOT_FOUND, "Availability slot not found", "AVAILABILITY_SLOT_NOT_FOUND");
    }

    @ExceptionHandler(ProfessionalNotAvailableException.class)
    ResponseEntity<AgendaErrorResponseDTO> handleProfessionalNotAvailable(
            ProfessionalNotAvailableException exception
    ) {
        return error(HttpStatus.NOT_FOUND, "Active professional not found", "PROFESSIONAL_NOT_AVAILABLE");
    }

    @ExceptionHandler(InvalidScheduleTimeException.class)
    ResponseEntity<AgendaErrorResponseDTO> handleInvalidScheduleTime(InvalidScheduleTimeException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), "INVALID_SCHEDULE_TIME");
    }

    @ExceptionHandler(InactiveAgendaException.class)
    ResponseEntity<AgendaErrorResponseDTO> handleInactiveAgenda(InactiveAgendaException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), "AGENDA_INACTIVE");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<AgendaErrorResponseDTO> handleInvalidAssociation(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), "INVALID_PROFESSIONAL_SPECIALTY_ASSOCIATION");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<AgendaErrorResponseDTO> handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        if (!isSpecialtyForeignKeyViolation(exception)) {
            return error(HttpStatus.CONFLICT, "Schedule conflicts with active or protected capacity",
                    "AGENDA_CAPACITY_CONFLICT");
        }
        return error(
                HttpStatus.BAD_REQUEST,
                "Specialty reference is invalid",
                "INVALID_SPECIALTY_REFERENCE"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<AgendaErrorResponseDTO> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        return error(HttpStatus.BAD_REQUEST, "Request validation failed", "VALIDATION_ERROR");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<AgendaErrorResponseDTO> handleConstraintViolation(ConstraintViolationException exception) {
        return error(HttpStatus.BAD_REQUEST, "Request validation failed", "VALIDATION_ERROR");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<AgendaErrorResponseDTO> handleAccessDenied(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "Access denied", "ACCESS_DENIED");
    }

    private ResponseEntity<AgendaErrorResponseDTO> error(HttpStatus status, String message, String errorCode) {
        return ResponseEntity.status(status)
                .body(new AgendaErrorResponseDTO(false, message, errorCode, Instant.now()));
    }

    private boolean isSpecialtyForeignKeyViolation(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof org.hibernate.exception.ConstraintViolationException constraintViolation
                    && SCHEDULE_SPECIALTY_FOREIGN_KEY.equals(constraintViolation.getConstraintName())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
