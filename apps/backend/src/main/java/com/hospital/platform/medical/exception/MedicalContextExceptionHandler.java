package com.hospital.platform.medical.exception;

import com.hospital.platform.appointments.dto.AppointmentErrorResponseDTO;
import com.hospital.platform.medical.controller.MedicalContextController;
import com.hospital.platform.medical.controller.MedicalLongitudinalController;
import com.hospital.platform.medical.service.MedicalAppointmentNotFoundException;
import com.hospital.platform.medical.service.InvalidMedicalHistoryPageException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {MedicalContextController.class, MedicalLongitudinalController.class})
public class MedicalContextExceptionHandler {

    @ExceptionHandler(MedicalAppointmentNotFoundException.class)
    ResponseEntity<AppointmentErrorResponseDTO> notFound(MedicalAppointmentNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AppointmentErrorResponseDTO(
                false, "Medical appointment not found", "MEDICAL_APPOINTMENT_NOT_FOUND", Instant.now()));
    }

    @ExceptionHandler(InvalidMedicalHistoryPageException.class)
    ResponseEntity<AppointmentErrorResponseDTO> invalidPage(InvalidMedicalHistoryPageException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AppointmentErrorResponseDTO(
                false, "Invalid medical history page", "INVALID_MEDICAL_HISTORY_PAGE", Instant.now()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<AppointmentErrorResponseDTO> accessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AppointmentErrorResponseDTO(
                false, "Medical context access denied", "MEDICAL_CONTEXT_ACCESS_DENIED", Instant.now()));
    }
}
