package com.hospital.platform.agenda.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import com.hospital.platform.agenda.dto.AgendaResponseDTO;
import com.hospital.platform.agenda.dto.AvailabilitySlotResponseDTO;
import com.hospital.platform.agenda.dto.CreateAgendaRequestDTO;
import com.hospital.platform.agenda.dto.UpdateAgendaRequestDTO;
import com.hospital.platform.agenda.dto.UpdateAgendaStatusRequestDTO;
import com.hospital.platform.agenda.entity.AvailabilitySlotStatus;
import com.hospital.platform.agenda.service.AgendaService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@SpringJUnitConfig(AgendaControllerAuthorizationTest.MethodSecurityTestConfiguration.class)
class AgendaControllerAuthorizationTest {

    private static final UUID AGENDA_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SPECIALTY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Autowired
    private AgendaController agendaController;

    @Autowired
    private AgendaService agendaService;

    @BeforeEach
    void setUpRequestContext() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAdminAccessToEveryAgendaEndpoint() {
        when(agendaService.createAgenda(any())).thenReturn(agendaResponse());
        when(agendaService.findAgendas(null, null)).thenReturn(List.of());
        when(agendaService.findAgendaById(AGENDA_ID)).thenReturn(agendaResponse());
        when(agendaService.updateAgenda(any(), any())).thenReturn(agendaResponse());
        when(agendaService.changeAgendaStatus(any(), anyBoolean())).thenReturn(agendaResponse());
        when(agendaService.findAvailability(null, null, null, null)).thenReturn(List.of());
        when(agendaService.findAvailabilitySlotById(SLOT_ID)).thenReturn(slotResponse());

        assertThat(agendaController.findAgendas(null, null)).isEmpty();
        assertThatCode(() -> agendaController.createAgenda(createRequest())).doesNotThrowAnyException();
        assertThatCode(() -> agendaController.findAgendaById(AGENDA_ID)).doesNotThrowAnyException();
        assertThatCode(() -> agendaController.updateAgenda(AGENDA_ID, updateRequest())).doesNotThrowAnyException();
        assertThatCode(() -> agendaController.changeAgendaStatus(
                AGENDA_ID,
                new UpdateAgendaStatusRequestDTO(false)
        )).doesNotThrowAnyException();
        assertThatCode(() -> agendaController.findAvailability(null, null, null, null))
                .doesNotThrowAnyException();
        assertThatCode(() -> agendaController.findAvailabilitySlotById(SLOT_ID)).doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(roles = "PROFESSIONAL")
    void rejectsUnauthorizedRoleFromEveryAgendaEndpoint() {
        assertThatThrownBy(() -> agendaController.createAgenda(createRequest()))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> agendaController.findAgendas(null, null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> agendaController.findAgendaById(AGENDA_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> agendaController.updateAgenda(AGENDA_ID, updateRequest()))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> agendaController.changeAgendaStatus(
                AGENDA_ID,
                new UpdateAgendaStatusRequestDTO(false)
        )).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> agendaController.findAvailability(null, null, null, null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> agendaController.findAvailabilitySlotById(SLOT_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    private CreateAgendaRequestDTO createRequest() {
        return new CreateAgendaRequestDTO(
                PROFESSIONAL_ID,
                SPECIALTY_ID,
                1,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );
    }

    private UpdateAgendaRequestDTO updateRequest() {
        return new UpdateAgendaRequestDTO(
                PROFESSIONAL_ID,
                SPECIALTY_ID,
                1,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );
    }

    private AgendaResponseDTO agendaResponse() {
        return new AgendaResponseDTO(
                AGENDA_ID,
                PROFESSIONAL_ID,
                SPECIALTY_ID,
                1,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                true
        );
    }

    private AvailabilitySlotResponseDTO slotResponse() {
        return new AvailabilitySlotResponseDTO(
                SLOT_ID,
                AGENDA_ID,
                LocalDate.of(2026, 10, 1),
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                AvailabilitySlotStatus.AVAILABLE,
                true
        );
    }

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityTestConfiguration {

        @Bean
        AgendaService agendaService() {
            return Mockito.mock(AgendaService.class);
        }

        @Bean
        AgendaController agendaController(AgendaService agendaService) {
            return new AgendaController(agendaService);
        }
    }
}
