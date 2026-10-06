package com.hospital.platform.medical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.platform.users.service.AuthenticatedUser;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
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
@AutoConfigureMockMvc
@Testcontainers
class MedicalIntegratedRuntimeIT {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final LocalDate TODAY = LocalDate.now(LIMA);
    private static final LocalDate APPOINTMENT_DATE = TODAY.plusDays(1);
    private static final String RUNTIME_USER = "hospital_app_runtime_it";
    private static final String RUNTIME_PASSWORD = "runtime-test-only";
    private static final UUID ADMIN_USER = UUID.fromString("10000000-0000-0000-0000-000000000011");
    private static final UUID RECEPTION_USER = UUID.fromString("10000000-0000-0000-0000-000000000012");

    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_integrated_test")
            .withUsername("hospital_owner_test")
            .withPassword("hospital_owner_test");
    private static JdbcTemplate owner;

    @TestConfiguration
    static class ClockConfiguration {
        @Bean
        @Primary
        AdjustableClock integratedClock() {
            return new AdjustableClock(TODAY.atTime(9, 0).atZone(LIMA).toInstant(), LIMA);
        }
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        Flyway.configure().dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration").load().migrate();
        DataSource ownerDataSource = new DriverManagerDataSource(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        owner = new JdbcTemplate(ownerDataSource);
        owner.execute("create role " + RUNTIME_USER + " login password '" + RUNTIME_PASSWORD + "'");
        owner.execute("""
                create or replace function hospital_business_now() returns timestamp
                language sql volatile security definer set search_path=pg_catalog as
                $$ select timestamp '%s 09:00:00' $$
                """.formatted(APPOINTMENT_DATE));
        new ResourceDatabasePopulator(new ClassPathResource("medical-runtime-role.sql"))
                .execute(ownerDataSource);

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", () -> RUNTIME_USER);
        registry.add("spring.datasource.password", () -> RUNTIME_PASSWORD);
        registry.add("spring.flyway.enabled", () -> false);
    }

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private JdbcTemplate runtime;
    @Autowired private AdjustableClock clock;

    @Test
    void completesAdministrativeToPatientClinicalFlowWithRestrictedSqlRole() throws Exception {
        assertThat(runtime.queryForObject("select current_user", String.class)).isEqualTo(RUNTIME_USER);
        assertThat(owner.queryForObject("select max(installed_rank) from flyway_schema_history", Integer.class))
                .isEqualTo(18);
        assertThatThrownBy(() -> runtime.update("update availability_slots set status = 'BLOCKED'"))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> runtime.update("update icd10_codes set active = false"))
                .isInstanceOf(DataAccessException.class);

        owner.update("""
                insert into users (id, username, email, password_hash, first_name, last_name)
                values (?, 'integrated-admin', 'integrated-admin@example.test', 'test', 'Demo', 'Admin'),
                       (?, 'integrated-reception', 'integrated-reception@example.test', 'test', 'Demo', 'Reception')
                """, ADMIN_USER, RECEPTION_USER);
        UUID specialtyId = UUID.randomUUID();
        owner.update("insert into specialties (id, name) values (?, 'Integrated Medicine')", specialtyId);

