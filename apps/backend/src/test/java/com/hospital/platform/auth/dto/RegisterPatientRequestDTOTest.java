package com.hospital.platform.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RegisterPatientRequestDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsValidRegistrationData() {
        assertThat(validator.validate(request(
                "patient@example.com", "Ana", "Pérez", LocalDate.of(1990, 1, 1),
                "+573001234567", "Demo Health"
        ))).isEmpty();
    }

    @Test
    void rejectsInvalidEmailNamesBirthDatePhoneAndInsurance() {
        var violations = validator.validate(request(
                "not-an-email", " ", " ", LocalDate.now().plusDays(1), "abc", " "
        ));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("email", "firstName", "lastName", "birthDate", "phone", "insurance");
    }

    private RegisterPatientRequestDTO request(
            String email, String firstName, String lastName, LocalDate birthDate, String phone, String insurance
    ) {
        return new RegisterPatientRequestDTO(
                email, "strong-password", "DNI", "12345678", firstName, lastName,
                birthDate, phone, insurance
        );
    }
}
