package com.hospital.platform.professionals.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.hospital.platform.professionals.dto.ProfessionalResponseDTO;
import com.hospital.platform.professionals.service.ProfessionalService;
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

@SpringJUnitConfig(ProfessionalControllerAuthorizationTest.MethodSecurityTestConfiguration.class)
class ProfessionalControllerAuthorizationTest {

    @Autowired
    private ProfessionalController professionalController;

    @Autowired
    private ProfessionalService professionalService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAdminProfessionalManagementAccess() {
        when(professionalService.findProfessionals()).thenReturn(List.of());

        assertThat(professionalController.findProfessionals()).isEmpty();
    }

    @Test
    @WithMockUser(roles = "PROFESSIONAL")
    void rejectsProfessionalRoleFromAdministrativeManagement() {
        assertThatThrownBy(() -> professionalController.findProfessionals())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityTestConfiguration {

        @Bean
        ProfessionalService professionalService() {
            return Mockito.mock(ProfessionalService.class);
        }

        @Bean
        ProfessionalController professionalController(ProfessionalService professionalService) {
            return new ProfessionalController(professionalService);
        }
    }
}
