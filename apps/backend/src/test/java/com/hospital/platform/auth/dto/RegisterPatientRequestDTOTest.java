package com.hospital.platform.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegisterPatientRequestDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsValidRegistrationData() {
        assertThat(validator.validate(request(
                "patient@example.com", "Ana", "Pérez", LocalDate.of(1990, 1, 1),
                "+573001234567", "SIS"
        ))).isEmpty();
    }

    @Test
    void rejectsInvalidEmailNamesBirthDatePhoneAndInsurance() {
        var violations = validator.validate(request(
                "not-an-email", " ", " ", LocalDate.now().plusDays(1), "abc", " "
        ));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("email", "firstName", "lastName", "birthDate", "phone", "insuranceSelectionPresent");
    }

    @Test
    void acceptsInsuranceIdWithoutLegacyTextAndRejectsMissingSelection() {
        RegisterPatientRequestDTO byId = new RegisterPatientRequestDTO(
                "patient@example.com", "strong-password", "DNI", "12345678", "Ana", "Perez",
                LocalDate.of(1990, 1, 1), "3001234567", null, null, null, UUID.randomUUID());
        assertThat(validator.validate(byId)).isEmpty();
        assertThat(validator.validate(request("patient@example.com", "Ana", "Perez",
                LocalDate.of(1990, 1, 1), "3001234567", null)))
                .extracting(v -> v.getPropertyPath().toString()).contains("insuranceSelectionPresent");
    }

    private RegisterPatientRequestDTO request(
            String email, String firstName, String lastName, LocalDate birthDate, String phone, String insurance
    ) {
        return new RegisterPatientRequestDTO(
                email, "strong-password", "DNI", "12345678", firstName, lastName,
                birthDate, phone, insurance, null, null
        );
    }
}
