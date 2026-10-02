package com.hospital.platform.auth.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.hospital.platform.patients.domain.DocumentIdentity;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterPatientRequestDTO(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotBlank @Size(max = 50) String documentType,
        @NotBlank @Size(max = 50) String documentNumber,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull @Past LocalDate birthDate,
        @NotBlank @Size(max = 50)
        @Pattern(regexp = "^\\+?[0-9]{7,15}$") String phone,
        @NotBlank @Size(max = 150) String insurance
) {
    @AssertTrue(message = "Document type or number is invalid")
    public boolean isDocumentIdentityValid() {
        return DocumentIdentity.isValid(documentType, documentNumber);
    }

    @JsonAnySetter
    public void rejectUnknownField(String name, Object value) {
        throw new IllegalArgumentException("Unknown registration field: " + name);
    }
}
