package com.hospital.platform.users.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.hospital.platform.users.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.mockito.Mockito;

@SpringJUnitConfig(UserControllerAuthorizationTest.MethodSecurityTestConfiguration.class)
class UserControllerAuthorizationTest {

    @Autowired
    private UserController userController;

    @Autowired
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAdminUserManagementAccess() {
        when(userService.findUsers()).thenReturn(List.of());

        assertThat(userController.findUsers()).isEmpty();
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void rejectsNormalUserFromAdminUserManagementAccess() {
        assertThatThrownBy(() -> userController.findUsers())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityTestConfiguration {

        @Bean
        UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        UserController userController(UserService userService) {
            return new UserController(userService);
        }
    }
}
