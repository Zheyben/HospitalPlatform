package com.hospital.platform.patients.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreatePatientRequestDTO(
        @NotBlank
        @Size(max = 50)
        String documentType,

        @NotBlank
        @Size(max = 50)
        String documentNumber,

        @Past
        LocalDate birthDate,

        @Size(max = 50)
        String phone,

        String address
) {
}
