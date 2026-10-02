package com.hospital.platform.patients.dto;

import com.hospital.platform.patients.domain.DocumentIdentity;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdatePatientRequestDTO(
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
    @AssertTrue(message = "Document type or number is invalid")
    public boolean isDocumentIdentityValid() {
        return DocumentIdentity.isValid(documentType, documentNumber);
    }
}
