package com.hospital.platform.medical.exception;

import com.hospital.platform.appointments.dto.AppointmentErrorResponseDTO;
import com.hospital.platform.appointments.exception.InvalidAppointmentTransitionException;
import com.hospital.platform.medical.controller.MedicalFinalizationController;
import com.hospital.platform.medical.service.InvalidMedicalFinalizationException;
import com.hospital.platform.medical.service.MedicalDraftNotFoundException;
import com.hospital.platform.medical.service.MedicalFinalizationConflictException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = MedicalFinalizationController.class)
public class MedicalFinalizationExceptionHandler {

    @ExceptionHandler(MedicalDraftNotFoundException.class)
    ResponseEntity<AppointmentErrorResponseDTO> notFound(MedicalDraftNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "Medical encounter not found", "MEDICAL_ENCOUNTER_NOT_FOUND");
    }

    @ExceptionHandler({InvalidMedicalFinalizationException.class,
            MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<AppointmentErrorResponseDTO> invalid(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "Clinical finalization data is invalid", "INVALID_MEDICAL_FINALIZATION");
    }

    @ExceptionHandler({MedicalFinalizationConflictException.class, InvalidAppointmentTransitionException.class})
    ResponseEntity<AppointmentErrorResponseDTO> conflict(Exception exception) {
        return error(HttpStatus.CONFLICT, "Medical encounter cannot be finalized", "MEDICAL_FINALIZATION_CONFLICT");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<AppointmentErrorResponseDTO> denied(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "Medical encounter access denied", "MEDICAL_ENCOUNTER_ACCESS_DENIED");
    }

    private ResponseEntity<AppointmentErrorResponseDTO> error(HttpStatus status, String message, String code) {
        return ResponseEntity.status(status)
                .body(new AppointmentErrorResponseDTO(false, message, code, Instant.now()));
    }
}
