package com.hospital.platform.agenda.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.platform.agenda.dto.AgendaResponseDTO;
import com.hospital.platform.agenda.dto.AgendaPublicationResponseDTO;
import com.hospital.platform.agenda.exception.AgendaExceptionHandler;
import com.hospital.platform.agenda.exception.AgendaNotFoundException;
import com.hospital.platform.agenda.exception.InactiveAgendaException;
import com.hospital.platform.agenda.service.AgendaService;
import java.time.LocalTime;
import java.util.List;
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
class AgendaControllerTest {

    private static final UUID AGENDA_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SPECIALTY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Mock
    private AgendaService agendaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AgendaController(agendaService))
                .setControllerAdvice(new AgendaExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createsAgendaAndReturnsCreatedResponse() throws Exception {
        when(agendaService.createAgenda(any())).thenReturn(response());
        String request = """
                {
                  "professionalId": "%s",
                  "specialtyId": "%s",
                  "dayOfWeek": 1,
                  "startTime": "09:00:00",
                  "endTime": "12:00:00"
                }
                """.formatted(PROFESSIONAL_ID, SPECIALTY_ID);

        mockMvc.perform(post("/agendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/agendas/" + AGENDA_ID))
                .andExpect(jsonPath("$.id").value(AGENDA_ID.toString()));
    }

    @Test
    void rejectsInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/agendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void returnsAgendaNotFoundError() throws Exception {
        when(agendaService.findAgendaById(AGENDA_ID)).thenThrow(new AgendaNotFoundException(AGENDA_ID));

        mockMvc.perform(get("/agendas/{id}", AGENDA_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("AGENDA_NOT_FOUND"));
    }

    @Test
    void publishesSlotsAndReturnsCreatedCount() throws Exception {
        when(agendaService.publishAgenda(AGENDA_ID))
                .thenReturn(new AgendaPublicationResponseDTO(AGENDA_ID, 12, 14));

        mockMvc.perform(post("/agendas/{id}/publish", AGENDA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value(AGENDA_ID.toString()))
                .andExpect(jsonPath("$.createdSlots").value(12))
                .andExpect(jsonPath("$.horizonDays").value(14));
    }

    @Test
    void rejectsPublicationOfInactiveAgenda() throws Exception {
        when(agendaService.publishAgenda(AGENDA_ID)).thenThrow(new InactiveAgendaException());

        mockMvc.perform(post("/agendas/{id}/publish", AGENDA_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("AGENDA_INACTIVE"));
    }

    @Test
    void returnsAvailabilityResponse() throws Exception {
        when(agendaService.findAvailability(null, PROFESSIONAL_ID, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/availability").param("professionalId", PROFESSIONAL_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private AgendaResponseDTO response() {
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
}
