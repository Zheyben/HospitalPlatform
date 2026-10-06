package com.hospital.platform.patients.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.hospital.platform.patients.dto.PatientResponseDTO;
import com.hospital.platform.patients.dto.UpdatePatientDemographicsRequestDTO;
import com.hospital.platform.patients.service.PatientService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(PatientControllerAuthorizationTest.MethodSecurityTestConfiguration.class)
class PatientControllerAuthorizationTest {

    @Autowired
    private PatientController patientController;

    @Autowired
    private PatientService patientService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAdminPatientManagementAccess() {
        when(patientService.findPatients()).thenReturn(List.of());

        assertThat(patientController.findPatients()).isEmpty();
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void allowsPatientToReadOwnProfile() {
        PatientResponseDTO response = new PatientResponseDTO(
                null, null, "DNI", "12345678", null, null, null, null, null, true);
        when(patientService.findCurrentPatient()).thenReturn(response);

        assertThat(patientController.findCurrentPatient()).isSameAs(response);
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void rejectsUserWithoutPatientPermissions() {
        assertThatThrownBy(() -> patientController.findPatients())
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> patientController.findCurrentPatient())
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> patientController.updateDemographics(null,
                new UpdatePatientDemographicsRequestDTO(null, null, null, null,
                        null, null, null, null)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityTestConfiguration {

        @Bean
        PatientService patientService() {
            return Mockito.mock(PatientService.class);
        }

        @Bean
        PatientController patientController(PatientService patientService) {
            return new PatientController(patientService);
        }
    }
}
