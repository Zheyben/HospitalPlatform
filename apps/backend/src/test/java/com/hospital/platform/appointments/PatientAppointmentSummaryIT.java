package com.hospital.platform.appointments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.platform.users.service.AuthenticatedUser;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
class PatientAppointmentSummaryIT {

    private static final UUID USER_A = UUID.fromString("55555555-5555-5555-5555-5555555555a1");
    private static final UUID USER_B = UUID.fromString("55555555-5555-5555-5555-5555555555b2");
    private static final UUID PATIENT_A = UUID.fromString("66666666-6666-6666-6666-6666666666a1");
    private static final UUID PATIENT_B = UUID.fromString("66666666-6666-6666-6666-6666666666b2");
    private static final UUID PROFESSIONAL = UUID.fromString("33333333-3333-3333-3333-3333333333a1");
    private static final UUID SPECIALTY = UUID.fromString("44444444-4444-4444-4444-4444444444a1");
    private static final UUID SCHEDULE = UUID.fromString("11111111-1111-1111-1111-1111111111a1");
    private static final UUID SLOT_A = UUID.fromString("22222222-2222-2222-2222-2222222222a1");
    private static final UUID SLOT_B = UUID.fromString("22222222-2222-2222-2222-2222222222b2");
    private static final UUID SLOT_OTHER = UUID.fromString("22222222-2222-2222-2222-2222222222c3");
    private UUID appointmentA;
    private UUID appointmentB;
    private UUID appointmentOther;
    private LocalDate appointmentDate;

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_patient_summary_test")
            .withUsername("hospital_app_test")
            .withPassword("hospital_app_test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        appointmentDate = LocalDate.now(ZoneId.of("America/Lima"))
                .with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        jdbcTemplate.execute("truncate table audit_logs, appointments, availability_slots, schedules, "
                + "professional_specialties, patients, professionals, specialties, refresh_tokens, "
                + "user_roles, users cascade");
        jdbcTemplate.update("insert into users (id, username, email, password_hash) values "
                + "(?, 'patient-summary-a', 'patient-summary-a@example.test', 'test'), "
                + "(?, 'patient-summary-b', 'patient-summary-b@example.test', 'test')", USER_A, USER_B);
        jdbcTemplate.update("insert into patients (id, user_id, document_type, document_number) values "
                + "(?, ?, 'DNI', '94000001'), (?, ?, 'DNI', '94000002')",
                PATIENT_A, USER_A, PATIENT_B, USER_B);
        jdbcTemplate.update("insert into specialties (id, name) "
                + "values (?, 'Cardiology Test')", SPECIALTY);
        jdbcTemplate.update("insert into professionals (id, license_number) "
                + "values (?, '123456')", PROFESSIONAL);
        jdbcTemplate.update("insert into professional_specialties (professional_id, specialty_id) values (?, ?)",
                PROFESSIONAL, SPECIALTY);
        jdbcTemplate.update("insert into schedules "
                + "(id, professional_id, specialty_id, day_of_week, start_time, end_time, active) "
                + "values (?, ?, ?, 2, time '09:00', time '12:00', true)",
                SCHEDULE, PROFESSIONAL, SPECIALTY);
        insertSlot(SLOT_A, "09:00");
        insertSlot(SLOT_B, "10:00");
        insertSlot(SLOT_OTHER, "11:00");
        appointmentA = jdbcTemplate.queryForObject("select capacity_reserve(?,?,?)", UUID.class,
                SLOT_A, PATIENT_A, null);
        appointmentB = jdbcTemplate.queryForObject("select capacity_reserve(?,?,?)", UUID.class,
                SLOT_B, PATIENT_A, "Control");
        appointmentOther = jdbcTemplate.queryForObject("select capacity_reserve(?,?,?)", UUID.class,
                SLOT_OTHER, PATIENT_B, null);
        jdbcTemplate.queryForObject("select capacity_cancel(?)", Object.class, appointmentA);
        jdbcTemplate.update("update schedules set active = false where id = ?", SCHEDULE);
        jdbcTemplate.update("update professionals set deleted_at = current_timestamp where id = ?", PROFESSIONAL);
        jdbcTemplate.update("update specialties set active = false, deleted_at = current_timestamp where id = ?",
                SPECIALTY);
    }

    @Test
    void showsOwnHistoryWithHumanDetailsAndNullableFields() throws Exception {
        mockMvc.perform(get("/appointments/me/summary")
                        .with(authentication(patientAuthentication(USER_A))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.appointmentId == '" + appointmentA + "')].status")
                        .value(org.hamcrest.Matchers.hasItem("CANCELLED")))
                .andExpect(jsonPath("$[?(@.appointmentId == '" + appointmentB + "')].status")
                        .value(org.hamcrest.Matchers.hasItem("SCHEDULED")))
                .andExpect(jsonPath("$[0].professionalName").value("123456"))
                .andExpect(jsonPath("$[0].specialtyName").value("Cardiology Test"))
                .andExpect(jsonPath("$[0].appointmentDate").value(appointmentDate.toString()))
                .andExpect(jsonPath("$[0].startTime").exists());

