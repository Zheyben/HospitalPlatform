package com.hospital.platform.medical.exception;

import com.hospital.platform.appointments.dto.AppointmentErrorResponseDTO;
import com.hospital.platform.medical.controller.PatientClinicalReadController;
import com.hospital.platform.medical.service.InvalidPatientClinicalPageException;
import com.hospital.platform.medical.service.PatientClinicalRecordNotFoundException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PatientClinicalReadController.class)
public class PatientClinicalReadExceptionHandler {

    @ExceptionHandler(InvalidPatientClinicalPageException.class)
    ResponseEntity<AppointmentErrorResponseDTO> invalidPage(InvalidPatientClinicalPageException exception) {
        return error(HttpStatus.BAD_REQUEST, "Invalid clinical page", "INVALID_CLINICAL_PAGE");
    }

    @ExceptionHandler(PatientClinicalRecordNotFoundException.class)
    ResponseEntity<AppointmentErrorResponseDTO> notFound(PatientClinicalRecordNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "Clinical record not found", "CLINICAL_RECORD_NOT_FOUND");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<AppointmentErrorResponseDTO> denied(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "Clinical record access denied", "CLINICAL_RECORD_ACCESS_DENIED");
    }

    private ResponseEntity<AppointmentErrorResponseDTO> error(HttpStatus status, String message, String code) {
        return ResponseEntity.status(status)
                .body(new AppointmentErrorResponseDTO(false, message, code, Instant.now()));
    }
}
