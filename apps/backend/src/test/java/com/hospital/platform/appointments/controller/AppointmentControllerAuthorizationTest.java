package com.hospital.platform.appointments.controller;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hospital.platform.appointments.dto.AppointmentResponseDTO;
import com.hospital.platform.appointments.dto.CreateAppointmentRequestDTO;
import com.hospital.platform.appointments.dto.RescheduleAppointmentRequestDTO;
import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.service.AppointmentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@SpringJUnitConfig(AppointmentControllerAuthorizationTest.MethodSecurityTestConfiguration.class)
class AppointmentControllerAuthorizationTest {

    private static final UUID APPOINTMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID NEW_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222223");
    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID PATIENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    @Autowired
    private AppointmentController appointmentController;

    @Autowired
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        when(appointmentService.createAppointment(any())).thenReturn(response());
        when(appointmentService.findAppointments()).thenReturn(List.of(response()));
        when(appointmentService.findAppointmentById(APPOINTMENT_ID)).thenReturn(response());
        when(appointmentService.confirmAppointment(APPOINTMENT_ID)).thenReturn(response());
        when(appointmentService.cancelAppointment(APPOINTMENT_ID)).thenReturn(response());
        when(appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID)).thenReturn(response());
        when(appointmentService.checkInAppointment(APPOINTMENT_ID)).thenReturn(response());
        when(appointmentService.moveAppointmentToWaiting(APPOINTMENT_ID)).thenReturn(response());
        when(appointmentService.startAppointmentAttention(APPOINTMENT_ID)).thenReturn(response());
        when(appointmentService.completeAppointment(APPOINTMENT_ID)).thenReturn(response());
    }

    @AfterEach
    void cleanUp() {
        RequestContextHolder.resetRequestAttributes();
        Mockito.reset(appointmentService);
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void allowsPatientRole() {
        assertEveryEndpointIsAllowed(new CreateAppointmentRequestDTO(SLOT_ID, null, "Control"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAdminRole() {
        assertEveryEndpointIsAllowed(new CreateAppointmentRequestDTO(SLOT_ID, PATIENT_ID, "Control"));
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void allowsReceptionistRole() {
        assertEveryEndpointIsAllowed(new CreateAppointmentRequestDTO(SLOT_ID, PATIENT_ID, "Control"));
    }

    @Test
    @WithMockUser(roles = "PROFESSIONAL")
    void rejectsProfessionalRole() {
        CreateAppointmentRequestDTO request = new CreateAppointmentRequestDTO(SLOT_ID, PATIENT_ID, "Control");

        assertThatThrownBy(() -> appointmentController.createAppointment(request))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> appointmentController.findAppointments())
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> appointmentController.findAppointmentById(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> appointmentController.confirmAppointment(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> appointmentController.cancelAppointment(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> appointmentController.rescheduleAppointment(
                APPOINTMENT_ID,
                new RescheduleAppointmentRequestDTO(NEW_SLOT_ID)
        )).isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> appointmentController.checkInAppointment(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> appointmentController.moveAppointmentToWaiting(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatCode(() -> appointmentController.startAppointmentAttention(APPOINTMENT_ID))
                .doesNotThrowAnyException();
        assertThatCode(() -> appointmentController.completeAppointment(APPOINTMENT_ID))
                .doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void allowsReceptionistOperationsAndRejectsProfessionalOperations() {
        assertThatCode(() -> appointmentController.checkInAppointment(APPOINTMENT_ID))
                .doesNotThrowAnyException();
        assertThatCode(() -> appointmentController.moveAppointmentToWaiting(APPOINTMENT_ID))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> appointmentController.startAppointmentAttention(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> appointmentController.completeAppointment(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsAdminFromAllOperationalEndpoints() {
        assertEveryOperationalEndpointIsDenied();
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void rejectsPatientFromAllOperationalEndpoints() {
        assertEveryOperationalEndpointIsDenied();
    }

    private void assertEveryEndpointIsAllowed(CreateAppointmentRequestDTO request) {
        assertThatCode(() -> appointmentController.createAppointment(request)).doesNotThrowAnyException();
        assertThatCode(() -> appointmentController.findAppointments()).doesNotThrowAnyException();
        assertThatCode(() -> appointmentController.findAppointmentById(APPOINTMENT_ID))
                .doesNotThrowAnyException();
        assertThatCode(() -> appointmentController.confirmAppointment(APPOINTMENT_ID)).doesNotThrowAnyException();
        assertThatCode(() -> appointmentController.cancelAppointment(APPOINTMENT_ID)).doesNotThrowAnyException();
        assertThatCode(() -> appointmentController.rescheduleAppointment(
                APPOINTMENT_ID,
                new RescheduleAppointmentRequestDTO(NEW_SLOT_ID)
        )).doesNotThrowAnyException();
    }

    private void assertEveryOperationalEndpointIsDenied() {
        assertThatThrownBy(() -> appointmentController.checkInAppointment(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> appointmentController.moveAppointmentToWaiting(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> appointmentController.startAppointmentAttention(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> appointmentController.completeAppointment(APPOINTMENT_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    private AppointmentResponseDTO response() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 1, 8, 0);
        return new AppointmentResponseDTO(
                APPOINTMENT_ID,
                PATIENT_ID,
                PROFESSIONAL_ID,
                SLOT_ID,
                AppointmentStatus.SCHEDULED,
                null,
                "Control",
                null,
                null,
                now,
                now
        );
    }

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityTestConfiguration {

        @Bean
        AppointmentService appointmentService() {
            return Mockito.mock(AppointmentService.class);
        }

        @Bean
        AppointmentController appointmentController(AppointmentService appointmentService) {
            return new AppointmentController(appointmentService);
        }
    }
}
