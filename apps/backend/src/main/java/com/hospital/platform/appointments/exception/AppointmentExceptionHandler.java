package com.hospital.platform.appointments.exception;

import com.hospital.platform.appointments.controller.AppointmentController;
import com.hospital.platform.appointments.dto.AppointmentErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = AppointmentController.class)
public class AppointmentExceptionHandler {

    @ExceptionHandler(AppointmentNotFoundException.class)
    ResponseEntity<AppointmentErrorResponseDTO> handleAppointmentNotFound(AppointmentNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "Appointment not found", "APPOINTMENT_NOT_FOUND");
    }

    @ExceptionHandler(PatientNotAvailableException.class)
    ResponseEntity<AppointmentErrorResponseDTO> handlePatientNotAvailable(PatientNotAvailableException exception) {
        return error(HttpStatus.NOT_FOUND, "Active patient not found", "PATIENT_NOT_AVAILABLE");
    }

    @ExceptionHandler(ProfessionalNotAvailableException.class)
    ResponseEntity<AppointmentErrorResponseDTO> handleProfessionalNotAvailable(
            ProfessionalNotAvailableException exception
    ) {
        return error(HttpStatus.CONFLICT, "Active professional not found", "PROFESSIONAL_NOT_AVAILABLE");
    }

    @ExceptionHandler(SlotUnavailableException.class)
    ResponseEntity<AppointmentErrorResponseDTO> handleSlotUnavailable(SlotUnavailableException exception) {
        return error(HttpStatus.CONFLICT, "Availability slot is unavailable", "SLOT_UNAVAILABLE");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<AppointmentErrorResponseDTO> handleCapacityViolation(DataIntegrityViolationException exception) {
        String detail = exception.getMostSpecificCause().getMessage();
        if (detail != null && detail.contains("Appointment missing")) {
            return error(HttpStatus.NOT_FOUND, "Appointment not found", "APPOINTMENT_NOT_FOUND");
        }
        if (detail != null && (detail.contains("SLOT_UNAVAILABLE") || detail.contains("New slot missing"))) {
            return error(HttpStatus.CONFLICT, "Availability slot is unavailable", "SLOT_UNAVAILABLE");
        }
        return error(HttpStatus.CONFLICT, "Appointment operation conflicts with current capacity or state",
                "APPOINTMENT_CAPACITY_CONFLICT");
    }

    @ExceptionHandler(InvalidAppointmentTransitionException.class)
    ResponseEntity<AppointmentErrorResponseDTO> handleInvalidAppointmentTransition(
            InvalidAppointmentTransitionException exception
    ) {
        return error(
                HttpStatus.CONFLICT,
                "Appointment transition is not allowed",
                "INVALID_APPOINTMENT_TRANSITION"
        );
    }

    @ExceptionHandler(AppointmentSuccessorExistsException.class)
    ResponseEntity<AppointmentErrorResponseDTO> handleAppointmentSuccessorExists(
            AppointmentSuccessorExistsException exception
    ) {
        return error(
                HttpStatus.CONFLICT,
                "Appointment already has a rescheduled successor",
                "APPOINTMENT_SUCCESSOR_EXISTS"
        );
    }

    @ExceptionHandler(InvalidSlotProfessionalException.class)
    ResponseEntity<AppointmentErrorResponseDTO> handleInvalidSlotProfessional(
            InvalidSlotProfessionalException exception
    ) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), "INVALID_SLOT_PROFESSIONAL");
    }

    @ExceptionHandler(InvalidAppointmentRequestException.class)
    ResponseEntity<AppointmentErrorResponseDTO> handleInvalidAppointmentRequest(
            InvalidAppointmentRequestException exception
    ) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), "INVALID_APPOINTMENT_REQUEST");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<AppointmentErrorResponseDTO> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        return error(HttpStatus.BAD_REQUEST, "Request validation failed", "VALIDATION_ERROR");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<AppointmentErrorResponseDTO> handleConstraintViolation(ConstraintViolationException exception) {
        return error(HttpStatus.BAD_REQUEST, "Request validation failed", "VALIDATION_ERROR");
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    ResponseEntity<AppointmentErrorResponseDTO> handleMalformedRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "Request validation failed", "VALIDATION_ERROR");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<AppointmentErrorResponseDTO> handleAccessDenied(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "Appointment access denied", "APPOINTMENT_ACCESS_DENIED");
    }

    private ResponseEntity<AppointmentErrorResponseDTO> error(
            HttpStatus status,
            String message,
            String errorCode
    ) {
        return ResponseEntity.status(status)
                .body(new AppointmentErrorResponseDTO(false, message, errorCode, Instant.now()));
    }
}
