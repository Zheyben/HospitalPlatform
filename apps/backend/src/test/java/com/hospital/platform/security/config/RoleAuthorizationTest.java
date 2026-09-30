package com.hospital.platform.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.beans.factory.annotation.Autowired;

@SpringJUnitConfig(RoleAuthorizationTest.MethodSecurityTestConfiguration.class)
class RoleAuthorizationTest {

    @Autowired
    private AdminOnlyService adminOnlyService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAdminRole() {
        assertThat(adminOnlyService.adminOnly()).isEqualTo("ok");
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void rejectsUnauthorizedRole() {
        assertThatThrownBy(() -> adminOnlyService.adminOnly())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityTestConfiguration {

        @Bean
        AdminOnlyService adminOnlyService() {
            return new AdminOnlyService();
        }
    }

    static class AdminOnlyService {

        @PreAuthorize("hasRole('ADMIN')")
        String adminOnly() {
            return "ok";
        }
    }
}