        assertThat(jdbcTemplate.queryForObject("select count(*) from appointments", Integer.class)).isEqualTo(3);
    }

    @Test
    void limitsSummaryToCurrentPatientAndRejectsOtherRoles() throws Exception {
        mockMvc.perform(get("/appointments/me/summary")
                        .with(authentication(patientAuthentication(USER_B))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].appointmentId").value(appointmentOther.toString()));
        mockMvc.perform(get("/appointments/me/summary"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/appointments/me/summary")
                        .with(authentication(authenticationToken(USER_A, "ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void usesProfessionalsCurrentNameWhenTheirAccountHasNames() throws Exception {
        UUID doctorUser = UUID.fromString("55555555-5555-5555-5555-5555555555c3");
        jdbcTemplate.update("insert into users "
                + "(id, username, email, password_hash, first_name, last_name) "
                + "values (?, 'summary-doctor', 'summary-doctor@example.test', 'test', 'Ana', 'Rojas')",
                doctorUser);
        jdbcTemplate.update("update professionals set user_id = ? where id = ?", doctorUser, PROFESSIONAL);

        mockMvc.perform(get("/appointments/me/summary")
                        .with(authentication(patientAuthentication(USER_A))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professionalName").value("Ana Rojas"));
    }

    @Test
    void receptionQueryIsLimitedAndKeepsHistoryWithoutReasons() throws Exception {
        jdbcTemplate.update("update users set first_name = 'Ana', last_name = 'Demo' where id = ?", USER_A);

        mockMvc.perform(get("/appointments/reception")
                        .with(authentication(authenticationToken(USER_A, "RECEPTIONIST")))
                        .param("patientId", PATIENT_A.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].appointmentId").value(appointmentA.toString()))
                .andExpect(jsonPath("$[0].status").value("CANCELLED"))
                .andExpect(jsonPath("$[0].patientName").value("Ana Demo"))
                .andExpect(jsonPath("$[0].documentNumber").value("94000001"))
                .andExpect(jsonPath("$[0].professionalName").value("123456"))
                .andExpect(jsonPath("$[0].specialtyName").value("Cardiology Test"))
                .andExpect(jsonPath("$[0].appointmentDate").value(appointmentDate.toString()))
                .andExpect(jsonPath("$[0].startTime").exists())
                .andExpect(jsonPath("$[0].reason").doesNotExist())
                .andExpect(jsonPath("$[0].slotId").doesNotExist());

        mockMvc.perform(get("/appointments/reception")
                        .with(authentication(authenticationToken(USER_A, "RECEPTIONIST")))
                        .param("patientId", PATIENT_A.toString()).param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/appointments/reception")
                        .with(authentication(authenticationToken(USER_A, "RECEPTIONIST")))
                        .param("patientId", PATIENT_B.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appointmentId").value(appointmentOther.toString()));
    }

    @Test
    void receptionQueryRequiresFilterAndRejectsBroadLegacyReads() throws Exception {
        var reception = authentication(authenticationToken(USER_A, "RECEPTIONIST"));
        mockMvc.perform(get("/appointments/reception").with(reception))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/appointments/reception").with(reception)
                        .param("patientId", PATIENT_A.toString()).param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_APPOINTMENT_REQUEST"));
        mockMvc.perform(get("/appointments/reception").with(reception)
                        .param("patientId", PATIENT_A.toString()).param("limit", "101"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/appointments").with(reception))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/appointments/{id}", appointmentA).with(reception))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/appointments/reception")
                        .with(authentication(patientAuthentication(USER_A)))
                        .param("patientId", PATIENT_A.toString()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/appointments/reception")
                        .with(authentication(authenticationToken(USER_A, "ADMIN")))
                        .param("patientId", PATIENT_A.toString()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/appointments/reception").param("patientId", PATIENT_A.toString()))
                .andExpect(status().isUnauthorized());
    }

    private void insertSlot(UUID id, String startTime) {
        jdbcTemplate.update("insert into availability_slots "
                + "(id, schedule_id, slot_date, start_time, end_time) "
                + "values (?, ?, ?, cast(? as time), cast(? as time) + interval '30 minutes')",
                id, SCHEDULE, Date.valueOf(appointmentDate), startTime, startTime);
    }

    private UsernamePasswordAuthenticationToken patientAuthentication(UUID userId) {
        return authenticationToken(userId, "PATIENT");
    }

    private UsernamePasswordAuthenticationToken authenticationToken(UUID userId, String role) {
        AuthenticatedUser principal = new AuthenticatedUser(userId, "patient-summary@example.test",
                "patient-summary", "test", true, Set.of(role), Set.of());
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
