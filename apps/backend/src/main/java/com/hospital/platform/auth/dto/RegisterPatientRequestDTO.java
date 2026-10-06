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
import java.util.UUID;

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
        @Size(max = 150) String insurance,
        @Size(max = 500) String address,
        @Size(max = 50) String sex,
        UUID insuranceId,
        @Size(max = 80) String maritalStatus,
        @Size(max = 120) String occupation,
        @Size(max = 120) String district,
        @Size(max = 120) String educationLevel,
        @Size(max = 60) String affiliationNumber,
        @Size(max = 150) String emergencyContactName,
        @Size(max = 80) String emergencyContactRelationship,
        @Size(max = 50) @Pattern(regexp = "^\\+?[0-9]{7,15}$") String emergencyContactPhone
) {
    public RegisterPatientRequestDTO(String email, String password, String documentType, String documentNumber,
                                     String firstName, String lastName, LocalDate birthDate, String phone,
                                     String insurance, String address, String sex) {
        this(email, password, documentType, documentNumber, firstName, lastName, birthDate,
                phone, insurance, address, sex, null, null, null, null, null, null, null, null, null);
    }

    public RegisterPatientRequestDTO(String email, String password, String documentType, String documentNumber,
                                     String firstName, String lastName, LocalDate birthDate, String phone,
                                     String insurance, String address, String sex, UUID insuranceId) {
        this(email, password, documentType, documentNumber, firstName, lastName, birthDate,
                phone, insurance, address, sex, insuranceId, null, null, null, null, null, null, null, null);
    }

    @AssertTrue(message = "Document type or number is invalid")
    public boolean isDocumentIdentityValid() {
        return DocumentIdentity.isValid(documentType, documentNumber);
    }

    @AssertTrue(message = "Insurance selection is required")
    public boolean isInsuranceSelectionPresent() {
        return insuranceId != null || (insurance != null && !insurance.isBlank());
    }

    @JsonAnySetter
    public void rejectUnknownField(String name, Object value) {
        throw new IllegalArgumentException("Unknown registration field: " + name);
    }
}
