package com.hospital.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.platform.agenda.contract.AvailabilitySlotService;
import java.sql.Date;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class HospitalPlatformApplicationIT {

    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SPECIALTY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID INVALID_SPECIALTY_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ACTIVE_SCHEDULE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID INACTIVE_SCHEDULE_ID = UUID.fromString("11111111-1111-1111-1111-111111111112");
    private static final UUID AVAILABLE_ACTIVE_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222221");
    private static final UUID AVAILABLE_INACTIVE_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID RESERVED_ACTIVE_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222223");
    private static final UUID BLOCKED_ACTIVE_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222224");
    private static final UUID PATIENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666663");

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_test")
            .withUsername("hospital_app_test")
            .withPassword("hospital_app_test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AvailabilitySlotService availabilitySlotService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Clock clock;

    @BeforeEach
    void setUpAgendaData() {
        jdbcTemplate.execute("truncate table appointments, availability_slots, schedules, "
                + "professional_specialties, patients, professionals, specialties cascade");

        jdbcTemplate.update(
                "insert into specialties (id, name) values (?, ?)",
                SPECIALTY_ID,
                "Cardiology Test"
        );
        jdbcTemplate.update(
                "insert into professionals (id, license_number) values (?, ?)",
                PROFESSIONAL_ID,
                "910001"
        );
        jdbcTemplate.update("insert into patients (id, document_type, document_number) values (?, 'DNI', '90000003')",
                PATIENT_ID);
        jdbcTemplate.update("insert into professional_specialties (professional_id, specialty_id) values (?, ?)",
                PROFESSIONAL_ID, SPECIALTY_ID);
        jdbcTemplate.update(
                "insert into schedules "
                        + "(id, professional_id, specialty_id, day_of_week, start_time, end_time, active) "
                        + "values (?, ?, ?, 1, time '09:00', time '12:00', true)",
                ACTIVE_SCHEDULE_ID,
                PROFESSIONAL_ID,
                SPECIALTY_ID
        );
        jdbcTemplate.update(
                "insert into schedules "
                        + "(id, professional_id, specialty_id, day_of_week, start_time, end_time, active) "
                        + "values (?, ?, ?, 2, time '09:00', time '12:00', true)",
                INACTIVE_SCHEDULE_ID,
                PROFESSIONAL_ID,
                SPECIALTY_ID
        );

        insertSlot(AVAILABLE_ACTIVE_SLOT_ID, ACTIVE_SCHEDULE_ID, "09:00", "AVAILABLE");
        insertSlot(AVAILABLE_INACTIVE_SLOT_ID, INACTIVE_SCHEDULE_ID, "09:00", "AVAILABLE");
        jdbcTemplate.update("update schedules set active=false where id=?", INACTIVE_SCHEDULE_ID);
        insertSlot(RESERVED_ACTIVE_SLOT_ID, ACTIVE_SCHEDULE_ID, "10:00", "AVAILABLE");
        jdbcTemplate.queryForObject("select capacity_reserve(?,?,?)", UUID.class,
                RESERVED_ACTIVE_SLOT_ID, PATIENT_ID, "Existing booking");
        insertSlot(BLOCKED_ACTIVE_SLOT_ID, ACTIVE_SCHEDULE_ID, "11:00", "BLOCKED");
    }

    @Test
    void contextLoadsAndAppliesMigrationsThroughRefreshTokens() {
        assertThat(flyway.info().current().getVersion().toString()).isEqualTo("18");

        Integer rolesTableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables "
                        + "where table_schema = 'public' and table_name = 'roles'",
                Integer.class);
        Integer refreshTokensTableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables "
                        + "where table_schema = 'public' and table_name = 'refresh_tokens'",
                Integer.class);

        assertThat(rolesTableCount).isEqualTo(1);
        assertThat(refreshTokensTableCount).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from roles where name='PROFESSIONAL'", Integer.class)).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createsProfessionalAccountWithOneSpecialtyAndChangesStatus() throws Exception {
        String email = "doctor-" + UUID.randomUUID() + "@example.test";
        String request = """
                {"firstName":"Ana","lastName":"Ruiz","email":"%s",
                 "password":"SecurePass123","licenseNumber":"12345","specialtyId":"%s"}
                """.formatted(email, SPECIALTY_ID);

        mockMvc.perform(post("/professionals").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.specialtyId").value(SPECIALTY_ID.toString()));

        UUID createdId = jdbcTemplate.queryForObject("""
                select p.id from professionals p join users u on u.id=p.user_id where u.email=?
                """, UUID.class, email);
        assertThat(jdbcTemplate.queryForObject("""
                select count(*) from professional_specialties where professional_id=?
                """, Integer.class, createdId)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("""
                select u.password_hash from users u join professionals p on p.user_id=u.id where p.id=?
                """, String.class, createdId)).isNotEqualTo("SecurePass123");

        mockMvc.perform(patch("/professionals/{id}/status", createdId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(patch("/professionals/{id}/status", createdId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(true));

        jdbcTemplate.update("insert into specialties(id,name) values (?,?)",
                INVALID_SPECIALTY_ID, "Second specialty");
        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into professional_specialties(professional_id,specialty_id) values (?,?)
                """, createdId, INVALID_SPECIALTY_ID))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void reportsAvailableSlotWithActiveScheduleAsUsable() {
        assertThat(availabilitySlotService.isUsable(AVAILABLE_ACTIVE_SLOT_ID)).isTrue();
    }

    @Test
    void adminPublishesNewScheduleAndPatientCanSeeSlots() throws Exception {
        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);
        int dayOfWeek = tomorrow.getDayOfWeek().getValue() % 7;
        String request = """
                {"professionalId":"%s","specialtyId":"%s","dayOfWeek":%d,
                 "startTime":"13:00:00","endTime":"14:00:00"}
                """.formatted(PROFESSIONAL_ID, SPECIALTY_ID, dayOfWeek);

        mockMvc.perform(post("/agendas").with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated());
        UUID scheduleId = jdbcTemplate.queryForObject("""
                select id from schedules where professional_id=? and start_time=time '13:00'
                """, UUID.class, PROFESSIONAL_ID);

        mockMvc.perform(post("/agendas/{id}/publish", scheduleId)
                        .with(user("patient").roles("PATIENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/agendas/{id}/publish", scheduleId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdSlots").value(4))
                .andExpect(jsonPath("$.horizonDays").value(14));
        mockMvc.perform(post("/agendas/{id}/publish", scheduleId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdSlots").value(0));
        mockMvc.perform(get("/availability").param("scheduleId", scheduleId.toString())
                        .with(user("patient").roles("PATIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        String changedHours = """
                {"professionalId":"%s","specialtyId":"%s","dayOfWeek":%d,
                 "startTime":"14:00:00","endTime":"15:00:00"}
                """.formatted(PROFESSIONAL_ID, SPECIALTY_ID, dayOfWeek);
        mockMvc.perform(put("/agendas/{id}", scheduleId).with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(changedHours))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("AGENDA_CAPACITY_CONFLICT"));
        mockMvc.perform(patch("/agendas/{id}/status", scheduleId)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(post("/agendas/{id}/publish", scheduleId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("AGENDA_INACTIVE"));
        mockMvc.perform(get("/availability").param("scheduleId", scheduleId.toString())
                        .with(user("patient").roles("PATIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void reportsAvailableSlotWithInactiveScheduleAsNotUsable() {
        assertThat(availabilitySlotService.isUsable(AVAILABLE_INACTIVE_SLOT_ID)).isFalse();
    }

    @Test
    void reportsReservedSlotWithActiveScheduleAsNotUsable() {
        assertThat(availabilitySlotService.isUsable(RESERVED_ACTIVE_SLOT_ID)).isFalse();
    }

    @Test
    void reportsBlockedSlotWithActiveScheduleAsNotUsable() {
        assertThat(availabilitySlotService.isUsable(BLOCKED_ACTIVE_SLOT_ID)).isFalse();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void returnsControlledErrorForMissingProfessionalSpecialtyAssociation() throws Exception {
        String request = """
                {
                  "professionalId": "%s",
                  "specialtyId": "%s",
                  "dayOfWeek": 1,
                  "startTime": "09:00:00",
                  "endTime": "12:00:00"
                }
                """.formatted(PROFESSIONAL_ID, INVALID_SPECIALTY_ID);

        mockMvc.perform(post("/agendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Professional and specialty must have an active association"))
                .andExpect(jsonPath("$.errorCode").value("INVALID_PROFESSIONAL_SPECIALTY_ASSOCIATION"));
    }

    @Test
    @WithMockUser(roles = "PROFESSIONAL")
    void returnsForbiddenForUnauthorizedRoleOnEveryAgendaEndpoint() throws Exception {
        for (MockHttpServletRequestBuilder request : agendaRequests()) {
            mockMvc.perform(request).andExpect(status().isForbidden());
        }
    }

    @Test
    void requiresAuthenticationOnEveryAgendaEndpoint() throws Exception {
        for (MockHttpServletRequestBuilder request : agendaRequests()) {
            mockMvc.perform(request).andExpect(status().isUnauthorized());
        }
    }

    private void insertSlot(UUID slotId, UUID scheduleId, String startTime, String status) {
        LocalDate nextDate = LocalDate.now(ZoneId.of("America/Lima")).with(
                TemporalAdjusters.next(scheduleId.equals(INACTIVE_SCHEDULE_ID)
                        ? DayOfWeek.TUESDAY : DayOfWeek.MONDAY));
        jdbcTemplate.update(
                "insert into availability_slots "
                        + "(id, schedule_id, slot_date, start_time, end_time, status) "
                        + "values (?, ?, ?, cast(? as time), cast(? as time) + interval '30 minutes', ?)",
                slotId,
                scheduleId,
                Date.valueOf(nextDate),
                startTime,
                startTime,
                status
        );
    }

    private List<MockHttpServletRequestBuilder> agendaRequests() {
        String agendaRequest = """
                {
                  "professionalId": "%s",
                  "specialtyId": "%s",
                  "dayOfWeek": 1,
                  "startTime": "09:00:00",
                  "endTime": "12:00:00"
                }
                """.formatted(PROFESSIONAL_ID, SPECIALTY_ID);

        return List.of(
                post("/agendas").contentType(MediaType.APPLICATION_JSON).content(agendaRequest),
                get("/agendas"),
                get("/agendas/{id}", ACTIVE_SCHEDULE_ID),
                put("/agendas/{id}", ACTIVE_SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(agendaRequest),
                patch("/agendas/{id}/status", ACTIVE_SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"),
                post("/agendas/{id}/publish", ACTIVE_SCHEDULE_ID),
                get("/availability"),
                get("/availability/{id}", AVAILABLE_ACTIVE_SLOT_ID)
        );
    }
}
