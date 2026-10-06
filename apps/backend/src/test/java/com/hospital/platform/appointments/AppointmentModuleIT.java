package com.hospital.platform.appointments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.platform.appointments.dto.CreateAppointmentRequestDTO;
import com.hospital.platform.appointments.exception.SlotUnavailableException;
import com.hospital.platform.appointments.service.AppointmentService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class AppointmentModuleIT {

    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333331");
    private static final UUID SPECIALTY_ID = UUID.fromString("44444444-4444-4444-4444-444444444441");
    private static final UUID PATIENT_A_ID = UUID.fromString("66666666-6666-6666-6666-666666666661");
    private static final UUID PATIENT_B_ID = UUID.fromString("66666666-6666-6666-6666-666666666662");
    private static final UUID ACTIVE_SCHEDULE_ID = UUID.fromString("11111111-1111-1111-1111-111111111121");
    private static final UUID INACTIVE_SCHEDULE_ID = UUID.fromString("11111111-1111-1111-1111-111111111122");
    private static final UUID CREATE_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222231");
    private static final UUID ROLLBACK_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222232");
    private static final UUID CONCURRENT_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222233");
    private static final UUID RESERVED_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222234");
    private static final UUID BLOCKED_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222235");
    private static final UUID INACTIVE_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222236");

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_appointments_test")
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
    private MockMvc mockMvc;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void setUpData() {
        jdbcTemplate.execute("truncate table appointments, availability_slots, schedules, "
                + "professional_specialties, patients, professionals, specialties cascade");

        jdbcTemplate.update(
                "insert into specialties (id, name) values (?, ?)",
                SPECIALTY_ID,
                "Appointment Test Specialty"
        );
        jdbcTemplate.update(
                "insert into professionals (id, license_number) values (?, ?)",
                PROFESSIONAL_ID,
                "910004"
        );
        jdbcTemplate.update(
                "insert into patients (id, document_type, document_number) values (?, 'DNI', ?), (?, 'DNI', ?)",
                PATIENT_A_ID,
                "90000001",
                PATIENT_B_ID,
                "90000002"
        );
        jdbcTemplate.update("insert into professional_specialties(professional_id,specialty_id) values (?,?)",
                PROFESSIONAL_ID, SPECIALTY_ID);
        insertSchedule(ACTIVE_SCHEDULE_ID, true);
        insertSchedule(INACTIVE_SCHEDULE_ID, false);
        insertSlot(CREATE_SLOT_ID, ACTIVE_SCHEDULE_ID, "09:00", "AVAILABLE");
        insertSlot(ROLLBACK_SLOT_ID, ACTIVE_SCHEDULE_ID, "10:00", "AVAILABLE");
        insertSlot(CONCURRENT_SLOT_ID, ACTIVE_SCHEDULE_ID, "11:00", "AVAILABLE");
        insertSlot(RESERVED_SLOT_ID, ACTIVE_SCHEDULE_ID, "12:00", "AVAILABLE");
        jdbcTemplate.queryForObject("select capacity_reserve(?,?,?)", UUID.class,
                RESERVED_SLOT_ID, PATIENT_B_ID, "Existing reservation");
        insertSlot(BLOCKED_SLOT_ID, ACTIVE_SCHEDULE_ID, "13:00", "BLOCKED");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createsAppointmentAndReservesSlotAgainstPostgreSql() throws Exception {
        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(CREATE_SLOT_ID, PATIENT_A_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(PATIENT_A_ID.toString()))
                .andExpect(jsonPath("$.professionalId").value(PROFESSIONAL_ID.toString()))
                .andExpect(jsonPath("$.appointmentStatus").value("SCHEDULED"))
                .andExpect(jsonPath("$.flowStage").doesNotExist());

        assertThat(appointmentCount(CREATE_SLOT_ID)).isEqualTo(1);
        assertThat(slotStatus(CREATE_SLOT_ID)).isEqualTo("RESERVED");
        assertThat(flyway.info().current().getVersion().toString()).isEqualTo("18");
    }

    @Test
    void requiresAuthenticationAndRejectsProfessionalRole() throws Exception {
        mockMvc.perform(get("/appointments"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/appointments").with(user("professional").roles("PROFESSIONAL")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/appointments/{id}/confirm", UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTHENTICATION_REQUIRED"));
        mockMvc.perform(post("/appointments/{id}/cancel", UUID.randomUUID())
                        .with(user("professional").roles("PROFESSIONAL")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("APPOINTMENT_ACCESS_DENIED"));
        mockMvc.perform(post("/appointments/{id}/reschedule", UUID.randomUUID())
                        .with(user("professional").roles("PROFESSIONAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":\"" + CREATE_SLOT_ID + "\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("APPOINTMENT_ACCESS_DENIED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void preservesNotFoundAndUnavailableContractsWithCapacityLocks() throws Exception {
        mockMvc.perform(post("/appointments/{id}/confirm", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("APPOINTMENT_NOT_FOUND"));
        UUID appointmentId = jdbcTemplate.queryForObject("select id from appointments where slot_id=?",
                UUID.class, RESERVED_SLOT_ID);
        mockMvc.perform(post("/appointments/{id}/reschedule", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("SLOT_UNAVAILABLE"));
    }

    @Test
    void rejectsPatientChoosingAnotherPatientId() throws Exception {
        mockMvc.perform(post("/appointments")
                        .with(user("patient").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(CREATE_SLOT_ID, PATIENT_B_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("APPOINTMENT_ACCESS_DENIED"));
    }

    @Test
    void rejectsReservedBlockedAndInactiveScheduleSlots() {
        authenticateAdmin();

        for (UUID slotId : List.of(RESERVED_SLOT_ID, BLOCKED_SLOT_ID, INACTIVE_SLOT_ID)) {
            assertThatThrownBy(() -> appointmentService.createAppointment(request(slotId, PATIENT_A_ID)))
                    .isInstanceOf(SlotUnavailableException.class);
        }
    }

    @Test
    void enforcesAppointmentForeignKeys() {
        UUID missingReference = UUID.fromString("99999999-9999-9999-9999-999999999999");

        assertAppointmentInsertViolatesForeignKey(missingReference, PROFESSIONAL_ID, CREATE_SLOT_ID);
        assertAppointmentInsertViolatesForeignKey(PATIENT_A_ID, missingReference, CREATE_SLOT_ID);
        assertAppointmentInsertViolatesForeignKey(PATIENT_A_ID, PROFESSIONAL_ID, missingReference);

        assertThat(appointmentCount(CREATE_SLOT_ID)).isZero();
    }

    @Test
    void rollsBackReservedSlotWhenAppointmentInsertFails() {
        createRejectingInsertTrigger();
        authenticateAdmin();

        try {
            assertThatThrownBy(() -> appointmentService.createAppointment(request(ROLLBACK_SLOT_ID, PATIENT_A_ID)))
                    .isInstanceOf(DataAccessException.class);
        } finally {
            dropRejectingInsertTrigger();
        }

        assertThat(slotStatus(ROLLBACK_SLOT_ID)).isEqualTo("AVAILABLE");
        assertThat(appointmentCount(ROLLBACK_SLOT_ID)).isZero();
    }

    @Test
    void rollsBackReservationWhenProfessionalBecomesInactive() {
        jdbcTemplate.update("update professionals set deleted_at = current_timestamp where id = ?", PROFESSIONAL_ID);
        authenticateAdmin();

        assertThatThrownBy(() -> appointmentService.createAppointment(request(CREATE_SLOT_ID, PATIENT_A_ID)))
                .isInstanceOf(SlotUnavailableException.class);

        assertThat(slotStatus(CREATE_SLOT_ID)).isEqualTo("AVAILABLE");
        assertThat(appointmentCount(CREATE_SLOT_ID)).isZero();
    }

    @Test
    void allowsOnlyOneOfTwoConcurrentReservations() throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<String> first = executor.submit(
                    () -> attemptConcurrentCreation(PATIENT_A_ID, ready, start));
            Future<String> second = executor.submit(
                    () -> attemptConcurrentCreation(PATIENT_B_ID, ready, start));

            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            assertThat(List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("SUCCESS", "CONFLICT");
        }

        assertThat(appointmentCount(CONCURRENT_SLOT_ID)).isEqualTo(1);
        assertThat(slotStatus(CONCURRENT_SLOT_ID)).isEqualTo("RESERVED");
    }

    private String attemptConcurrentCreation(UUID patientId, CountDownLatch ready, CountDownLatch start)
            throws InterruptedException {
        authenticateAdmin();
        ready.countDown();
        start.await(10, TimeUnit.SECONDS);
        try {
            appointmentService.createAppointment(request(CONCURRENT_SLOT_ID, patientId));
            return "SUCCESS";
        } catch (SlotUnavailableException exception) {
            return "CONFLICT";
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void authenticateAdmin() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));
    }

    private CreateAppointmentRequestDTO request(UUID slotId, UUID patientId) {
        return new CreateAppointmentRequestDTO(slotId, patientId, "Appointment integration test");
    }

    private String requestJson(UUID slotId, UUID patientId) {
        return """
                {
                  "slotId": "%s",
                  "patientId": "%s",
                  "reason": "Appointment integration test"
                }
                """.formatted(slotId, patientId);
    }

    private void insertSchedule(UUID scheduleId, boolean active) {
        jdbcTemplate.update(
                "insert into schedules "
                        + "(id, professional_id, specialty_id, day_of_week, start_time, end_time, active) "
                        + "values (?, ?, ?, 2, time '09:00', time '16:00', ?)",
                scheduleId,
                PROFESSIONAL_ID,
                SPECIALTY_ID,
                active
        );
    }

    private void insertSlot(UUID slotId, UUID scheduleId, String startTime, String status) {
        jdbcTemplate.update(
                "insert into availability_slots "
                        + "(id, schedule_id, slot_date, start_time, end_time, status) "
                        + "values (?, ?, date '2030-10-01', cast(? as time), cast(? as time) + interval '30 minutes', ?)",
                slotId,
                scheduleId,
                startTime,
                startTime,
                status
        );
    }

    private void createRejectingInsertTrigger() {
        jdbcTemplate.execute("""
                create function reject_appointment_insert() returns trigger
                language plpgsql as $$
                begin
                    raise exception 'forced appointment insert failure';
                end;
                $$
                """);
        jdbcTemplate.execute("""
                create trigger reject_appointment_insert_trigger
                before insert on appointments
                for each row execute function reject_appointment_insert()
                """);
    }

    private void dropRejectingInsertTrigger() {
        jdbcTemplate.execute("drop trigger if exists reject_appointment_insert_trigger on appointments");
        jdbcTemplate.execute("drop function if exists reject_appointment_insert()");
    }

    private void assertAppointmentInsertViolatesForeignKey(
            UUID patientId,
            UUID professionalId,
            UUID slotId
    ) {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "insert into appointments "
                        + "(id, patient_id, professional_id, slot_id, appointment_status) "
                        + "values (?, ?, ?, ?, 'SCHEDULED')",
                UUID.randomUUID(),
                patientId,
                professionalId,
                slotId
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private int appointmentCount(UUID slotId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from appointments where slot_id = ?",
                Integer.class,
                slotId
        );
    }

    private String slotStatus(UUID slotId) {
        return jdbcTemplate.queryForObject(
                "select status from availability_slots where id = ?",
                String.class,
                slotId
        );
    }
}
