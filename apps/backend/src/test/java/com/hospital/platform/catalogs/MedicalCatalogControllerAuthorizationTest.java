package com.hospital.platform.catalogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.hospital.platform.catalogs.controller.MedicalCatalogController;
import com.hospital.platform.catalogs.service.MedicalCatalogService;
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

@SpringJUnitConfig(MedicalCatalogControllerAuthorizationTest.MethodSecurityConfiguration.class)
class MedicalCatalogControllerAuthorizationTest {

    @Autowired private MedicalCatalogController controller;
    @Autowired private MedicalCatalogService service;

    @Test
    @WithMockUser(roles = "PROFESSIONAL")
    void allowsProfessionalSearch() {
        when(service.searchIcd10("te", 20)).thenReturn(List.of());

        assertThat(controller.searchIcd10("te", 20)).isEmpty();
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void rejectsReceptionist() {
        assertThatThrownBy(() -> controller.searchMedications("te", 20))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsAdmin() {
        assertThatThrownBy(() -> controller.searchProcedures("te", 20))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void rejectsPatient() {
        assertThatThrownBy(() -> controller.searchIcd10("te", 20))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityConfiguration {
        @Bean
        MedicalCatalogService medicalCatalogService() {
            return Mockito.mock(MedicalCatalogService.class);
        }

        @Bean
        MedicalCatalogController medicalCatalogController(MedicalCatalogService service) {
            return new MedicalCatalogController(service);
        }
    }
}
