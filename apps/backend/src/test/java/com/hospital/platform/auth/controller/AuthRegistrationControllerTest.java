package com.hospital.platform.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.platform.auth.dto.RegisterPatientRequestDTO;
import com.hospital.platform.auth.dto.RegisterPatientResponseDTO;
import com.hospital.platform.auth.exception.AuthExceptionHandler;
import com.hospital.platform.auth.service.AuthService;
import com.hospital.platform.auth.service.PatientRegistrationService;
import com.hospital.platform.patients.exception.DuplicateDocumentException;
import com.hospital.platform.users.exception.DuplicateEmailException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class AuthRegistrationControllerTest {

    @Mock private AuthService authService;
    @Mock private PatientRegistrationService registrationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService, registrationService))
                .setControllerAdvice(new AuthExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void registrationReturns201AndOnlySafeFields() throws Exception {
        when(registrationService.register(any(RegisterPatientRequestDTO.class)))
                .thenReturn(new RegisterPatientResponseDTO(
                        UUID.randomUUID(), UUID.randomUUID(), "patient@example.com", "Ana", "Pérez",
                        "DNI", "12345678", LocalDate.of(1990, 1, 1), "3001234567", "SIS", null, null, null
                ));

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(request()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("patient@example.com"))
                .andExpect(jsonPath("$.insurance").value("SIS"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        when(registrationService.register(any(RegisterPatientRequestDTO.class)))
                .thenThrow(new DuplicateEmailException("patient@example.com"));

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(request()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void duplicateDocumentReturns409() throws Exception {
        when(registrationService.register(any(RegisterPatientRequestDTO.class)))
                .thenThrow(new DuplicateDocumentException("12345678"));

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(request()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_DOCUMENT"));
    }

    @Test
    void invalidDataAndClientSuppliedRolesReturn400() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(request().replace("\"firstName\":\"Ana\"", "\"firstName\":\"\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(request().replaceFirst("}\\s*$", ",\"roles\":[\"ADMIN\"]}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        verifyNoInteractions(registrationService);
    }

    private String request() {
        return """
                {"email":"patient@example.com","password":"strong-password","documentType":"DNI",
                 "documentNumber":"12345678","firstName":"Ana","lastName":"Pérez",
                 "birthDate":"1990-01-01","phone":"3001234567","insurance":"SIS"}
                """;
    }
}
