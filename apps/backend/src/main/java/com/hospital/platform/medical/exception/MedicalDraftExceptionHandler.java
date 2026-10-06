package com.hospital.platform.medical.exception;

import com.hospital.platform.appointments.dto.AppointmentErrorResponseDTO;
import com.hospital.platform.medical.controller.MedicalAssessmentDraftController;
import com.hospital.platform.medical.controller.MedicalHistoryDraftController;
import com.hospital.platform.medical.controller.MedicalPrescriptionDraftController;
import com.hospital.platform.medical.service.InvalidMedicalAssessmentDraftException;
import com.hospital.platform.medical.service.InvalidMedicalPrescriptionDraftException;
import com.hospital.platform.medical.service.MedicalDraftNotFoundException;
import com.hospital.platform.medical.service.MedicalDraftVersionConflictException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {MedicalHistoryDraftController.class,
        MedicalAssessmentDraftController.class, MedicalPrescriptionDraftController.class})
public class MedicalDraftExceptionHandler {

    @ExceptionHandler(MedicalDraftNotFoundException.class)
    ResponseEntity<AppointmentErrorResponseDTO> notFound(MedicalDraftNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "Medical draft not found", "MEDICAL_DRAFT_NOT_FOUND");
    }

    @ExceptionHandler(MedicalDraftVersionConflictException.class)
    ResponseEntity<AppointmentErrorResponseDTO> versionConflict(MedicalDraftVersionConflictException exception) {
        return error(HttpStatus.CONFLICT, "Medical draft version changed", "MEDICAL_DRAFT_VERSION_CONFLICT");
    }

    @ExceptionHandler({InvalidMedicalAssessmentDraftException.class, InvalidMedicalPrescriptionDraftException.class})
    ResponseEntity<AppointmentErrorResponseDTO> invalidReference(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "Medical draft selection is invalid", "INVALID_MEDICAL_DRAFT_REFERENCE");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<AppointmentErrorResponseDTO> invalidRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "Request validation failed", "VALIDATION_ERROR");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<AppointmentErrorResponseDTO> accessDenied(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "Medical draft access denied", "MEDICAL_DRAFT_ACCESS_DENIED");
    }

    private ResponseEntity<AppointmentErrorResponseDTO> error(HttpStatus status, String message, String code) {
        return ResponseEntity.status(status)
                .body(new AppointmentErrorResponseDTO(false, message, code, Instant.now()));
    }
}
