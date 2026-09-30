package com.hospital.platform.patients.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePatientStatusRequestDTO(
        @NotNull
        PatientStatus status
) {
}
