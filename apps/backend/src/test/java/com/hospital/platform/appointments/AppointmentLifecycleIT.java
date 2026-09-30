package com.hospital.platform.appointments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.platform.appointments.exception.AppointmentSuccessorExistsException;
import com.hospital.platform.appointments.exception.InvalidAppointmentTransitionException;
import com.hospital.platform.appointments.exception.SlotUnavailableException;
import com.hospital.platform.appointments.service.AppointmentService;
import com.hospital.platform.users.service.AuthenticatedUser;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class AppointmentLifecycleIT {

    private static final UUID USER_ID = UUID.fromString("55555555-5555-5555-5555-555555555591");
    private static final UUID PATIENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666691");
    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333391");
    private static final UUID SPECIALTY_ID = UUID.fromString("44444444-4444-4444-4444-444444444491");
    private static final UUID SCHEDULE_ID = UUID.fromString("11111111-1111-1111-1111-111111111191");
    private static final UUID SLOT_A_ID = UUID.fromString("22222222-2222-2222-2222-222222222291");
    private static final UUID SLOT_B_ID = UUID.fromString("22222222-2222-2222-2222-222222222292");
    private static final UUID SLOT_C_ID = UUID.fromString("22222222-2222-2222-2222-222222222293");
    private static final UUID SLOT_D_ID = UUID.fromString("22222222-2222-2222-2222-222222222294");
    private static final UUID SLOT_E_ID = UUID.fromString("22222222-2222-2222-2222-222222222295");
    private static final UUID SLOT_F_ID = UUID.fromString("22222222-2222-2222-2222-222222222296");

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_appointment_lifecycle_test")
            .withUsername("hospital_app_test")
            .withPassword("hospital_app_test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUpData() {
        dropFailureTriggers();
        jdbcTemplate.update("delete from audit_logs");
        jdbcTemplate.update("delete from appointments");
        jdbcTemplate.update("delete from availability_slots");
        jdbcTemplate.update("delete from schedules");
        jdbcTemplate.update("delete from patients");
        jdbcTemplate.update("delete from professionals");
        jdbcTemplate.update("delete from specialties");
        jdbcTemplate.update("delete from users");

        jdbcTemplate.update(
                "insert into users (id, username, email, password_hash) values (?, ?, ?, ?)",
                USER_ID,
                "lifecycle-admin",
                "lifecycle-admin@example.test",
                "test-password-hash"
        );
        jdbcTemplate.update(
                "insert into patients (id, user_id, document_type, document_number) values (?, ?, 'DNI', ?)",
                PATIENT_ID,
                USER_ID,
                "93000001"
        );
        jdbcTemplate.update(
                "insert into specialties (id, name) values (?, ?)",
                SPECIALTY_ID,
                "Lifecycle Specialty"
        );
        jdbcTemplate.update(
                "insert into professionals (id, user_id, license_number) values (?, ?, ?)",
                PROFESSIONAL_ID,
                USER_ID,
                "CMP-LIFECYCLE-001"
        );
        jdbcTemplate.update(
                "insert into schedules "
                        + "(id, professional_id, specialty_id, day_of_week, start_time, end_time, active) "
                        + "values (?, ?, ?, 1, time '08:00', time '14:00', true)",
                SCHEDULE_ID,
                PROFESSIONAL_ID,
                SPECIALTY_ID
        );
        insertSlot(SLOT_A_ID, "08:00", "RESERVED");
        insertSlot(SLOT_B_ID, "09:00", "AVAILABLE");
        insertSlot(SLOT_C_ID, "10:00", "AVAILABLE");
        insertSlot(SLOT_D_ID, "11:00", "AVAILABLE");
        insertSlot(SLOT_E_ID, "12:00", "AVAILABLE");
        insertSlot(SLOT_F_ID, "13:00", "AVAILABLE");
        authenticateAdmin();
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        dropFailureTriggers();
    }

    @Test
    void confirmsAppointmentExactlyOnce() {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);

        appointmentService.confirmAppointment(appointmentId);
        appointmentService.confirmAppointment(appointmentId);

        assertThat(appointmentStatus(appointmentId)).isEqualTo("CONFIRMED");
        assertThat(auditCount("APPOINTMENT_CONFIRMED")).isEqualTo(1);
        assertThat(slotStatus(SLOT_A_ID)).isEqualTo("RESERVED");
    }

    @Test
    void cancelsScheduledAndConfirmedAppointmentsAndIsIdempotent() {
        UUID scheduledId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);
        jdbcTemplate.update("update availability_slots set status = 'RESERVED' where id = ?", SLOT_B_ID);
        UUID confirmedId = insertAppointment(SLOT_B_ID, "CONFIRMED", null);

        appointmentService.cancelAppointment(scheduledId);
        appointmentService.cancelAppointment(scheduledId);
        appointmentService.cancelAppointment(confirmedId);

        assertThat(appointmentStatus(scheduledId)).isEqualTo("CANCELLED");
        assertThat(appointmentStatus(confirmedId)).isEqualTo("CANCELLED");
        assertThat(cancelledBy(scheduledId)).isEqualTo(USER_ID);
        assertThat(cancelledAtExists(scheduledId)).isTrue();
        assertThat(slotStatus(SLOT_A_ID)).isEqualTo("AVAILABLE");
        assertThat(slotStatus(SLOT_B_ID)).isEqualTo("AVAILABLE");
        assertThat(auditCount("APPOINTMENT_CANCELLED")).isEqualTo(2);
    }

    @Test
    void reschedulesAppointmentsAndBuildsAChain() {
        UUID originalId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);

        UUID successorId = appointmentService.rescheduleAppointment(originalId, SLOT_B_ID).id();
        UUID secondSuccessorId = appointmentService.rescheduleAppointment(successorId, SLOT_C_ID).id();

        assertThat(appointmentStatus(originalId)).isEqualTo("RESCHEDULED");
        assertThat(appointmentStatus(successorId)).isEqualTo("RESCHEDULED");
        assertThat(appointmentStatus(secondSuccessorId)).isEqualTo("SCHEDULED");
        assertThat(predecessorId(successorId)).isEqualTo(originalId);
        assertThat(predecessorId(secondSuccessorId)).isEqualTo(successorId);
        assertThat(patientId(secondSuccessorId)).isEqualTo(PATIENT_ID);
        assertThat(professionalId(secondSuccessorId)).isEqualTo(PROFESSIONAL_ID);
        assertThat(slotStatus(SLOT_A_ID)).isEqualTo("AVAILABLE");
        assertThat(slotStatus(SLOT_B_ID)).isEqualTo("AVAILABLE");
        assertThat(slotStatus(SLOT_C_ID)).isEqualTo("RESERVED");
        assertThat(auditCount("APPOINTMENT_RESCHEDULED")).isEqualTo(2);
    }

    @Test
    void exposesLifecycleThroughHttpWithoutDuplicatingSideEffects() throws Exception {
        UUID confirmId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);
        jdbcTemplate.update("update availability_slots set status = 'RESERVED' where id = ?", SLOT_B_ID);
        UUID cancelId = insertAppointment(SLOT_B_ID, "SCHEDULED", null);
        jdbcTemplate.update("update availability_slots set status = 'RESERVED' where id = ?", SLOT_C_ID);
        UUID rescheduleId = insertAppointment(SLOT_C_ID, "SCHEDULED", null);

        mockMvc.perform(post("/appointments/{id}/confirm", confirmId)
                        .with(authentication(adminAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentStatus").value("CONFIRMED"));
        mockMvc.perform(post("/appointments/{id}/confirm", confirmId)
                        .with(authentication(adminAuthentication())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/appointments/{id}/cancel", cancelId)
                        .with(authentication(adminAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentStatus").value("CANCELLED"));
        mockMvc.perform(post("/appointments/{id}/cancel", cancelId)
                        .with(authentication(adminAuthentication())))
                .andExpect(status().isOk());

        var rescheduleResult = mockMvc.perform(post("/api/v1/appointments/{id}/reschedule", rescheduleId)
                        .contextPath("/api/v1")
                        .with(authentication(adminAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":\"" + SLOT_D_ID + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slotId").value(SLOT_D_ID.toString()))
                .andExpect(jsonPath("$.appointmentStatus").value("SCHEDULED"))
                .andReturn();

        UUID successorId = successorId(rescheduleId);
        String location = rescheduleResult.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        assertThat(URI.create(location).getPath())
                .startsWith("/api/v1/appointments/")
                .isEqualTo("/api/v1/appointments/" + successorId);

        assertThat(appointmentStatus(confirmId)).isEqualTo("CONFIRMED");
        assertThat(appointmentStatus(cancelId)).isEqualTo("CANCELLED");
        assertThat(slotStatus(SLOT_B_ID)).isEqualTo("AVAILABLE");
        assertThat(appointmentStatus(rescheduleId)).isEqualTo("RESCHEDULED");
        assertThat(slotStatus(SLOT_C_ID)).isEqualTo("AVAILABLE");
        assertThat(slotStatus(SLOT_D_ID)).isEqualTo("RESERVED");
        assertThat(auditCount("APPOINTMENT_CONFIRMED")).isEqualTo(1);
        assertThat(auditCount("APPOINTMENT_CANCELLED")).isEqualTo(1);
        assertThat(auditCount("APPOINTMENT_RESCHEDULED")).isEqualTo(1);
    }

    @Test
    void rollsBackWhenNewSlotReservationFails() {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);
        jdbcTemplate.update("update availability_slots set status = 'BLOCKED' where id = ?", SLOT_B_ID);

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(appointmentId, SLOT_B_ID))
                .isInstanceOf(SlotUnavailableException.class);

        assertThat(appointmentStatus(appointmentId)).isEqualTo("SCHEDULED");
        assertThat(slotStatus(SLOT_A_ID)).isEqualTo("RESERVED");
        assertThat(slotStatus(SLOT_B_ID)).isEqualTo("BLOCKED");
        assertThat(appointmentCountForPredecessor(appointmentId)).isZero();
        assertThat(auditCount("APPOINTMENT_RESCHEDULED")).isZero();
    }

    @Test
    void rollsBackWhenSuccessorCreationFails() {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);
        insertAppointment(SLOT_B_ID, "SCHEDULED", null);
        jdbcTemplate.update("update availability_slots set status = 'AVAILABLE' where id = ?", SLOT_B_ID);

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(appointmentId, SLOT_B_ID))
                .isInstanceOf(SlotUnavailableException.class);

        assertUnchangedReschedule(appointmentId, SLOT_B_ID);
        assertThat(appointmentCountForPredecessor(appointmentId)).isZero();
    }

    @Test
    void rollsBackWhenOriginalUpdateFails() {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);
        createRejectingAppointmentUpdateTrigger(appointmentId);

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(appointmentId, SLOT_B_ID))
                .isInstanceOfAny(JpaSystemException.class, DataAccessException.class);

        assertUnchangedReschedule(appointmentId, SLOT_B_ID);
    }

    @Test
    void rollsBackWhenOldSlotReleaseFails() {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);
        createRejectingSlotReleaseTrigger(SLOT_A_ID);

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(appointmentId, SLOT_B_ID))
                .isInstanceOfAny(JpaSystemException.class, DataAccessException.class);

        assertUnchangedReschedule(appointmentId, SLOT_B_ID);
    }

    @Test
    void rollsBackCancellationWhenSlotReleaseFails() {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);
        createRejectingSlotReleaseTrigger(SLOT_A_ID);

        assertThatThrownBy(() -> appointmentService.cancelAppointment(appointmentId))
                .isInstanceOfAny(JpaSystemException.class, DataAccessException.class);

        assertThat(appointmentStatus(appointmentId)).isEqualTo("SCHEDULED");
        assertThat(cancelledAtExists(appointmentId)).isFalse();
        assertThat(cancelledBy(appointmentId)).isNull();
        assertThat(slotStatus(SLOT_A_ID)).isEqualTo("RESERVED");
        assertThat(auditCount("APPOINTMENT_CANCELLED")).isZero();
    }

    @Test
    void rollsBackAllRescheduleEffectsWhenAuditFails() {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);
        createRejectingAuditInsertTrigger();

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(appointmentId, SLOT_B_ID))
                .isInstanceOfAny(JpaSystemException.class, DataAccessException.class);

        assertUnchangedReschedule(appointmentId, SLOT_B_ID);
    }

    @Test
    void rollsBackConfirmationWhenAuditFails() {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);
        createRejectingAuditInsertTrigger();

        assertThatThrownBy(() -> appointmentService.confirmAppointment(appointmentId))
                .isInstanceOfAny(JpaSystemException.class, DataAccessException.class);

        assertThat(appointmentStatus(appointmentId)).isEqualTo("SCHEDULED");
        assertThat(slotStatus(SLOT_A_ID)).isEqualTo("RESERVED");
        assertThat(auditCount("APPOINTMENT_CONFIRMED")).isZero();
    }

    @Test
    void rollsBackCancellationWhenAuditFails() {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);
        createRejectingAuditInsertTrigger();

        assertThatThrownBy(() -> appointmentService.cancelAppointment(appointmentId))
                .isInstanceOfAny(JpaSystemException.class, DataAccessException.class);

        assertThat(appointmentStatus(appointmentId)).isEqualTo("SCHEDULED");
        assertThat(slotStatus(SLOT_A_ID)).isEqualTo("RESERVED");
        assertThat(cancelledAtExists(appointmentId)).isFalse();
        assertThat(cancelledBy(appointmentId)).isNull();
        assertThat(auditCount("APPOINTMENT_CANCELLED")).isZero();
    }

    @Test
    void serializesConcurrentConfirmationAndCancellation() throws Exception {
        UUID confirmId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);
        jdbcTemplate.update("update availability_slots set status = 'RESERVED' where id = ?", SLOT_B_ID);
        UUID cancelId = insertAppointment(SLOT_B_ID, "SCHEDULED", null);
        createSlotReleaseObservationTrigger(SLOT_B_ID);

        assertThat(runWithDeterministicContention(() -> appointmentService.confirmAppointment(confirmId)))
                .containsExactly("SUCCESS", "SUCCESS");
        assertThat(runWithDeterministicContention(() -> appointmentService.cancelAppointment(cancelId)))
                .containsExactly("SUCCESS", "SUCCESS");

        assertThat(appointmentStatus(confirmId)).isEqualTo("CONFIRMED");
        assertThat(appointmentStatus(cancelId)).isEqualTo("CANCELLED");
        assertThat(auditCount("APPOINTMENT_CONFIRMED")).isEqualTo(1);
        assertThat(auditCount("APPOINTMENT_CANCELLED")).isEqualTo(1);
        assertThat(slotStatus(SLOT_B_ID)).isEqualTo("AVAILABLE");
        assertThat(observedSlotReleaseCount()).isEqualTo(1);
    }

    @Test
    void allowsOnlyOneConcurrentReschedule() throws Exception {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "SCHEDULED", null);

        assertThat(runWithDeterministicContention(
                () -> appointmentService.rescheduleAppointment(appointmentId, SLOT_B_ID)))
                .containsExactly("SUCCESS", "REJECTED");

        assertThat(appointmentStatus(appointmentId)).isEqualTo("RESCHEDULED");
        assertThat(appointmentCountForPredecessor(appointmentId)).isEqualTo(1);
        assertThat(slotStatus(SLOT_A_ID)).isEqualTo("AVAILABLE");
        assertThat(slotStatus(SLOT_B_ID)).isEqualTo("RESERVED");
        assertThat(auditCount("APPOINTMENT_RESCHEDULED")).isEqualTo(1);
    }

    @Test
    void executesCompleteOperationalFlowAndKeepsConsumedSlotReserved() {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "CONFIRMED", null);

        authenticate("RECEPTIONIST");
        appointmentService.checkInAppointment(appointmentId);
        appointmentService.moveAppointmentToWaiting(appointmentId);

        authenticate("PROFESSIONAL");
        appointmentService.startAppointmentAttention(appointmentId);
        appointmentService.completeAppointment(appointmentId);
        appointmentService.completeAppointment(appointmentId);

        assertThat(appointmentStatus(appointmentId)).isEqualTo("COMPLETED");
        assertThat(flowStage(appointmentId)).isEqualTo("FINISHED");
        assertThat(slotStatus(SLOT_A_ID)).isEqualTo("RESERVED");
        assertThat(auditCount("APPOINTMENT_CHECKED_IN")).isEqualTo(1);
        assertThat(auditCount("APPOINTMENT_WAITING")).isEqualTo(1);
        assertThat(auditCount("APPOINTMENT_ATTENTION_STARTED")).isEqualTo(1);
        assertThat(auditCount("APPOINTMENT_COMPLETED")).isEqualTo(1);
        assertThat(auditFlowStage("APPOINTMENT_CHECKED_IN", "old_values")).isNull();
        assertThat(auditFlowStage("APPOINTMENT_COMPLETED", "new_values")).isEqualTo("FINISHED");
    }

    @Test
    void repeatsWaitingThroughHttpWithoutChangingStateOrDuplicatingAudit() throws Exception {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "CONFIRMED", null);
        authenticate("RECEPTIONIST");
        appointmentService.checkInAppointment(appointmentId);

        mockMvc.perform(post("/appointments/{id}/waiting", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowStage").value("WAITING"));
        mockMvc.perform(post("/appointments/{id}/waiting", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowStage").value("WAITING"));

        assertThat(flowStage(appointmentId)).isEqualTo("WAITING");
        assertThat(auditCount("APPOINTMENT_WAITING")).isEqualTo(1);
    }

    @Test
    void repeatsStartAttentionThroughHttpWithoutChangingStateOrDuplicatingAudit() throws Exception {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "CONFIRMED", null);
        authenticate("RECEPTIONIST");
        appointmentService.checkInAppointment(appointmentId);
        appointmentService.moveAppointmentToWaiting(appointmentId);
        authenticate("PROFESSIONAL");

        mockMvc.perform(post("/appointments/{id}/start-attention", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowStage").value("IN_ATTENTION"));
        mockMvc.perform(post("/appointments/{id}/start-attention", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowStage").value("IN_ATTENTION"));

        assertThat(flowStage(appointmentId)).isEqualTo("IN_ATTENTION");
        assertThat(auditCount("APPOINTMENT_ATTENTION_STARTED")).isEqualTo(1);
    }

    @Test
    void rollsBackOperationalTransitionWhenAuditFails() {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "CONFIRMED", null);
        createRejectingAuditInsertTrigger();
        authenticate("RECEPTIONIST");

        assertThatThrownBy(() -> appointmentService.checkInAppointment(appointmentId))
                .isInstanceOfAny(JpaSystemException.class, DataAccessException.class);

        assertThat(appointmentStatus(appointmentId)).isEqualTo("CONFIRMED");
        assertThat(flowStage(appointmentId)).isNull();
        assertThat(slotStatus(SLOT_A_ID)).isEqualTo("RESERVED");
        assertThat(auditCount("APPOINTMENT_CHECKED_IN")).isZero();
    }

    @Test
    void enforcesProfessionalOwnershipAgainstPostgreSqlData() {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "CONFIRMED", null);
        jdbcTemplate.update("update appointments set flow_stage = 'WAITING' where id = ?", appointmentId);
        authenticate("PROFESSIONAL");

        appointmentService.startAppointmentAttention(appointmentId);
        assertThat(flowStage(appointmentId)).isEqualTo("IN_ATTENTION");

        UUID deniedId = insertAppointment(SLOT_B_ID, "CONFIRMED", null);
        jdbcTemplate.update("update availability_slots set status = 'RESERVED' where id = ?", SLOT_B_ID);
        jdbcTemplate.update("update appointments set flow_stage = 'WAITING' where id = ?", deniedId);
        jdbcTemplate.update("update professionals set user_id = null where id = ?", PROFESSIONAL_ID);

        assertThatThrownBy(() -> appointmentService.startAppointmentAttention(deniedId))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(flowStage(deniedId)).isEqualTo("WAITING");
        assertThat(auditCount("APPOINTMENT_ATTENTION_STARTED")).isEqualTo(1);
    }

    @Test
    void serializesConcurrentCheckInWithoutDuplicatingAudit() throws Exception {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "CONFIRMED", null);

        assertThat(runWithDeterministicContention(
                () -> appointmentService.checkInAppointment(appointmentId),
                "RECEPTIONIST"
        )).containsExactly("SUCCESS", "SUCCESS");

        assertThat(flowStage(appointmentId)).isEqualTo("CHECK_IN");
        assertThat(auditCount("APPOINTMENT_CHECKED_IN")).isEqualTo(1);
    }

    @Test
    void rejectsUnauthenticatedAndAdminHttpOperations() throws Exception {
        UUID appointmentId = insertAppointment(SLOT_A_ID, "CONFIRMED", null);
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/appointments/{id}/check-in", appointmentId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/appointments/{id}/check-in", appointmentId)
                        .with(authentication(adminAuthentication())))
                .andExpect(status().isForbidden());
    }

    private List<String> runWithDeterministicContention(ThrowingOperation operation) throws Exception {
        return runWithDeterministicContention(operation, "ADMIN");
    }

    private List<String> runWithDeterministicContention(
            ThrowingOperation operation,
            String role
    ) throws Exception {
        CountDownLatch firstOperationCompleted = new CountDownLatch(1);
        CountDownLatch allowFirstCommit = new CountDownLatch(1);
        CountDownLatch secondOperationStarted = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<String> first = executor.submit(
                    () -> executeHoldingTransaction(operation, firstOperationCompleted, allowFirstCommit, role));
            Future<String> second;
            try {
                assertThat(firstOperationCompleted.await(10, TimeUnit.SECONDS)).isTrue();
                second = executor.submit(() -> {
                    secondOperationStarted.countDown();
                    return attempt(operation, role);
                });
                assertThat(secondOperationStarted.await(10, TimeUnit.SECONDS)).isTrue();
                assertThat(awaitPostgreSqlLockContention()).isTrue();
            } finally {
                allowFirstCommit.countDown();
            }

            return List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS));
        }
    }

    private String executeHoldingTransaction(
            ThrowingOperation operation,
            CountDownLatch operationCompleted,
            CountDownLatch allowCommit,
            String role
    ) {
        authenticate(role);
        try {
            return new TransactionTemplate(transactionManager).execute(status -> {
                operation.run();
                operationCompleted.countDown();
                await(allowCommit, "Timed out waiting to commit the lock-holding transaction");
                return "SUCCESS";
            });
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private String attempt(ThrowingOperation operation) {
        return attempt(operation, "ADMIN");
    }

    private String attempt(ThrowingOperation operation, String role) {
        authenticate(role);
        try {
            operation.run();
            return "SUCCESS";
        } catch (InvalidAppointmentTransitionException | AppointmentSuccessorExistsException exception) {
            return "REJECTED";
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private boolean awaitPostgreSqlLockContention() {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            Boolean blocked = jdbcTemplate.queryForObject("""
                    select exists (
                        select 1
                        from pg_stat_activity activity
                        where activity.datname = current_database()
                          and activity.pid <> pg_backend_pid()
                          and cardinality(pg_blocking_pids(activity.pid)) > 0
                          and lower(activity.query) like '%appointment%'
                    )
                    """, Boolean.class);
            if (Boolean.TRUE.equals(blocked)) {
                return true;
            }
            Thread.onSpinWait();
        }
        return false;
    }

    private void authenticateAdmin() {
        authenticate("ADMIN");
    }

    private UsernamePasswordAuthenticationToken adminAuthentication() {
        return roleAuthentication("ADMIN");
    }

    private void authenticate(String role) {
        SecurityContextHolder.getContext().setAuthentication(roleAuthentication(role));
    }

    private UsernamePasswordAuthenticationToken roleAuthentication(String role) {
        AuthenticatedUser user = new AuthenticatedUser(
                USER_ID,
                "lifecycle-admin@example.test",
                "lifecycle-admin",
                "test-password-hash",
                true,
                Set.of(role),
                Set.of()
        );
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private void insertSlot(UUID slotId, String startTime, String status) {
        jdbcTemplate.update(
                "insert into availability_slots "
                        + "(id, schedule_id, slot_date, start_time, end_time, status) "
                        + "values (?, ?, date '2031-01-06', cast(? as time), "
                        + "cast(? as time) + interval '30 minutes', ?)",
                slotId,
                SCHEDULE_ID,
                startTime,
                startTime,
                status
        );
    }

    private UUID insertAppointment(UUID slotId, String status, UUID predecessorId) {
        UUID appointmentId = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into appointments "
                        + "(id, patient_id, professional_id, slot_id, appointment_status, reason, "
                        + "rescheduled_from_id) values (?, ?, ?, ?, ?, 'Lifecycle integration test', ?)",
                appointmentId,
                PATIENT_ID,
                PROFESSIONAL_ID,
                slotId,
                status,
                predecessorId
        );
        return appointmentId;
    }

    private void assertUnchangedReschedule(UUID appointmentId, UUID newSlotId) {
        assertThat(appointmentStatus(appointmentId)).isEqualTo("SCHEDULED");
        assertThat(slotStatus(SLOT_A_ID)).isEqualTo("RESERVED");
        assertThat(slotStatus(newSlotId)).isEqualTo("AVAILABLE");
        assertThat(appointmentCountForPredecessor(appointmentId)).isZero();
        assertThat(auditCount("APPOINTMENT_RESCHEDULED")).isZero();
    }

    private void createRejectingAppointmentUpdateTrigger(UUID appointmentId) {
        jdbcTemplate.execute("""
                create function reject_lifecycle_appointment_update() returns trigger
                language plpgsql as $$
                begin
                    if old.id = '%s'::uuid then
                        raise exception 'forced appointment update failure';
                    end if;
                    return new;
                end;
                $$
                """.formatted(appointmentId));
        jdbcTemplate.execute("""
                create trigger reject_lifecycle_appointment_update_trigger
                before update on appointments
                for each row execute function reject_lifecycle_appointment_update()
                """);
    }

    private void createRejectingAuditInsertTrigger() {
        jdbcTemplate.execute("""
                create function reject_lifecycle_audit_insert() returns trigger
                language plpgsql as $$
                begin
                    raise exception 'forced audit insert failure';
                end;
                $$
                """);
        jdbcTemplate.execute("""
                create trigger reject_lifecycle_audit_insert_trigger
                before insert on audit_logs
                for each row execute function reject_lifecycle_audit_insert()
                """);
    }

    private void createRejectingSlotReleaseTrigger(UUID slotId) {
        jdbcTemplate.execute("""
                create function reject_lifecycle_slot_release() returns trigger
                language plpgsql as $$
                begin
                    if old.id = '%s'::uuid
                       and old.status = 'RESERVED'
                       and new.status = 'AVAILABLE' then
                        raise exception 'forced slot release failure';
                    end if;
                    return new;
                end;
                $$
                """.formatted(slotId));
        jdbcTemplate.execute("""
                create trigger reject_lifecycle_slot_release_trigger
                before update on availability_slots
                for each row execute function reject_lifecycle_slot_release()
                """);
    }

    private void createSlotReleaseObservationTrigger(UUID slotId) {
        jdbcTemplate.execute("create table lifecycle_slot_release_observations (slot_id uuid not null)");
        jdbcTemplate.execute("""
                create function observe_lifecycle_slot_release() returns trigger
                language plpgsql as $$
                begin
                    if old.id = '%s'::uuid
                       and old.status = 'RESERVED'
                       and new.status = 'AVAILABLE' then
                        insert into lifecycle_slot_release_observations (slot_id) values (new.id);
                    end if;
                    return new;
                end;
                $$
                """.formatted(slotId));
        jdbcTemplate.execute("""
                create trigger observe_lifecycle_slot_release_trigger
                after update on availability_slots
                for each row execute function observe_lifecycle_slot_release()
                """);
    }

    private int observedSlotReleaseCount() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from lifecycle_slot_release_observations",
                Integer.class
        );
        return count == null ? 0 : count;
    }

    private void dropFailureTriggers() {
        jdbcTemplate.execute("drop trigger if exists reject_lifecycle_appointment_update_trigger on appointments");
        jdbcTemplate.execute("drop function if exists reject_lifecycle_appointment_update()");
        jdbcTemplate.execute("drop trigger if exists reject_lifecycle_audit_insert_trigger on audit_logs");
        jdbcTemplate.execute("drop function if exists reject_lifecycle_audit_insert()");
        jdbcTemplate.execute("drop trigger if exists reject_lifecycle_slot_release_trigger on availability_slots");
        jdbcTemplate.execute("drop function if exists reject_lifecycle_slot_release()");
        jdbcTemplate.execute("drop trigger if exists observe_lifecycle_slot_release_trigger on availability_slots");
        jdbcTemplate.execute("drop function if exists observe_lifecycle_slot_release()");
        jdbcTemplate.execute("drop table if exists lifecycle_slot_release_observations");
    }

    private void await(CountDownLatch latch, String timeoutMessage) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(timeoutMessage);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Lifecycle contention test interrupted", exception);
        }
    }

    private String appointmentStatus(UUID appointmentId) {
        return jdbcTemplate.queryForObject(
                "select appointment_status from appointments where id = ?",
                String.class,
                appointmentId
        );
    }

    private String flowStage(UUID appointmentId) {
        return jdbcTemplate.queryForObject(
                "select flow_stage from appointments where id = ?",
                String.class,
                appointmentId
        );
    }

    private String auditFlowStage(String action, String valuesColumn) {
        if (!valuesColumn.equals("old_values") && !valuesColumn.equals("new_values")) {
            throw new IllegalArgumentException("Unsupported audit values column");
        }
        return jdbcTemplate.queryForObject(
                "select " + valuesColumn + " ->> 'flowStage' from audit_logs where action = ?",
                String.class,
                action
        );
    }

    private UUID predecessorId(UUID appointmentId) {
        return jdbcTemplate.queryForObject(
                "select rescheduled_from_id from appointments where id = ?",
                UUID.class,
                appointmentId
        );
    }

    private UUID successorId(UUID appointmentId) {
        return jdbcTemplate.queryForObject(
                "select id from appointments where rescheduled_from_id = ?",
                UUID.class,
                appointmentId
        );
    }

    private UUID patientId(UUID appointmentId) {
        return jdbcTemplate.queryForObject(
                "select patient_id from appointments where id = ?",
                UUID.class,
                appointmentId
        );
    }

    private UUID professionalId(UUID appointmentId) {
        return jdbcTemplate.queryForObject(
                "select professional_id from appointments where id = ?",
                UUID.class,
                appointmentId
        );
    }

    private UUID cancelledBy(UUID appointmentId) {
        return jdbcTemplate.queryForObject(
                "select cancelled_by from appointments where id = ?",
                UUID.class,
                appointmentId
        );
    }

    private boolean cancelledAtExists(UUID appointmentId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "select cancelled_at is not null from appointments where id = ?",
                Boolean.class,
                appointmentId
        ));
    }

    private String slotStatus(UUID slotId) {
        return jdbcTemplate.queryForObject(
                "select status from availability_slots where id = ?",
                String.class,
                slotId
        );
    }

    private int appointmentCountForPredecessor(UUID appointmentId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from appointments where rescheduled_from_id = ?",
                Integer.class,
                appointmentId
        );
    }

    private int auditCount(String eventType) {
        return jdbcTemplate.queryForObject(
                "select count(*) from audit_logs where action = ?",
                Integer.class,
                eventType
        );
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run();
    }
}