        JsonNode professional = body(mvc.perform(post("/professionals")
                        .with(authentication(actor(ADMIN_USER, "ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"firstName":"Ana","lastName":"Rojas",
                                 "email":"integrated-doctor@example.test","password":"SecurePass123",
                                 "licenseNumber":"123456","specialtyId":"%s"}
                                """.formatted(specialtyId)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        UUID professionalId = UUID.fromString(professional.get("id").asText());
        UUID doctorUserId = owner.queryForObject(
                "select user_id from professionals where id = ?", UUID.class, professionalId);

        int dayOfWeek = APPOINTMENT_DATE.getDayOfWeek().getValue() % 7;
        JsonNode schedule = body(mvc.perform(post("/agendas")
                        .with(authentication(actor(ADMIN_USER, "ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"professionalId":"%s","specialtyId":"%s","dayOfWeek":%d,
                                 "startTime":"10:00:00","endTime":"11:00:00"}
                                """.formatted(professionalId, specialtyId, dayOfWeek)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        UUID scheduleId = UUID.fromString(schedule.get("id").asText());
        mvc.perform(post("/agendas/{id}/publish", scheduleId)
                        .with(authentication(actor(ADMIN_USER, "ADMIN"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.createdSlots").value(4));

        JsonNode registration = body(mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"email":"integrated-patient@example.test","password":"strong-password",
                                 "documentType":"DNI","documentNumber":"97000001",
                                 "firstName":"Elena","lastName":"Diaz","birthDate":"1990-01-01",
                                 "phone":"987654321","insurance":"SIS"}
                                """))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        UUID patientUserId = UUID.fromString(registration.get("userId").asText());
        UUID patientId = UUID.fromString(registration.get("patientId").asText());
        assertThat(owner.queryForObject("select count(*) from clinical_records where patient_id = ?",
                Integer.class, patientId)).isEqualTo(1);

        JsonNode available = body(mvc.perform(get("/availability")
                        .with(authentication(actor(patientUserId, "PATIENT")))
                        .param("scheduleId", scheduleId.toString())
                        .param("slotDate", APPOINTMENT_DATE.toString()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(available).hasSize(2);
        UUID slotId = UUID.fromString(available.get(0).get("slotId").asText());
        JsonNode appointment = body(mvc.perform(post("/appointments")
                        .with(authentication(actor(patientUserId, "PATIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":\"" + slotId + "\",\"reason\":\"Synthetic visit\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        UUID appointmentId = UUID.fromString(appointment.get("id").asText());
        mvc.perform(post("/appointments")
                        .with(authentication(actor(patientUserId, "PATIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":\"" + slotId + "\"}"))
                .andExpect(status().isConflict());

        mvc.perform(get("/patients/search")
                        .with(authentication(actor(RECEPTION_USER, "RECEPTIONIST")))
                        .param("documentType", "DNI").param("documentNumber", "97000001"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.patientId").value(patientId.toString()));
        mvc.perform(get("/appointments/reception")
                        .with(authentication(actor(RECEPTION_USER, "RECEPTIONIST")))
                        .param("patientId", patientId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appointmentId").value(appointmentId.toString()))
                .andExpect(jsonPath("$[0].reason").doesNotExist());
        mvc.perform(post("/appointments/{id}/confirm", appointmentId)
                        .with(authentication(actor(RECEPTION_USER, "RECEPTIONIST"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.appointmentStatus").value("CONFIRMED"));
        clock.set(APPOINTMENT_DATE.atTime(9, 30).atZone(LIMA).toInstant());
        mvc.perform(post("/appointments/{id}/check-in", appointmentId)
                        .with(authentication(actor(RECEPTION_USER, "RECEPTIONIST"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.flowStage").value("CHECK_IN"));
        mvc.perform(post("/appointments/{id}/check-in", appointmentId)
                        .with(authentication(actor(RECEPTION_USER, "RECEPTIONIST"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.flowStage").value("CHECK_IN"));
        mvc.perform(post("/appointments/{id}/waiting", appointmentId)
                        .with(authentication(actor(RECEPTION_USER, "RECEPTIONIST"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.flowStage").value("WAITING"));

        JsonNode encounter = body(mvc.perform(post("/medical/appointments/{id}/start", appointmentId)
                        .with(authentication(actor(doctorUserId, "PROFESSIONAL"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        UUID encounterId = UUID.fromString(encounter.get("encounterId").asText());
        mvc.perform(post("/medical/appointments/{id}/start", appointmentId)
                        .with(authentication(actor(doctorUserId, "PROFESSIONAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.encounterId").value(encounterId.toString()));

        UUID icdSource = UUID.randomUUID();
        UUID medicationSource = UUID.randomUUID();
        UUID codeId = UUID.randomUUID();
        UUID medicationId = UUID.randomUUID();
        UUID presentationId = UUID.randomUUID();
        owner.update("""
                insert into medical_catalog_sources
                    (id, catalog_type, source_name, source_version, license_reference,
                     approved_by_user_id, approved_at)
                values (?, 'ICD10', 'Synthetic ICD', '1', 'TEST ONLY', ?, current_timestamp),
                       (?, 'MEDICATION', 'Synthetic drug', '1', 'TEST ONLY', ?, current_timestamp)
                """, icdSource, ADMIN_USER, medicationSource, ADMIN_USER);
        owner.update("insert into icd10_codes (id, code, description, source_id) "
                + "values (?, 'SYN-001', 'Synthetic diagnosis', ?)", codeId, icdSource);
        owner.update("insert into medications (id, generic_name, source_id) "
                + "values (?, 'Synthetic medicine', ?)", medicationId, medicationSource);
        owner.update("""
                insert into medication_presentations
                    (id, medication_id, name, concentration, pharmaceutical_form)
                values (?, ?, 'Tablet', '10 mg', 'TABLET')
                """, presentationId, medicationId);

        mvc.perform(put("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(doctorUserId, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"history\":{\"otherAlerts\":\"Synthetic allergy\"}}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));
        mvc.perform(put("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(doctorUserId, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"version":1,"assessment":{
                                  "presentation":{"reason":"Synthetic visit",
                                    "symptomsAndCurrentIllness":"Synthetic symptoms"},
                                  "diagnosis":{"primaryDiagnosis":"Synthetic diagnosis",
                                    "icd10CodeId":"%s","diagnosisType":"PRESUMPTIVE"},
                                  "treatmentPlan":{"therapeuticPlan":"Synthetic plan",
                                    "generalIndications":"Synthetic instructions","referralType":"NONE"}}}
                                """.formatted(codeId)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(2));
        mvc.perform(put("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(doctorUserId, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"version":2,"prescription":{"items":[{"medicationId":"%s",
                                  "presentationId":"%s","dose":1,"doseUnit":"MG",
                                  "frequency":"ONCE","route":"ORAL","duration":1,
                                  "durationUnit":"DAYS","quantity":1,
                                  "usageInstructions":"Synthetic use only"}]}}
                                """.formatted(medicationId, presentationId)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(3));
        clock.set(APPOINTMENT_DATE.atTime(11, 0).atZone(LIMA).toInstant());
        owner.execute("""
                create or replace function hospital_business_now() returns timestamp
                language sql volatile security definer set search_path=pg_catalog as
                $$ select timestamp '%s 11:00:00' $$
                """.formatted(APPOINTMENT_DATE));
        mvc.perform(post("/medical/encounters/{id}/finalize", encounterId)
                        .with(authentication(actor(doctorUserId, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\":3}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FINALIZED"));
        mvc.perform(post("/medical/encounters/{id}/finalize", encounterId)
                        .with(authentication(actor(doctorUserId, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\":3}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FINALIZED"));

        assertThat(owner.queryForObject("select count(*) from clinical_final_records where encounter_id = ?",
                Integer.class, encounterId)).isEqualTo(1);
        assertThat(owner.queryForObject("select count(*) from clinical_prescriptions where encounter_id = ?",
                Integer.class, encounterId)).isEqualTo(1);
        assertThat(owner.queryForObject("select status from availability_slots where id = ?",
                String.class, slotId)).isEqualTo("RESERVED");
        assertThat(owner.queryForObject("select appointment_status || '/' || flow_stage "
                + "from appointments where id = ?", String.class, appointmentId)).isEqualTo("COMPLETED/FINISHED");
        mvc.perform(get("/medical/me/encounters/{id}", encounterId)
                        .with(authentication(actor(patientUserId, "PATIENT"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.primaryDiagnosis").value("Synthetic diagnosis"));
        mvc.perform(get("/medical/me/prescriptions")
                        .with(authentication(actor(patientUserId, "PATIENT"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1));
        mvc.perform(get("/medical/me/encounters/{id}", encounterId)
                        .with(authentication(actor(RECEPTION_USER, "RECEPTIONIST"))))
                .andExpect(status().isForbidden());
    }

    private JsonNode body(String response) throws Exception {
        return json.readTree(response);
    }

    private UsernamePasswordAuthenticationToken actor(UUID userId, String role) {
        AuthenticatedUser user = new AuthenticatedUser(userId, "integrated@example.test", "integrated",
                "test", true, Set.of(role), Set.of());
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    static final class AdjustableClock extends Clock {
        private final AtomicReference<Instant> instant;
        private final ZoneId zone;

        private AdjustableClock(Instant instant, ZoneId zone) {
            this(new AtomicReference<>(instant), zone);
        }

        private AdjustableClock(AtomicReference<Instant> instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        void set(Instant value) {
            instant.set(value);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId newZone) {
            return new AdjustableClock(instant, newZone);
        }

        @Override
        public Instant instant() {
            return instant.get();
        }
    }
}
