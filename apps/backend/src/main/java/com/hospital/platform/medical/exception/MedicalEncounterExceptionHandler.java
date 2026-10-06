package com.hospital.platform.medical.exception;

import com.hospital.platform.appointments.dto.AppointmentErrorResponseDTO;
import com.hospital.platform.appointments.exception.AppointmentNotFoundException;
import com.hospital.platform.appointments.exception.InvalidAppointmentTransitionException;
import com.hospital.platform.medical.controller.MedicalEncounterController;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = MedicalEncounterController.class)
public class MedicalEncounterExceptionHandler {

    @ExceptionHandler(AppointmentNotFoundException.class)
    ResponseEntity<AppointmentErrorResponseDTO> notFound(AppointmentNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "Appointment not found", "APPOINTMENT_NOT_FOUND");
    }

    @ExceptionHandler(InvalidAppointmentTransitionException.class)
    ResponseEntity<AppointmentErrorResponseDTO> invalidTransition(InvalidAppointmentTransitionException exception) {
        return error(HttpStatus.CONFLICT, "Appointment transition is not allowed", "INVALID_APPOINTMENT_TRANSITION");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<AppointmentErrorResponseDTO> accessDenied(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "Medical encounter access denied", "MEDICAL_ENCOUNTER_ACCESS_DENIED");
    }

    private ResponseEntity<AppointmentErrorResponseDTO> error(HttpStatus status, String message, String code) {
        return ResponseEntity.status(status)
                .body(new AppointmentErrorResponseDTO(false, message, code, Instant.now()));
    }
}
