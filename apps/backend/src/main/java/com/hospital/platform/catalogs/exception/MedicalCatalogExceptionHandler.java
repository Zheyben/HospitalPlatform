package com.hospital.platform.catalogs.exception;

import com.hospital.platform.appointments.dto.AppointmentErrorResponseDTO;
import com.hospital.platform.catalogs.controller.MedicalCatalogController;
import com.hospital.platform.catalogs.service.InvalidMedicalCatalogQueryException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = MedicalCatalogController.class)
public class MedicalCatalogExceptionHandler {

    @ExceptionHandler({InvalidMedicalCatalogQueryException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<AppointmentErrorResponseDTO> invalidQuery(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AppointmentErrorResponseDTO(
                false, "Invalid catalog query", "INVALID_CATALOG_QUERY", Instant.now()));
    }
}
