package com.hospital.platform.appointments.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.platform.appointments.dto.AppointmentResponseDTO;
import com.hospital.platform.appointments.dto.ReceptionAppointmentSummaryDTO;
import com.hospital.platform.appointments.dto.ReceptionWaitingRoomDTO;
import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.entity.FlowStage;
import org.springframework.dao.DataIntegrityViolationException;
import com.hospital.platform.appointments.exception.AppointmentSuccessorExistsException;
import com.hospital.platform.appointments.exception.AppointmentExceptionHandler;
import com.hospital.platform.appointments.exception.AppointmentNotFoundException;
import com.hospital.platform.appointments.exception.InvalidAppointmentTransitionException;
import com.hospital.platform.appointments.exception.SlotUnavailableException;
import com.hospital.platform.appointments.service.AppointmentService;
import com.hospital.platform.medical.service.MedicalEncounterService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    private static final UUID APPOINTMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID NEW_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222223");
    private static final UUID SUCCESSOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111112");
    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID PATIENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private MedicalEncounterService medicalEncounterService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AppointmentController(appointmentService, medicalEncounterService))
                .setControllerAdvice(new AppointmentExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createsAppointmentAndReturnsCreatedResponse() throws Exception {
        when(appointmentService.createAppointment(any())).thenReturn(response());

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/appointments/" + APPOINTMENT_ID))
                .andExpect(jsonPath("$.appointmentStatus").value("SCHEDULED"))
                .andExpect(jsonPath("$.flowStage").doesNotExist());
    }

    @Test
    void rejectsCreateRequestWithoutSlotId() throws Exception {
        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Control\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void returnsAppointmentNotFoundError() throws Exception {
        when(appointmentService.findAppointmentById(APPOINTMENT_ID))
                .thenThrow(new AppointmentNotFoundException(APPOINTMENT_ID));

        mockMvc.perform(get("/appointments/{id}", APPOINTMENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("APPOINTMENT_NOT_FOUND"));
    }

    @Test
    void returnsConflictWhenSlotCannotBeReserved() throws Exception {
        when(appointmentService.createAppointment(any())).thenThrow(new SlotUnavailableException(SLOT_ID));

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("SLOT_UNAVAILABLE"));
    }

    @Test
    void returnsForbiddenWhenOwnershipValidationFails() throws Exception {
        when(appointmentService.findAppointmentById(APPOINTMENT_ID))
                .thenThrow(new AccessDeniedException("Appointment access denied"));

        mockMvc.perform(get("/appointments/{id}", APPOINTMENT_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("APPOINTMENT_ACCESS_DENIED"));
    }

    @Test
    void returnsAppointmentList() throws Exception {
        when(appointmentService.findAppointments()).thenReturn(List.of(response()));

        mockMvc.perform(get("/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(APPOINTMENT_ID.toString()));
    }

    @Test
    void bindsFilteredReceptionQueryAndOmitsReason() throws Exception {
        when(appointmentService.findReceptionAppointments(PATIENT_ID, 50)).thenReturn(List.of(
                new ReceptionAppointmentSummaryDTO(
                        APPOINTMENT_ID, PATIENT_ID, "DNI", "94000001", "Ana Demo", "Luis Rojas",
                        "Medicina General", LocalDate.of(2026, 10, 6), LocalTime.of(9, 0),
                        LocalTime.of(9, 30), AppointmentStatus.CONFIRMED, FlowStage.WAITING
                )));

        mockMvc.perform(get("/appointments/reception").param("patientId", PATIENT_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appointmentId").value(APPOINTMENT_ID.toString()))
                .andExpect(jsonPath("$[0].patientName").value("Ana Demo"))
                .andExpect(jsonPath("$[0].flowStage").value("WAITING"))
                .andExpect(jsonPath("$[0].reason").doesNotExist());
        verify(appointmentService).findReceptionAppointments(PATIENT_ID, 50);

        mockMvc.perform(get("/appointments/reception"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void bindsWaitingRoomPageAndOmitsSensitiveFields() throws Exception {
        when(appointmentService.findReceptionWaitingRoom(20, 10)).thenReturn(List.of(
                new ReceptionWaitingRoomDTO(APPOINTMENT_ID, "Ana Demo", LocalTime.of(9, 0),
                        "Luis Rojas", "Medicina General", FlowStage.WAITING)));

        mockMvc.perform(get("/appointments/reception/waiting-room")
                        .param("limit", "20").param("offset", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appointmentId").value(APPOINTMENT_ID.toString()))
                .andExpect(jsonPath("$[0].patientDisplay").value("Ana Demo"))
                .andExpect(jsonPath("$[0].flowStage").value("WAITING"))
                .andExpect(jsonPath("$[0].reason").doesNotExist())
                .andExpect(jsonPath("$[0].documentNumber").doesNotExist());
        verify(appointmentService).findReceptionWaitingRoom(20, 10);
    }

    @Test
    void confirmsAppointmentAndPreservesIdempotentResponse() throws Exception {
        AppointmentResponseDTO confirmed = response(APPOINTMENT_ID, SLOT_ID, AppointmentStatus.CONFIRMED);
        when(appointmentService.confirmAppointment(APPOINTMENT_ID)).thenReturn(confirmed);

        mockMvc.perform(post("/appointments/{id}/confirm", APPOINTMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentStatus").value("CONFIRMED"));
        mockMvc.perform(post("/appointments/{id}/confirm", APPOINTMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentStatus").value("CONFIRMED"));

        verify(appointmentService, times(2)).confirmAppointment(APPOINTMENT_ID);
    }

    @Test
    void cancelsAppointmentAndPreservesIdempotentResponse() throws Exception {
        AppointmentResponseDTO cancelled = response(APPOINTMENT_ID, SLOT_ID, AppointmentStatus.CANCELLED);
        when(appointmentService.cancelAppointment(APPOINTMENT_ID)).thenReturn(cancelled);

        mockMvc.perform(post("/appointments/{id}/cancel", APPOINTMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentStatus").value("CANCELLED"));
        mockMvc.perform(post("/appointments/{id}/cancel", APPOINTMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentStatus").value("CANCELLED"));

        verify(appointmentService, times(2)).cancelAppointment(APPOINTMENT_ID);
    }

    @Test
    void reschedulesAppointmentAndReturnsCreatedSuccessor() throws Exception {
        AppointmentResponseDTO successor = response(SUCCESSOR_ID, NEW_SLOT_ID, AppointmentStatus.SCHEDULED);
        when(appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID)).thenReturn(successor);

        mockMvc.perform(post("/appointments/{id}/reschedule", APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":\"" + NEW_SLOT_ID + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/appointments/" + SUCCESSOR_ID))
                .andExpect(jsonPath("$.id").value(SUCCESSOR_ID.toString()))
                .andExpect(jsonPath("$.slotId").value(NEW_SLOT_ID.toString()))
                .andExpect(jsonPath("$.appointmentStatus").value("SCHEDULED"));
    }

    @Test
    void mapsLifecycleConflicts() throws Exception {
        when(appointmentService.confirmAppointment(APPOINTMENT_ID))
                .thenThrow(new InvalidAppointmentTransitionException(
                        APPOINTMENT_ID,
                        AppointmentStatus.CANCELLED,
                        "confirmed"
                ));
        when(appointmentService.cancelAppointment(APPOINTMENT_ID))
                .thenThrow(new DataIntegrityViolationException("Capacity conflict"));
        when(appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID))
                .thenThrow(new AppointmentSuccessorExistsException(APPOINTMENT_ID));

        mockMvc.perform(post("/appointments/{id}/confirm", APPOINTMENT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_APPOINTMENT_TRANSITION"));
        mockMvc.perform(post("/appointments/{id}/cancel", APPOINTMENT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("APPOINTMENT_CAPACITY_CONFLICT"));
        mockMvc.perform(post("/appointments/{id}/reschedule", APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":\"" + NEW_SLOT_ID + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("APPOINTMENT_SUCCESSOR_EXISTS"));
    }

    @Test
    void mapsUnavailableRescheduleSlotToConflict() throws Exception {
        when(appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID))
                .thenThrow(new SlotUnavailableException(NEW_SLOT_ID));

        mockMvc.perform(post("/appointments/{id}/reschedule", APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":\"" + NEW_SLOT_ID + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("SLOT_UNAVAILABLE"));
    }

    @Test
    void mapsInvalidCancellationAndRescheduleTransitions() throws Exception {
        when(appointmentService.cancelAppointment(APPOINTMENT_ID))
                .thenThrow(new InvalidAppointmentTransitionException(
                        APPOINTMENT_ID,
                        AppointmentStatus.RESCHEDULED,
                        "cancelled"
                ));
        when(appointmentService.rescheduleAppointment(APPOINTMENT_ID, NEW_SLOT_ID))
                .thenThrow(new InvalidAppointmentTransitionException(
                        APPOINTMENT_ID,
                        AppointmentStatus.CANCELLED,
                        "rescheduled"
                ));

        mockMvc.perform(post("/appointments/{id}/cancel", APPOINTMENT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_APPOINTMENT_TRANSITION"));
        mockMvc.perform(post("/appointments/{id}/reschedule", APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":\"" + NEW_SLOT_ID + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_APPOINTMENT_TRANSITION"));
    }

    @Test
    void exposesAllAppointmentOperationsWithoutRequestBodies() throws Exception {
        when(appointmentService.checkInAppointment(APPOINTMENT_ID))
                .thenReturn(response(AppointmentStatus.CONFIRMED, FlowStage.CHECK_IN));
        when(appointmentService.moveAppointmentToWaiting(APPOINTMENT_ID))
                .thenReturn(response(AppointmentStatus.CONFIRMED, FlowStage.WAITING));
        when(medicalEncounterService.start(APPOINTMENT_ID))
                .thenReturn(new MedicalEncounterService.StartResult(
                        null, response(AppointmentStatus.CONFIRMED, FlowStage.IN_ATTENTION)));
        when(appointmentService.completeAppointment(APPOINTMENT_ID))
                .thenReturn(response(AppointmentStatus.COMPLETED, FlowStage.FINISHED));

        mockMvc.perform(post("/appointments/{id}/check-in", APPOINTMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowStage").value("CHECK_IN"));
        mockMvc.perform(post("/appointments/{id}/waiting", APPOINTMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowStage").value("WAITING"));
        mockMvc.perform(post("/appointments/{id}/start-attention", APPOINTMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowStage").value("IN_ATTENTION"));
        mockMvc.perform(post("/appointments/{id}/complete", APPOINTMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.flowStage").value("FINISHED"));
    }

    @Test
    void mapsOperationNotFoundAndInvalidTransition() throws Exception {
        when(appointmentService.checkInAppointment(APPOINTMENT_ID))
                .thenThrow(new AppointmentNotFoundException(APPOINTMENT_ID));
        when(appointmentService.completeAppointment(APPOINTMENT_ID))
                .thenThrow(new InvalidAppointmentTransitionException(
                        APPOINTMENT_ID,
                        AppointmentStatus.CONFIRMED,
                        "completed"
                ));

        mockMvc.perform(post("/appointments/{id}/check-in", APPOINTMENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("APPOINTMENT_NOT_FOUND"));
        mockMvc.perform(post("/appointments/{id}/complete", APPOINTMENT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_APPOINTMENT_TRANSITION"));
    }

    @Test
    void rejectsInvalidLifecyclePathUuid() throws Exception {
        mockMvc.perform(post("/appointments/not-a-uuid/confirm"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsMissingNullMalformedAndInvalidRescheduleBodies() throws Exception {
        mockMvc.perform(post("/appointments/{id}/reschedule", APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/appointments/{id}/reschedule", APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/appointments/{id}/reschedule", APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/appointments/{id}/reschedule", APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":\"not-a-uuid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    private String createRequest() {
        return """
                {
                  "slotId": "%s",
                  "patientId": "%s",
                  "reason": "Routine control"
                }
                """.formatted(SLOT_ID, PATIENT_ID);
    }

    private AppointmentResponseDTO response() {
        return response(APPOINTMENT_ID, SLOT_ID, AppointmentStatus.SCHEDULED);
    }

    private AppointmentResponseDTO response(UUID id, UUID slotId, AppointmentStatus status) {
        return response(id, slotId, status, null);
    }

    private AppointmentResponseDTO response(AppointmentStatus status, FlowStage flowStage) {
        return response(APPOINTMENT_ID, SLOT_ID, status, flowStage);
    }

    private AppointmentResponseDTO response(
            UUID id,
            UUID slotId,
            AppointmentStatus status,
            FlowStage flowStage
    ) {
        LocalDateTime now = LocalDateTime.of(2026, 10, 1, 8, 0);
        return new AppointmentResponseDTO(
                id,
                PATIENT_ID,
                PROFESSIONAL_ID,
                slotId,
                status,
                flowStage,
                "Routine control",
                null,
                null,
                now,
                now
        );
    }
}
